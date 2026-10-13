<#
.SYNOPSIS
    Installs and launches the latest TaskPulse build on connected phones or emulators.
#>

$ErrorActionPreference = "Continue"

$sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
if (-not (Test-Path $sdkPath)) {
    $sdkPath = "C:\Users\daleb\AppData\Local\Android\Sdk"
}

$adb = Join-Path $sdkPath "platform-tools\adb.exe"
$emulator = Join-Path $sdkPath "emulator\emulator.exe"
$avdName = "Medium_Phone_API_35"

Write-Host "=========================================" -ForegroundColor Magenta
Write-Host "          TaskPulse App Launcher         " -ForegroundColor Magenta
Write-Host "=========================================" -ForegroundColor Magenta

# Function to get currently connected authorized devices
function Get-ActiveDevices {
    $lines = & $adb devices 2>$null
    $serials = @()
    foreach ($line in $lines) {
        if ($line -match "^\s*([A-Za-z0-9_\-]+)\s+device\b") {
            $serials += $matches[1]
        }
    }
    return $serials
}

# 1. Check for any connected physical phone or emulator
Write-Host "[1/4] Checking for connected devices or phones..." -ForegroundColor Cyan
$activeSerials = Get-ActiveDevices

if ($activeSerials.Count -gt 0) {
    Write-Host "Found connected device(s): $($activeSerials -join ', ')" -ForegroundColor Green
    Write-Host "Using your connected device (skipping emulator launch)." -ForegroundColor Green
} else {
    Write-Host "No active phone detected. Starting emulator '$avdName'..." -ForegroundColor Yellow
    if (-not (Test-Path $emulator)) {
        Write-Error "Emulator executable not found at: $emulator"
        exit 1
    }

    Start-Process -FilePath $emulator -ArgumentList "-avd $avdName" -WindowStyle Normal
    Write-Host "Waiting for emulator to initialize and boot (this may take ~30-60s)..." -ForegroundColor Yellow

    # Wait until adb recognizes the emulator
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 2
        $activeSerials = Get-ActiveDevices
        if ($activeSerials.Count -gt 0) {
            break
        }
    }

    # Wait until boot is completed
    for ($i = 0; $i -lt 60; $i++) {
        $bootProp = (& $adb -e shell getprop sys.boot_completed 2>$null) | Out-String
        if ($bootProp.Trim() -eq "1") {
            break
        }
        Start-Sleep -Seconds 2
    }
    Write-Host "Emulator ready!" -ForegroundColor Green
}

# 2. Build Debug APK with latest code changes
Write-Host "[2/4] Building latest TaskPulse Debug APK..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug

# 3. Install on all connected devices
Write-Host "[3/4] Installing TaskPulse..." -ForegroundColor Cyan
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"

$activeSerials = Get-ActiveDevices
if ($activeSerials.Count -gt 0) {
    foreach ($serial in $activeSerials) {
        Write-Host "  -> Installing to device ($serial)..." -ForegroundColor Cyan
        & $adb -s $serial install -r $apkPath
    }
} else {
    Write-Host "Using Gradle install fallback..." -ForegroundColor Yellow
    .\gradlew.bat installDebug
}

# 4. Launch the app on all active devices
Write-Host "[4/4] Launching TaskPulse MainActivity..." -ForegroundColor Cyan
$activeSerials = Get-ActiveDevices
if ($activeSerials.Count -gt 0) {
    foreach ($serial in $activeSerials) {
        Write-Host "  -> Launching on $serial..." -ForegroundColor Cyan
        & $adb -s $serial shell am start -n com.example.taskpulse/.MainActivity | Out-Null
    }
} else {
    & $adb shell am start -n com.example.taskpulse/.MainActivity 2>$null
}

Write-Host "=========================================" -ForegroundColor Green
Write-Host " TaskPulse is now running on your phone! 🚀" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
