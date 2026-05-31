# Сборка установочного APK для DFKons
# Запуск: правый клик -> "Выполнить с помощью PowerShell" или: .\build-installer.ps1

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$Version = "1.0"
$ApkName = "DFKons-$Version.apk"

# Папка, куда копируется готовый установочник
$OutputDir = Join-Path $env:USERPROFILE "OneDrive\Рабочий стол\по диплому"
if (-not (Test-Path $OutputDir)) {
    $OutputDir = Join-Path $ProjectRoot "release"
}
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

Write-Host "=== Сборка release APK ===" -ForegroundColor Cyan
Set-Location $ProjectRoot
& .\gradlew.bat assembleRelease --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка сборки. См. вывод Gradle выше." -ForegroundColor Red
    exit 1
}

$releaseApk = Get-ChildItem -Path "$ProjectRoot\app\build\outputs\apk\release" -Filter "*.apk" -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $releaseApk) {
    Write-Host "Release не найден, собираем debug (тоже устанавливается на телефон)..." -ForegroundColor Yellow
    & .\gradlew.bat assembleDebug --no-daemon
    if ($LASTEXITCODE -ne 0) { exit 1 }
    $releaseApk = Get-ChildItem -Path "$ProjectRoot\app\build\outputs\apk\debug" -Filter "app-debug.apk" | Select-Object -First 1
    $ApkName = "DFKons-$Version-debug.apk"
}

if (-not $releaseApk) {
    Write-Host "APK не найден." -ForegroundColor Red
    exit 1
}

$dest = Join-Path $OutputDir $ApkName
Copy-Item -Path $releaseApk.FullName -Destination $dest -Force

Write-Host ""
Write-Host "Готово!" -ForegroundColor Green
Write-Host "Установочник (APK):" -ForegroundColor Yellow
Write-Host "  $dest"
Write-Host ""
Write-Host "Исходный файл сборки:" -ForegroundColor Yellow
Write-Host "  $($releaseApk.FullName)"
Write-Host ""
Write-Host "Размер: $([math]::Round($dest.Length / 1MB, 2)) МБ" -ForegroundColor Gray
Write-Host ""
Write-Host "Установка на телефон: скопируйте APK на устройство и откройте файл" -ForegroundColor Gray
Write-Host "(разрешите установку из неизвестных источников)." -ForegroundColor Gray
