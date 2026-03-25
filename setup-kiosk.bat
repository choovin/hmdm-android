@echo off
chcp 65001 >nul
echo ============================================
echo Headwind MDM Kiosk模式设置 (Windows CMD)
echo ============================================
echo.

REM 设备序列号 (留空自动选择)
set DEVICE_SERIAL=

REM MDM应用包名
set MDM_PACKAGE=com.hmdm.launcher

REM 获取设备序列号
if "%DEVICE_SERIAL%"=="" (
    for /f "delims=" %%i in ('adb devices ^| findstr device$ ^| findstr /v "List"') do set "devices=%%i"
    for %%a in (%devices%) do (
        if not "%%a"=="" (
            set "SERIAL=%%a"
        )
    )
    if not defined SERIAL (
        echo [错误] 未找到ADB设备
        pause
        exit /b 1
    )
) else (
    set SERIAL=%DEVICE_SERIAL%
)

echo [信息] 使用设备: %SERIAL%
echo.

REM ========== 步骤1: 检查设备连接 ==========
echo [步骤1/9] 检查设备连接...
adb -s %SERIAL% shell echo 1 >nul
if errorlevel 1 (
    echo [错误] 设备连接失败
    pause
    exit /b 1
)
echo [OK] 设备已连接
echo.

REM ========== 步骤2: 设置Device Owner ==========
echo [步骤2/9] 设置Device Owner...
echo [警告] 此操作需要设备已设置锁屏密码
adb -s %SERIAL% shell dpm set-device-owner --device-owner-only %MDM_PACKAGE%/.AdminReceiver
if errorlevel 1 (
    echo [错误] Device Owner设置失败
    echo 可能原因:
    echo   1. 设备未设置锁屏密码
    echo   2. 设备已有其他Device Owner
    echo   3. 设备屏幕未解锁
) else (
    echo [OK] Device Owner设置成功
)
echo.

REM ========== 步骤3: 重启应用 ==========
echo [步骤3/9] 重启MDM应用...
adb -s %SERIAL% shell am force-stop %MDM_PACKAGE%
timeout /t 2 /nobreak >nul
adb -s %SERIAL% shell am start -n %MDM_PACKAGE%/.ui.MainActivity
echo [OK] 应用已重启
echo.

REM ========== 步骤4: 检查悬浮窗权限 ==========
echo [步骤4/9] 检查悬浮窗权限...
adb -s %SERIAL% shell dumpsys package %MDM_PACKAGE% | findstr /C:"SYSTEM_ALERT_WINDOW" >nul
if errorlevel 1 (
    echo [警告] 请手动授予悬浮窗权限
    echo 设置 -^> 应用 -^> %MDM_PACKAGE% -^> 悬浮窗 -^> 允许
) else (
    echo [OK] 悬浮窗权限已授予
)
echo.

REM ========== 步骤5: 设置为主桌面Launcher ==========
echo [步骤5/9] 设置为主桌面Launcher...
adb -s %SERIAL% shell "settings put secure home_launcher %MDM_PACKAGE%"
echo [OK] 已设置为主桌面
echo.

REM ========== 步骤6: 触发配置同步 ==========
echo [步骤6/9] 触发配置同步...
adb -s %SERIAL% shell "am broadcast -a com.hmdm.launcher.UPDATE_CONFIG"
echo [OK] 配置同步已触发
echo.

REM ========== 步骤7: 等待配置应用 ==========
echo [步骤7/9] 等待配置应用 (10秒)...
timeout /t 10 /nobreak >nul

REM ========== 步骤8: 检查Kiosk模式 ==========
echo [步骤8/9] 检查Kiosk模式状态...
adb -s %SERIAL% shell "dumpsys activity activities" | findstr /C:"LockTaskController" >temp_locktask.txt
adb -s %SERIAL% shell "dumpsys activity activities" | findstr /C:"mLockTaskModeState=LOCKED" >>temp_locktask.txt
findstr "LOCKED" temp_locktask.txt >nul
if errorlevel 1 (
    echo [警告] Kiosk模式可能未启动
    echo 请检查服务器配置是否启用Kiosk模式
) else (
    echo.
    echo ============================================
    echo [OK] 设备已进入Kiosk模式!
    echo ============================================
    echo 退出Kiosk: 点击右上角钥匙图标4次
)
del temp_locktask.txt 2>nul
echo.

REM ========== 步骤9: Device Owner状态 ==========
echo [步骤9/9] Device Owner状态:
adb -s %SERIAL% shell "dumpsys device_policy" | findstr /C:"Device Owner" /C:"Device Admin"
echo.

echo ============================================
echo [完成] 脚本执行完毕
echo ============================================
pause
