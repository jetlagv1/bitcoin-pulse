param([switch]$Offline)
$ErrorActionPreference = 'Stop'
$pulseProjectRoot = Split-Path -Parent $PSScriptRoot
$pulseEnvironment = Join-Path $pulseProjectRoot '../.tools/android-env.ps1'
if (Test-Path -LiteralPath $pulseEnvironment) { . $pulseEnvironment }
if (-not $pulseGradle) { $pulseGradle = Join-Path $pulseProjectRoot 'gradlew.bat' }
$pulseCredentialsFile = Join-Path $pulseProjectRoot '../.tools/android-build/release-signing/upload-key.json'
if (-not (Test-Path -LiteralPath $pulseCredentialsFile)) { throw 'Create your upload key first, or configure PULSE_UPLOAD_* and build with Gradle directly. See PLAY-STORE.md.' }
$pulseSigning = Get-Content -Raw -LiteralPath $pulseCredentialsFile | ConvertFrom-Json
try {
    $env:PULSE_UPLOAD_STORE_FILE = $pulseSigning.storeFile
    $env:PULSE_UPLOAD_STORE_PASSWORD = $pulseSigning.password
    $env:PULSE_UPLOAD_KEY_ALIAS = $pulseSigning.keyAlias
    $env:PULSE_UPLOAD_KEY_PASSWORD = $pulseSigning.password
    $pulseArguments = @(':app:bundleRelease', ':app:testDebugUnitTest', ':app:lintRelease', '--console=plain')
    if ($Offline) { $pulseArguments += '--offline' }
    Push-Location $pulseProjectRoot
    try {
        & $pulseGradle @pulseArguments
        if ($LASTEXITCODE -ne 0) { throw "Play release build failed (exit $LASTEXITCODE)." }
    } finally { Pop-Location }
} finally {
    Remove-Item Env:PULSE_UPLOAD_STORE_FILE,Env:PULSE_UPLOAD_STORE_PASSWORD,Env:PULSE_UPLOAD_KEY_ALIAS,Env:PULSE_UPLOAD_KEY_PASSWORD -ErrorAction SilentlyContinue
    $pulseSigning = $null
}
