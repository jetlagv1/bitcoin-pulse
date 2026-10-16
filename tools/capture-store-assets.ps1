$ErrorActionPreference = 'Stop'
$pulseProjectRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $pulseProjectRoot '../.tools/android-env.ps1')
$pulseCaptureSource = Join-Path $PSScriptRoot 'store-capture/StoreAssetCaptureTest.kt'
$pulseCaptureTarget = Join-Path $pulseProjectRoot 'app/src/test/java/com/buildapptest/bitcoinpulse/StoreAssetCaptureTest.kt'
if (Test-Path -LiteralPath $pulseCaptureTarget) { throw 'The temporary capture source already exists; inspect it before replacing it.' }
Push-Location $pulseProjectRoot
try {
    python tools/capture-public-data.py
    if ($LASTEXITCODE -ne 0) { throw 'Public API capture failed.' }
    Copy-Item -LiteralPath $pulseCaptureSource -Destination $pulseCaptureTarget
    try {
        & $pulseGradle :app:testDebugUnitTest --tests '*StoreAssetCaptureTest' --offline --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Native asset rendering failed.' }
    } finally {
        Remove-Item -LiteralPath $pulseCaptureTarget -Force
    }
    node tools/export-store-icon.cjs
    node tools/package-store-assets.cjs
} finally { Pop-Location }
