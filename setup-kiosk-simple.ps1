# Headwind MDM Kiosk模式设置 - 直接执行版
# 保存到桌面后，右键 -> "使用 PowerShell 运行"
# 需要先连接设备并启用USB调试

# ========== 修改这里 ==========
$MDM_PACKAGE = "com.hmdm.launcher"
$KIOSK_APP_PACKAGE = "com.hmdm.launcher"  # 锁定的应用包名
# ============================

$ErrorActionPreference = "Continue"

function Get-ADBDevice {
    $devices = adb devices | Select-String "device$" | ForEach-Object { ($_ -split "`t")[0] } | Where-Object { $_ -ne "" }
    if ($devices.Count -eq 0) {
        Write-Host "[ERROR] 未找到ADB设备" -ForegroundColor Red
        exit 1
    }
    if ($devices.Count -gt 1) {
        Write-Host "[ERROR] 发现多个设备，请指定序列号" -ForegroundColor Red
        Write-Host "可用设备: $($devices -join ', ')" -ForegroundColor Yellow
        exit 1
    }
    return $devices[0]
}

$serial = Get-ADBDevice
Write-Host "[INFO] 使用设备: $serial" -ForegroundColor Cyan

# 1. 设置Device Owner
Write-Host "[步骤1/8] 设置Device Owner..." -ForegroundColor Yellow
$adminReceiver = "$MDM_PACKAGE/.AdminReceiver"
adb -s $serial shell dpm set-device-owner --device-owner-only $adminReceiver 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Device Owner设置成功" -ForegroundColor Green
} else {
    Write-Host "[WARN] Device Owner设置失败，尝试强制设置..." -ForegroundColor Yellow
}

# 2. 强制停止应用
Write-Host "[步骤2/8] 重启MDM应用..." -ForegroundColor Yellow
adb -s $serial shell am force-stop $MDM_PACKAGE
Start-Sleep -Seconds 1

# 3. 启动应用
Write-Host "[步骤3/8] 启动MainActivity..." -ForegroundColor Yellow
adb -s $serial shell am start -n $MDM_PACKAGE/.ui.MainActivity
Start-Sleep -Seconds 2

# 4. 授予悬浮窗权限 (如果需要)
Write-Host "[步骤4/9] 检查悬浮窗权限..." -ForegroundColor Yellow
$hasOverlay = adb -s $serial shell "dumpsys package $MDM_PACKAGE | Select-String SYSTEM_ALERT_WINDOW"
if ($hasOverlay) {
    Write-Host "[OK] 悬浮窗权限已授予" -ForegroundColor Green
} else {
    Write-Host "[WARN] 请手动授予悬浮窗权限" -ForegroundColor Yellow
    Write-Host "设置 -> 应用 -> $MDM_PACKAGE -> 悬浮窗 -> 允许" -ForegroundColor Yellow
}

# 5. 设置为主桌面Launcher
Write-Host "[步骤5/9] 设置为主桌面Launcher..." -ForegroundColor Yellow
adb -s $serial shell "settings put secure home_launcher $MDM_PACKAGE"
Write-Host "[OK] 已设置为主桌面" -ForegroundColor Green

# 6. 触发配置同步
Write-Host "[步骤6/9] 触发配置同步..." -ForegroundColor Yellow
adb -s $serial shell "am broadcast -a com.hmdm.launcher.UPDATE_CONFIG"
Write-Host "[OK] 配置同步已触发" -ForegroundColor Green

# 7. 等待配置应用
Write-Host "[步骤7/9] 等待配置应用 (10秒)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 8. 检查Kiosk模式状态
Write-Host "[步骤8/9] 检查Kiosk模式状态..." -ForegroundColor Yellow
$lockTask = adb -s $serial shell "dumpsys activity activities | Select-String LockTaskController -Context 0,3"
if ($lockTask -match "mLockTaskModeState=LOCKED") {
    Write-Host ""
    Write-Host "############################################" -ForegroundColor Green
    Write-Host "[OK] 设备已进入Kiosk模式!" -ForegroundColor Green
    Write-Host "############################################" -ForegroundColor Green
    Write-Host ""
    Write-Host "退出Kiosk模式: 点击右上角钥匙图标4次" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "[WARN] Kiosk模式可能未自动启动" -ForegroundColor Yellow
    Write-Host "可能原因:" -ForegroundColor Yellow
    Write-Host "  1. 服务器配置中未启用Kiosk模式" -ForegroundColor Yellow
    Write-Host "  2. 服务器配置中未设置mainAppId/contentAppId" -ForegroundColor Yellow
    Write-Host "  3. 设备未成功同步服务器配置" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "请检查服务器配置，或手动在应用中触发同步" -ForegroundColor Yellow
}

# 9. 检查Device Owner状态
Write-Host ""
Write-Host "[步骤9/9] Device Owner状态:" -ForegroundColor Yellow
$owner = adb -s $serial shell "dumpsys device_policy | Select-String 'Device Owner' -Context 0,1"
Write-Host $owner -ForegroundColor Cyan

Write-Host ""
Write-Host "[完成] 脚本执行完毕" -ForegroundColor Magenta
Start-Sleep -Seconds 2
