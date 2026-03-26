# ============================================================
# runnode MDM Android 安装脚本 (Windows 11) - PowerShell Edition
# 功能：自动检测USB平板，批量安装APK并设置权限
# 美化版：带进度条、动画、彩色输出
# ============================================================

param(
    [switch]$Verbose = $false
)

# ============================================================
# 脚本配置
# ============================================================
$Script:ScriptDir = $PSScriptRoot
$Script:APKName = "hmdm-android-6.45-release.apk"
$Script:ProAPKName = "v4.0.0-prod.apk"
$Script:ADBDir = Join-Path $ScriptDir "adb-windows"
$Script:APKPath = Join-Path $ScriptDir $Script:APKName
$Script:ProAPKPath = Join-Path $ScriptDir $Script:ProAPKName
$Script:PKGName = "com.hmdm.launcher"

# ============================================================
# 控制台美化配置
# ============================================================

# Terminal color escape sequences (ANSI)
$AnsiReset = "`e[0m"
$AnsiBold = "`e[1m"
$AnsiDim = "`e[2m"

# Foreground colors
$AnsiBlack = "`e[30m"
$AnsiRed = "`e[31m"
$AnsiGreen = "`e[32m"
$AnsiYellow = "`e[33m"
$AnsiBlue = "`e[34m"
$AnsiMagenta = "`e[35m"
$AnsiCyan = "`e[36m"
$AnsiWhite = "`e[37m"

# Background colors
$AnsiBgBlack = "`e[40m"
$AnsiBgGreen = "`e[42m"
$AnsiBgYellow = "`e[43m"
$AnsiBgBlue = "`e[44m"
$AnsiBgRed = "`e[41m"

# ============================================================
# 辅助函数
# ============================================================

function Write-Banner {
    param([string]$Title, [string]$Subtitle = "")

    $banner = @"

    ${AnsiCyan}╔══════════════════════════════════════════════════════════════╗
    ║${AnsiBold}${AnsiCyan}  ███████╗██╗   ██╗ ██████╗ ██████╗███████╗███████╗███████╗${AnsiReset}  ║
    ║${AnsiBold}${AnsiCyan}  ██╔════╝██║   ██║██╔════╝██╔════╝██╔════╝██╔════╝██╔════╝${AnsiReset}  ║
    ║${AnsiBold}${AnsiCyan}  ███████╗██║   ██║██║     ██║     █████╗  ███████╗███████╗${AnsiReset}  ║
    ║${AnsiBold}${AnsiCyan}  ╚════██║██║   ██║██║     ██║     ██╔══╝  ╚════██║╚════██║${AnsiReset}  ║
    ║${AnsiBold}${AnsiCyan}  ███████║╚██████╔╝╚██████╗╚██████╗███████╗███████║███████║${AnsiReset}  ║
    ║${AnsiBold}${AnsiCyan}  ╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝╚══════╝╚══════╝╚══════╝${AnsiReset}  ║
    ╚══════════════════════════════════════════════════════════════╝${AnsiReset}

"@
    Write-Host $banner
    if ($Title) {
        Write-Host "${AnsiBold}${AnsiWhite}[ $Title ]${AnsiReset}" -NoNewline
    }
    if ($Subtitle) {
        Write-Host " ${AnsiDim}v$Subtitle${AnsiReset}"
    }
    Write-Host ""
}

function Write-Step {
    param(
        [int]$Current,
        [int]$Total,
        [string]$Message
    )

    $percent = [math]::Round(($Current / $Total) * 100)
    $filled = [math]::Round($percent / 2)
    $empty = 50 - $filled

    $progressBar = "[" + ("█" * $filled) + ("░" * $empty) + "]"
    $stepInfo = "${AnsiCyan}[$Current/$Total]${AnsiReset}"

    Write-Host "$stepInfo $progressBar ${AnsiYellow}$Message${AnsiReset}"
}

function Write-Success {
    param([string]$Message)
    $check = "${AnsiGreen}✓${AnsiReset}"
    Write-Host "$check $Message"
}

function Write-Error {
    param([string]$Message)
    $cross = "${AnsiRed}✗${AnsiReset}"
    Write-Host "$cross $Message" -ForegroundColor Red
}

function Write-Warning {
    param([string]$Message)
    $warn = "${AnsiYellow}⚠${AnsiReset}"
    Write-Host "$warn $Message" -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    $info = "${AnsiBlue}ℹ${AnsiReset}"
    Write-Host "$info $Message" -ForegroundColor Cyan
}

function Write-Spinner {
    param(
        [string]$Message,
        [int]$Duration = 2
    )

    $spinChars = @("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    $endTime = (Get-Date).AddSeconds($Duration)

    while ((Get-Date) -lt $endTime) {
        foreach ($char in $spinChars) {
            Write-Host "`r${AnsiCyan}$char${AnsiReset} $Message" -NoNewline
            Start-Sleep -Milliseconds 80
        }
    }
    Write-Host "`r" -NoNewline
    Write-Host (" " * 50) -NoNewline
    Write-Host "`r" -NoNewline
}

function Write-ProgressBar {
    param(
        [int]$Percent,
        [int]$Width = 40,
        [string]$Prefix = "",
        [string]$Suffix = ""
    )

    $filled = [math]::Round($Percent / (100 / $Width))
    $empty = $Width - $filled
    $bar = ("█" * $filled) + ("░" * $empty)

    $color = if ($Percent -lt 30) { "Red" }
              elseif ($Percent -lt 70) { "Yellow" }
              else { "Green" }

    Write-Host "`r${AnsiCyan}$Prefix${AnsiReset} [$bar] ${Percent}% $Suffix" -NoNewline
}

function Test-FileExists {
    param([string]$Path, [string]$Name)

    if (-not (Test-Path $Path)) {
        Write-Error "找不到文件: $Name"
        return $false
    }
    return $true
}

function Clear-Line {
    Write-Host (" " * 80) -NoNewline
    Write-Host "`r" -NoNewline
}

# ============================================================
# 进度条动画函数
# ============================================================

function Show-InstallationProgress {
    param([string]$APKName)

    for ($i = 0; $i -le 100; $i += 5) {
        Write-ProgressBar -Percent $i -Prefix "安装中" -Suffix $APKName
        Start-Sleep -Milliseconds 50
    }
    Clear-Line
}

function Show-DeviceDetection {
    Write-Host "${AnsiCyan}正在检测设备...${AnsiReset}" -NoNewline

    $spinChars = @("○", "◔", "◑", "◕", "●")
    for ($i = 0; $i -lt 20; $i++) {
        $idx = $i % $spinChars.Length
        Write-Host "`r${AnsiCyan}$($spinChars[$idx])${AnsiReset} 等待 Android 设备连接..." -NoNewline
        Start-Sleep -Milliseconds 150
    }
    Write-Host "`r" -NoNewline
    Clear-Line
}

# ============================================================
# 主程序
# ============================================================

function Main {
    # 检查管理员权限
    $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

    # 设置控制台
    $host.UI.RawUI.WindowTitle = "runnode MDM 安装向导 v6.45"
    $host.UI.RawUI.BackgroundColor = "Black"

    # 打印标题
    Clear-Host
    Write-Banner -Title "runnode MDM Android 安装向导" -Subtitle "6.45"

    if (-not $isAdmin) {
        Write-Warning "建议以管理员身份运行，以获得最佳体验"
        Write-Host ""
    }

    # ============================================================
    # 步骤 1: 检查环境
    # ============================================================
    Write-Step -Current 1 -Total 7 -Message "检查安装环境"
    Write-Host ""

    # 检查 APK 文件
    Write-Host "  检查 MDM APK..." -NoNewline
    if (Test-FileExists -Path $APKPath -Name $APKName) {
        Write-Success "MDM APK 已找到: $APKName"
    } else {
        Write-Error "MDM APK 文件不存在: $APKPath"
        Write-Host "${AnsiYellow}请确保 $APKName 在脚本同一目录下${AnsiReset}"
        Read-Host "按 Enter 退出"
        exit 1
    }

    # 检查 Pro APK
    Write-Host "  检查 Pro APK..." -NoNewline
    if (Test-FileExists -Path $ProAPKPath -Name $ProAPKName) {
        Write-Success "Pro APK 已找到: $ProAPKName"
    } else {
        Write-Warning "Pro APK 文件不存在: $ProAPKPath"
    }

    # 检查 ADB
    Write-Host "  检查 ADB 工具..." -NoNewline
    $adbExe = Join-Path $ADBDir "adb.exe"
    if (Test-FileExists -Path $adbExe -Name "adb.exe") {
        Write-Success "ADB 工具已找到"
    } else {
        Write-Error "ADB 工具不存在: $ADBDir"
        Read-Host "按 Enter 退出"
        exit 1
    }

    Write-Host ""

    # ============================================================
    # 步骤 2: 启动 ADB 服务
    # ============================================================
    Write-Step -Current 2 -Total 7 -Message "启动 ADB 服务"
    Write-Host ""

    Write-Spinner -Message "正在启动 ADB 服务" -Duration 2
    $null = & "$adbExe" start-server 2>$null

    # 检查 ADB 版本
    $adbVersion = & "$adbExe" version 2>$null | Select-Object -First 1
    Write-Success "ADB 服务已启动"
    Write-Info "ADB 版本: $adbVersion"

    # ============================================================
    # 主循环: 等待并处理设备
    # ============================================================
    Write-Host ""

    :deviceLoop while ($true) {
        Write-Step -Current 3 -Total 7 -Message "等待检测 Android 设备"
        Write-Host ""

        Write-Host "  ┌────────────────────────────────────────────────────────┐"
        Write-Host "  │                                                        │"
        Write-Host "  │  ${AnsiYellow}请使用 USB 线将 Android 平板连接到电脑${AnsiReset}         │"
        Write-Host "  │                                                        │"
        Write-Host "  │  确保平板已开启 USB 调试模式:                           │"
        Write-Host "  │    设置 → 关于平板电脑 → 连续点击版本号(7次)          │"
        Write-Host "  │    设置 → 开发者选项 → 开启 USB 调试                   │"
        Write-Host "  │                                                        │"
        Write-Host "  └────────────────────────────────────────────────────────┘"
        Write-Host ""

        # 等待设备
        Show-DeviceDetection
        $null = & "$adbExe" wait-for-device 2>$null

        Write-Success "检测到已连接的设备"
        Write-Host ""

        # 获取设备信息
        Write-Host "  正在获取设备信息..." -NoNewline
        Start-Sleep -Milliseconds 500

        $deviceModel = & "$adbExe" shell getprop ro.product.model 2>$null | Select-Object -First 1
        $androidVersion = & "$adbExe" shell getprop ro.build.version.release 2>$null | Select-Object -First 1
        $serial = & "$adbExe" shell getprop ro.serialno 2>$null | Select-Object -First 1

        Write-Success "设备信息获取完成"
        Write-Host ""
        Write-Host "  ┌────────────────────────────────────────────────────────┐"
        Write-Host "  │  ${AnsiCyan}设备信息${AnsiReset}                                          │"
        Write-Host "  ├────────────────────────────────────────────────────────┤"
        Write-Host "  │  型号:     ${AnsiWhite}$deviceModel${AnsiReset}" -NoNewline
        Write-Host " " * (42 - $deviceModel.Length) + "│"
        Write-Host "  │  Android:  ${AnsiWhite}$androidVersion${AnsiReset}" -NoNewline
        Write-Host " " * (42 - $androidVersion.Length) + "│"
        Write-Host "  │  序列号:   ${AnsiWhite}$serial${AnsiReset}" -NoNewline
        Write-Host " " * (42 - $serial.Length) + "│"
        Write-Host "  └────────────────────────────────────────────────────────┘"
        Write-Host ""

        # ============================================================
        # 步骤 4: 安装 APK
        # ============================================================
        Write-Step -Current 4 -Total 7 -Message "安装 MDM 应用"
        Write-Host ""

        # 安装 MDM APK
        Write-Host "  安装 MDM 主应用..." -NoNewline
        Write-Host ""

        # 模拟进度条
        $installJob = Start-Job -ScriptBlock {
            param($adb, $apk)
            & $adb install -r $apk 2>$null
        } -ArgumentList $adbExe, $APKPath

        while ($installJob.State -eq "Running") {
            Write-ProgressBar -Percent (Get-Random -Minimum 10 -Maximum 90) -Prefix "安装中" -Suffix (Split-Path $APKName -Leaf)
            Start-Sleep -Milliseconds 200
        }

        $installResult = Receive-Job -Job $installJob
        Remove-Job -Job $installJob

        if ($LASTEXITCODE -eq 0) {
            Write-Success "MDM 主应用安装成功"
        } else {
            Write-Error "MDM APK 安装失败"
            Write-Info "错误信息: $installResult"
        }

        # 安装 Pro APK
        Write-Host ""
        Write-Host "  安装 Pro 应用..." -NoNewline
        Write-Host ""

        $proInstallJob = Start-Job -ScriptBlock {
            param($adb, $apk)
            & $adb install -r $apk 2>$null
        } -ArgumentList $adbExe, $ProAPKPath

        while ($proInstallJob.State -eq "Running") {
            Write-ProgressBar -Percent (Get-Random -Minimum 10 -Maximum 90) -Prefix "安装中" -Suffix (Split-Path $ProAPKName -Leaf)
            Start-Sleep -Milliseconds 200
        }

        $proInstallResult = Receive-Job -Job $proInstallJob
        Remove-Job -Job $proInstallJob

        if ($LASTEXITCODE -eq 0) {
            Write-Success "Pro 应用安装成功"
        } else {
            Write-Warning "Pro APK 安装失败，继续执行..."
        }

        Write-Host ""

        # ============================================================
        # 步骤 5: 授予权限
        # ============================================================
        Write-Step -Current 5 -Total 7 -Message "授予必要权限"
        Write-Host ""

        $permissions = @(
            @{Name="存储"; Perm="android.permission.READ_EXTERNAL_STORAGE"},
            @{Name="存储写入"; Perm="android.permission.WRITE_EXTERNAL_STORAGE"},
            @{Name="电话"; Perm="android.permission.READ_PHONE_STATE"},
            @{Name="精确位置"; Perm="android.permission.ACCESS_FINE_LOCATION"},
            @{Name="模糊位置"; Perm="android.permission.ACCESS_COARSE_LOCATION"},
            @{Name="相机"; Perm="android.permission.CAMERA"},
            @{Name="录音"; Perm="android.permission.RECORD_AUDIO"},
            @{Name="蓝牙"; Perm="android.permission.BLUETOOTH"},
            @{Name="网络"; Perm="android.permission.INTERNET"},
            @{Name="自启动"; Perm="android.permission.RECEIVE_BOOT_COMPLETED"}
        )

        $permCount = 0
        $permTotal = $permissions.Count

        Write-Host "  ┌────────────────────────────────────────────────────────┐"
        Write-Host "  │  ${AnsiCyan}正在授予权限...${AnsiReset}                                   │"
        Write-Host "  └────────────────────────────────────────────────────────┘"
        Write-Host ""

        foreach ($perm in $permissions) {
            $permCount++
            $percent = [math]::Round(($permCount / $permTotal) * 100)

            Write-Host "  [" -NoNewline
            Write-Host "$permCount/$permTotal" -ForegroundColor Cyan -NoNewline
            Write-Host "]", $perm.Name.PadRight(12) -NoNewline
            Write-Host ("█" * [math]::Round($percent / 5)).PadRight(20) -NoNewline
            Write-Host " $percent%" -ForegroundColor Yellow

            $null = & "$adbExe" shell pm grant $PKGName $perm.Perm 2>$null
        }

        Write-Host ""
        Write-Success "所有权限授予完成"

        # ============================================================
        # 步骤 6: 设置 Device Owner
        # ============================================================
        Write-Host ""
        Write-Step -Current 6 -Total 7 -Message "设置设备管理员"
        Write-Host ""

        Write-Host "  正在清理旧的管理员权限..." -NoNewline
        $null = & "$adbExe" shell dpm remove-active-admin "$PKGName/.AdminReceiver" 2>$null
        Write-Success "清理完成"

        Write-Host "  正在注册设备管理员..." -NoNewline
        $dpmResult = & "$adbExe" shell dpm set-device-owner "$PKGName/.AdminReceiver" 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Success "设备管理员设置成功"
        } else {
            Write-Warning "设备所有者设置失败，可能需要手动确认"
            Write-Host ""
            Write-Host "  ${AnsiYellow}请在平板上手动操作:${AnsiReset}"
            Write-Host "    设置 → 安全 → 设备管理器 → runnode MDM"
            Write-Host "    选择"激活此设备管理员""
            Write-Host ""
        }

        # ============================================================
        # 步骤 7: 设置默认桌面
        # ============================================================
        Write-Host ""
        Write-Step -Current 7 -Total 7 -Message "设置默认桌面"
        Write-Host ""

        Write-Host "  正在设置 MDM 为默认桌面..." -NoNewline
        $null = & "$adbExe" shell cmd package set-home-activity --user 0 "$PKGName/.ui.MainActivity" 2>$null
        Write-Success "设置完成"

        Write-Host ""
        Write-Host "  ┌────────────────────────────────────────────────────────┐"
        Write-Host "  │  ${AnsiYellow}⚠ 重要提示${AnsiReset}                                       │"
        Write-Host "  ├────────────────────────────────────────────────────────┤"
        Write-Host "  │  如果平板弹出"选择默认桌面"对话框:                   │"
        Write-Host "  │    → 选择"runnode MDM"                              │"
        Write-Host "  │    → 勾选"始终使用"                                 │"
        Write-Host "  │                                                        │"
        Write-Host "  │  如果没有弹窗，手动设置:                               │"
        Write-Host "  │    设置 → 应用 → 默认应用 → 桌面                      │"
        Write-Host "  │    → 选择 runnode MDM                               │"
        Write-Host "  └────────────────────────────────────────────────────────┘"

        # ============================================================
        # 完成
        # ============================================================
        Write-Host ""
        Write-Host "${AnsiGreen}═══════════════════════════════════════════════════════════════${AnsiReset}"
        Write-Host ""
        Write-Host "${AnsiBold}${AnsiGreen}  ✓ 当前设备安装完成！${AnsiReset}"
        Write-Host ""
        Write-Host "  请在平板上确认以下操作:"
        Write-Host "    1. 同意设备管理员权限请求"
        Write-Host "    2. 选择默认桌面启动器"
        Write-Host "    3. 允许所有权限申请"
        Write-Host "    4. 平板将自动打开 MDM 配置界面"
        Write-Host ""
        Write-Host "${AnsiGreen}═══════════════════════════════════════════════════════════════${AnsiReset}"
        Write-Host ""

        # 询问继续
        Write-Host "${AnsiCyan}操作选项:${AnsiReset}" -ForegroundColor Cyan
        Write-Host "  [${AnsiGreen}C${AnsiReset}] 继续安装下一台设备"
        Write-Host "  [${AnsiRed}Q${AnsiReset}] 退出安装程序"
        Write-Host ""

        $choice = Read-Host "请输入选择 (C/Q)"

        if ($choice -eq "Q" -or $choice -eq "q") {
            Write-Host ""
            Write-Host "${AnsiCyan}感谢使用 runnode MDM 安装向导！${AnsiReset}"
            Write-Host "${AnsiDim}官网: https://h-mdm.com${AnsiReset}"
            exit 0
        }

        # 断开设备连接
        Write-Host ""
        Write-Host "${AnsiYellow}正在准备下一台设备...${AnsiReset}"
        $null = & "$adbExe" kill-server 2>$null
        Start-Sleep -Seconds 2
        $null = & "$adbExe" start-server 2>$null

        Clear-Host
        Write-Banner -Title "runnode MDM Android 安装向导" -Subtitle "6.45"
    }
}

# ============================================================
# 启动主程序
# ============================================================
try {
    Main
} catch {
    Write-Error "安装过程中发生错误: $_"
    Write-Host ""
    Read-Host "按 Enter 退出"
    exit 1
}
