@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion

:: ============================================================
:: runnode MDM Android 安装脚本 (Windows 11)
:: 功能：自动检测USB平板，批量安装APK并设置权限
:: ============================================================

set "SCRIPT_DIR=%~dp0"
set "APK_NAME=hmdm-android-6.45-release.apk"
set "PRO_APK_NAME=v4.0.0-prod.apk"
set "ADB_DIR=%SCRIPT_DIR%adb-windows"
set "APK_PATH=%SCRIPT_DIR%%APK_NAME%"
set "PRO_APK_PATH=%SCRIPT_DIR%%PRO_APK_NAME%"
set "PKG_NAME=com.hmdm.launcher"

:: 颜色定义
set "GREEN=Color 0A"
set "YELLOW=Color 0E"
set "RED=Color 0C"
set "CYAN=Color 09"
set "RESET=Color 07"

:: ============================================================
:: 函数定义
:: ============================================================
goto :skip_functions

:print_title
echo.
echo  ██████╗ ███████╗███████╗███████╗██████╗  ██████╗ ███████╗
echo  ██╔══██╗██╔════╝██╔════╝██╔════╝██╔══██╗██╔═══██╗██╔════╝
echo  ██████╔╝█████╗  ███████╗█████╗  ██████╔╝██║   ██║███████╗
echo  ██╔══██╗██╔══╝  ╚════██║██╔══╝  ██╔══██╗██║   ██║╚════██║
echo  ██║  ██║███████╗███████║███████╗██║  ██║╚██████╔╝███████║
echo  ╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝
echo.
echo  runnode MDM Android 安装向导 v6.45
echo  ============================================================
echo.
exit /b

:print_status
echo  [!] %~1
exit /b

:print_success
echo  [√] %~1
exit /b

:print_error
echo  [X] %~1
exit /b

:skip_functions

%CYAN%
call :print_title
%RESET%

:: ============================================================
:: 步骤1: 检查环境
:: ============================================================
echo  [步骤 1/7] 检查环境...
echo.

:: 检查 MDM APK 文件
if not exist "%APK_PATH%" (
    %RED%
    echo  [X] 错误：找不到 MDM APK 文件！
    echo  请确保 %APK_NAME% 在同一目录下
    %RESET%
    pause
    exit /b 1
)
%GREEN%
call :print_success "MDM APK 文件已找到: %APK_NAME%"
%RESET%

:: 检查 Pro APK 文件
if not exist "%PRO_APK_PATH%" (
    %RED%
    echo  [X] 错误：找不到 Pro APK 文件！
    echo  请确保 %PRO_APK_NAME% 在同一目录下
    %RESET%
    pause
    exit /b 1
)
%GREEN%
call :print_success "Pro APK 文件已找到: %PRO_APK_NAME%"
%RESET%

:: 检查 ADB
if not exist "%ADB_DIR%\adb.exe" (
    %RED%
    echo  [X] 错误：找不到 ADB 工具！
    echo  请确保 adb-windows 目录完整
    %RESET%
    pause
    exit /b 1
)
%GREEN%
call :print_success "ADB 工具已找到"
%RESET%

:: ============================================================
:: 步骤2: 启动 ADB 服务
:: ============================================================
echo.
echo  [步骤 2/7] 启动 ADB 服务...
echo.

"%ADB_DIR%\adb.exe" start-server >nul 2>&1
if errorlevel 1 (
    %RED%
    echo  [X] ADB 服务启动失败
    echo  请确保 Android 设备已通过 USB 连接到电脑
    %RESET%
    pause
    exit /b 1
)
%GREEN%
call :print_success "ADB 服务启动成功"
%RESET%

:: ============================================================
:: 主循环：等待并处理设备
:: ============================================================
echo.
echo  [步骤 3/7] 等待检测 Android 设备...
echo.

:: 等待设备连接
echo  请使用 USB 线将 Android 平板连接到电脑
echo  确保平板已开启 USB 调试模式
echo.

:wait_for_device
"%ADB_DIR%\adb.exe" wait-for-device
if errorlevel 1 (
    %YELLOW%
    call :print_status "等待设备连接..."
    goto :wait_for_device
)

%GREEN%
call :print_success "检测到已连接的设备"
%RESET%

:: 获取设备信息
echo.
echo  正在获取设备信息...
for /f "tokens=2 delims=: " %%i in ('"%ADB_DIR%\adb.exe" shell getprop ro.product.model 2^>nul') do set "DEVICE_MODEL=%%i"
for /f "tokens=2 delims=: " %%i in ('"%ADB_DIR%\adb.exe" shell getprop ro.build.version.release 2^>nul') do set "ANDROID_VERSION=%%i"
for /f "tokens=2 delims=: " %%i in ('"%ADB_DIR%\adb.exe" shell getprop ro.serialno 2^>nul') do set "SERIAL=%%i"

echo  设备型号: !DEVICE_MODEL!
echo  Android 版本: !ANDROID_VERSION!
echo  序列号: !SERIAL!
echo.

:: ============================================================
:: 步骤4: 安装 APK (MDM + Pro)
:: ============================================================
echo  [步骤 4/7] 安装 MDM 应用 (MDM + Pro)...
echo.

:: 安装 MDM APK
echo  正在安装 MDM 主应用: %APK_NAME%
"%ADB_DIR%\adb.exe" install -r "%APK_PATH%"
if errorlevel 1 (
    %RED%
    call :print_error "MDM APK 安装失败"
    %RESET%
    pause
    exit /b 1
)
%GREEN%
call :print_success "MDM 主应用安装成功"
%RESET%

:: 安装 Pro APK
echo.
echo  正在安装 Pro 应用: %PRO_APK_NAME%
"%ADB_DIR%\adb.exe" install -r "%PRO_APK_PATH%"
if errorlevel 1 (
    %YELLOW%
    call :print_status "Pro APK 安装失败，继续执行..."
    %RESET%
) else (
    %GREEN%
    call :print_success "Pro 应用安装成功"
    %RESET%
)

:: ============================================================
:: 步骤5: 授予权限
:: ============================================================
echo.
echo  [步骤 5/7] 授予必要权限...
echo.

set "PERMISSIONS=android.permission.READ_EXTERNAL_STORAGE android.permission.WRITE_EXTERNAL_STORAGE android.permission.READ_PHONE_STATE android.permission.ACCESS_FINE_LOCATION android.permission.ACCESS_COARSE_LOCATION android.permission.CAMERA android.permission.RECORD_AUDIO android.permission.BLUETOOTH android.permission.BLUETOOTH_ADMIN android.permission.ACCESS_WIFI_STATE android.permission.CHANGE_WIFI_STATE android.permission.INTERNET android.permission.ACCESS_NETWORK_STATE android.permission.RECEIVE_BOOT_COMPLETED android.permission.WAKE_LOCK android.permission.FOREGROUND_SERVICE android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS android.permission.REQUEST_INSTALL_PACKAGES"

echo  正在授予存储权限...
"%ADB_DIR%\adb.exe" shell pm grant %PKG_NAME% android.permission.READ_EXTERNAL_STORAGE >nul 2>&1
"%ADB_DIR%\adb.exe" shell pm grant %PKG_NAME% android.permission.WRITE_EXTERNAL_STORAGE >nul 2>&1
%GREEN%
call :print_success "存储权限授予完成"
%RESET%

echo  正在授予电话权限...
"%ADB_DIR%\adb.exe" shell pm grant %PKG_NAME% android.permission.READ_PHONE_STATE >nul 2>&1
%GREEN%
call :print_success "电话权限授予完成"
%RESET%

echo  正在授予位置权限...
"%ADB_DIR%\adb.exe" shell pm grant %PKG_NAME% android.permission.ACCESS_FINE_LOCATION >nul 2>&1
"%ADB_DIR%\adb.exe" shell pm grant %PKG_NAME% android.permission.ACCESS_COARSE_LOCATION >nul 2>&1
%GREEN%
call :print_success "位置权限授予完成"
%RESET%

echo  正在授予相机和音频权限...
"%ADB_DIR%\adb.exe" shell pm grant %PKG_NAME% android.permission.CAMERA >nul 2>&1
"%ADB_DIR%\adb.exe" shell pm grant %PKG_NAME% android.permission.RECORD_AUDIO >nul 2>&1
%GREEN%
call :print_success "相机和音频权限授予完成"
%RESET%

echo  正在授予网络和蓝牙权限...
for %%p in (%PERMISSIONS%) do (
    "%ADB_DIR%\adb.exe" shell pm grant %PKG_NAME% %%p >nul 2>&1
)
%GREEN%
call :print_success "网络和蓝牙权限授予完成"
%RESET%

:: ============================================================
:: 步骤6: 设置 Device Owner
:: ============================================================
echo.
echo  [步骤 6/7] 设置设备管理员权限...
echo.

:: 先移除旧的设备管理员（如果存在）
echo  正在清理旧的管理员权限...
"%ADB_DIR%\adb.exe" shell dpm remove-active-admin %PKG_NAME%/.AdminReceiver >nul 2>&1

:: 设置新的设备所有者
echo  正在注册设备管理员...
"%ADB_DIR%\adb.exe" shell dpm set-device-owner %PKG_NAME%/.AdminReceiver
if errorlevel 1 (
    %YELLOW%
    echo  警告: 设备所有者设置失败，可能需要手动在设备上确认
    echo  请在平板上进入: 设置 -^> 安全 -^> 设备管理器 -^> 选择 runnode MDM
    echo  然后手动激活此应用为设备管理员
    %RESET%
) else (
    %GREEN%
    call :print_success "设备管理员设置成功"
    %RESET%
)

:: ============================================================
:: 步骤7: 设置默认桌面启动器
:: ============================================================
echo.
echo  [步骤 7/7] 设置默认桌面启动器...
echo.

:: 设置 MDM 为默认 Launcher
echo  正在设置 MDM 为默认桌面应用...
"%ADB_DIR%\adb.exe" shell cmd package set-home-activity --user 0 %PKG_NAME%/.ui.MainActivity >nul 2>&1

%GREEN%
call :print_success "已尝试设置 MDM 为默认桌面应用"
%RESET%

:: 提示用户确认默认启动器
echo.
%YELLOW%
echo  [!] 重要: 请在平板上确认以下操作:
echo  1. 如果平板弹出"选择默认启动器"对话框
echo  2. 请选择 "runnode MDM" 并设为始终使用
echo  3. 如果没有弹出，请在平板设置中手动设置:
echo     设置 -^> 应用 -^> 默认应用 -^> 桌面 -^> 选择 runnode MDM
%RESET%

:: ============================================================
:: 完成
:: ============================================================
echo.
echo  ============================================================
echo.
%GREEN%
echo   ███████╗██╗   ██╗ ██████╗ ██████╗███████╗███████╗███████╗
echo   ██╔════╝██║   ██║██╔════╝██╔════╝██╔════╝██╔════╝██╔════╝
echo   ███████╗██║   ██║██║     ██║     █████╗  ███████╗███████╗
echo   ╚════██║██║   ██║██║     ██║     ██╔══╝  ╚════██║╚════██║
echo   ███████║╚██████╔╝╚██████╗╚██████╗███████╗███████║███████║
echo   ╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝╚══════╝╚══════╝╚══════╝
%RESET%
echo.
echo  [!] 当前设备安装完成！
echo.
echo  请执行以下操作:
echo  1. 在平板上同意设备管理员权限请求
echo  2. 选择 "runnode MDM" 为默认桌面启动器（如有弹窗）
echo  3. 如有权限弹窗，请全部允许
echo  4. 平板将自动打开 MDM 配置界面
echo.
echo  按任意键继续安装下一台设备，或关闭窗口退出...
echo.

pause >nul

:: 断开当前设备连接，准备下一台
"%ADB_DIR%\adb.exe" kill-server >nul 2>&1
timeout /t 2 >nul
"%ADB_DIR%\adb.exe" start-server >nul 2>&1

:: 重新等待设备
goto :wait_for_device
