# Headwind MDM Kiosk模式设置脚本 (Windows PowerShell)
# 使用方法: 右键点击此文件 -> "使用 PowerShell 运行" 或在 PowerShell 中执行: .\setup-kiosk.ps1

# ========== 配置区域 ==========
# 请根据实际情况修改以下变量
$DEVICE_SERIAL = ""  # 留空则自动选择第一个设备，或指定序列号如 "LPHQ55T6208"
$MDM_PACKAGE = "com.hmdm.launcher"
$ADMIN_RECEIVER = "$MDM_PACKAGE/.AdminReceiver"
$KIOSK_APP_PACKAGE = "com.hmdm.launcher"  # Kiosk模式锁定的应用包名
# =====================================

$ErrorActionPreference = "Stop"

function Get-DeviceSerial {
    if ($DEVICE_SERIAL) {
        return $DEVICE_SERIAL
    }
    $devices = adb devices | Select-String "device$" | ForEach-Object { ($_ -split "`t")[0] }
    if ($devices.Count -eq 0) {
        Write-Error "未找到任何ADB设备，请先连接设备并启用USB调试"
    }
    if ($devices.Count -gt 1) {
        Write-Host "发现多个设备，请设置 DEVICE_SERIAL 变量指定序列号"
        Write-Host "可用设备: $($devices -join ', ')"
        Write-Error "多设备冲突"
    }
    return $devices[0]
}

function Run-Adb {
    param([string]$Serial, [string]$Command)
    $fullCmd = "adb"
    if ($Serial) { $fullCmd += " -s $Serial" }
    $fullCmd += " $Command"
    Write-Host "[CMD] $fullCmd" -ForegroundColor DarkGray
    return Invoke-Expression $fullCmd
}

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "=== $Message ===" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Err {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# ========== 开始 ==========
Write-Host ""
Write-Host "############################################" -ForegroundColor Magenta
Write-Host "#  Headwind MDM Kiosk模式设置向导" -ForegroundColor Magenta
Write-Host "############################################" -ForegroundColor Magenta
Write-Host ""

$serial = Get-DeviceSerial
Write-Step "步骤1: 检查设备连接"
Write-Host "设备序列号: $serial"
Write-Success "设备已连接"

Write-Step "步骤2: 卸载旧版MDM应用（如有）"
$uninstall = Read-Host "是否卸载旧版MDM应用? (y/N)"
if ($uninstall -eq "y" -or $uninstall -eq "Y") {
    Run-Adb $serial "shell pm uninstall $MDM_PACKAGE"
    Write-Success "已卸载"
}

Write-Step "步骤3: 安装MDM应用"
$apkPath = Read-Host "请输入APK路径 (留空使用当前目录下的app-release.apk)"
if (-not $apkPath) {
    $apkPath = ".\app\build\outputs\apk\opensource\release\app-release.apk"
}
if (Test-Path $apkPath) {
    Run-Adb $serial "install -r $apkPath"
    Write-Success "安装完成"
} else {
    Write-Err "APK文件不存在: $apkPath"
    exit 1
}

Write-Step "步骤4: 设置Device Owner"
Write-Host "此操作需要设备有锁屏密码，且会清除设备数据"
Write-Host "确保设备已连接到电脑，且屏幕处于解锁状态"
$confirm = Read-Host "是否继续设置Device Owner? (y/N)"
if ($confirm -ne "y" -and $confirm -ne "Y") {
    Write-Host "已取消"
    exit 0
}

Write-Step "设置Device Owner"
Run-Adb $serial "shell dpm set-device-owner --device-owner-only $ADMIN_RECEIVER"
if ($LASTEXITCODE -eq 0) {
    Write-Success "Device Owner设置成功"
} else {
    Write-Err "Device Owner设置失败，请确保:"
    Write-Err "1. 设备已设置锁屏密码 (设置 -> 安全 -> 锁屏密码)"
    Write-Err "2. 设备未设置其他Device Owner"
    Write-Err "3. 设备屏幕处于解锁状态"
    exit 1
}

Write-Step "步骤5: 检查Device Owner状态"
$output = Run-Adb $serial 'shell dumpsys device_policy | Select-String "Device Owner" -Context 0,2'
if ($output -match "Device Owner") {
    Write-Success "Device Owner已正确设置"
} else {
    Write-Err "Device Owner状态异常"
    exit 1
}

Write-Step "步骤6: 授予悬浮窗权限"
Run-Adb $serial "shell settings put secure method_to_confirm_ OverlaySettings 2 `$null 2>&1"
$overlay = Run-Adb $serial "shell settings get global policy_control"
if ($overlay -match "alertwindow") {
    Write-Success "悬浮窗权限已存在"
} else {
    Write-Host "请在设备上手动授权悬浮窗:"
    Write-Host "设置 -> 应用 -> $MDM_PACKAGE -> 悬浮窗 -> 允许"
    $confirm = Read-Host "已授权后按回车继续"
}

Write-Step "步骤7: 设置为主桌面Launcher"
Run-Adb $serial "shell settings put secure home_launcher $MDM_PACKAGE"
Write-Success "已设置为主桌面"

Write-Step "步骤8: 重启MDM应用"
Run-Adb $serial "shell am force-stop $MDM_PACKAGE"
Start-Sleep -Seconds 2
Run-Adb $serial "shell am start -n $MDM_PACKAGE/.ui.MainActivity"
Write-Success "应用已重启"

Write-Step "步骤9: 触发配置同步"
Run-Adb $serial 'shell am broadcast -a com.hmdm.launcher.UPDATE_CONFIG'
Write-Success "配置同步已触发"

Write-Step "步骤10: 进入Kiosk模式"
Write-Host "如果服务器已配置Kiosk模式，设备将自动进入"
Write-Host "Kiosk模式启动后，点击右上角钥匙图标4次可退出"
Start-Sleep -Seconds 5

# 检查Kiosk状态
Write-Step "检查Kiosk模式状态"
$lockTask = Run-Adb $serial 'shell dumpsys activity activities | Select-String "LockTaskController" -Context 0,4'
if ($lockTask -match "mLockTaskModeState=LOCKED") {
    Write-Success "设备已进入Kiosk模式!"
} else {
    Write-Warn "Kiosk模式可能未启动，请检查:"
    Write-Warn "1. 服务器配置中是否启用了Kiosk模式"
    Write-Warn "2. 服务器配置中是否设置了mainAppId"
    Write-Warn "3. 设备是否已成功同步服务器配置"
    Write-Host ""
    Write-Host "可通过以下命令查看设备日志:"
    Write-Host "  adb logcat | Select-String -Pattern 'kiosk|locktask|config'"
}

Write-Host ""
Write-Host "############################################" -ForegroundColor Magenta
Write-Host "#  设置完成!" -ForegroundColor Magenta
Write-Host "############################################" -ForegroundColor Magenta
Write-Host ""
