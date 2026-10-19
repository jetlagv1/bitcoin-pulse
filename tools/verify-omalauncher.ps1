param([switch]$RefreshSources, [switch]$RecordedData)
$ErrorActionPreference = 'Stop'
$pulseProject = Split-Path -Parent $PSScriptRoot
$pulseEnvironment = Join-Path $pulseProject '../.tools/android-env.ps1'
if (Test-Path -LiteralPath $pulseEnvironment) { . $pulseEnvironment }
if (-not $pulseGradle) { $pulseGradle = Join-Path $pulseProject 'gradlew.bat' }
$pulseCache = [IO.Path]::GetFullPath((Join-Path $pulseProject '../.tools/omalauncher-native-check'))
$pulseData = [IO.Path]::GetFullPath((Join-Path $pulseProject '../.tools/omalauncher-reference/data'))
$pulseCommit = 'edab9fba94d2d491b8ef474aa74e5715cae7db18'
New-Item -ItemType Directory -Force -Path $pulseCache,(Join-Path $pulseCache 'lang'),$pulseData | Out-Null
function Get-PulseReference($relative, $destination) {
    if ($RefreshSources -or -not (Test-Path -LiteralPath $destination)) {
        Invoke-WebRequest ("https://raw.githubusercontent.com/SimonSchubert/omalauncher/$pulseCommit/" + $relative) -OutFile $destination
    }
}
foreach ($pulseName in @('Render','Props','Canvas')) {
    Get-PulseReference "app/src/main/kotlin/dev/omalauncher/widgets/render/$pulseName.kt" (Join-Path $pulseCache "$pulseName.kt")
}
foreach ($pulseName in @('Ast','Builtins','Compiler','Json','Lexer','Parser','Program','ShellWords','Sprite','Value')) {
    Get-PulseReference "owl/src/main/kotlin/dev/omalauncher/widgets/lang/$pulseName.kt" (Join-Path $pulseCache "lang/$pulseName.kt")
}
Get-PulseReference 'LICENSE' (Join-Path $pulseCache 'LICENSE')
# Kotlin 2.1 compatibility only: allow `return null` in the unused point-map helper.
# No layout, text measurement, colors, drawing or pointer handling are changed.
$pulseCanvasPath = Join-Path $pulseCache 'Canvas.kt'
$pulseCanvas = [IO.File]::ReadAllText($pulseCanvasPath).Replace("`r`n", "`n")
if ($pulseCanvas.Contains('private fun point(v: Value): Offset? =')) {
    $pulseCanvas = $pulseCanvas.Replace("private fun point(v: Value): Offset? =`n        when", 'private fun point(v: Value): Offset? { return when')
    $pulseCanvas = $pulseCanvas.Replace("            else -> null`n        }`n}", "            else -> null`n        }`n    }`n}")
    [IO.File]::WriteAllText($pulseCanvasPath, $pulseCanvas)
}
$pulseUrls = @{
    ticker='https://api.kraken.com/0/public/Ticker?pair=XBTEUR,XBTUSD'
    blocks='https://mempool.space/api/v1/blocks'
    fees='https://mempool.space/api/v1/fees/recommended'
    pool='https://mempool.space/api/mempool'
}
foreach ($pulseCurrency in @('eur','usd')) {
    foreach ($pulsePeriod in @(@('4h',240),@('Day',1440),@('Week',10080))) {
        $pulseUrls[$pulseCurrency + $pulsePeriod[0]] = 'https://api.kraken.com/0/public/OHLC?pair=XBT' + $pulseCurrency.ToUpperInvariant() + '&interval=' + $pulsePeriod[1]
    }
}
foreach ($pulseName in $pulseUrls.Keys) {
    $pulseResponsePath=Join-Path $pulseData "$pulseName.json"
    if (-not $RecordedData -or -not (Test-Path -LiteralPath $pulseResponsePath)) { Invoke-WebRequest $pulseUrls[$pulseName] -OutFile $pulseResponsePath }
}
$pulseSourceDir = $pulseCache.Replace('\','/').Replace("'", "\'")
$pulseFixtureDir = (Join-Path $pulseProject 'tools/omalauncher-check').Replace('\','/').Replace("'", "\'")
$pulseInitPath = Join-Path $pulseCache 'native-check.init.gradle'
@"
gradle.projectsEvaluated {
    def app = rootProject.project(':app')
    app.android.sourceSets.test.java.srcDir('$pulseSourceDir')
    app.android.sourceSets.test.java.srcDir('$pulseFixtureDir')
}
"@ | Set-Content -LiteralPath $pulseInitPath -Encoding utf8
Push-Location $pulseProject
try {
    & $pulseGradle :app:testDebugUnitTest --tests 'com.buildapptest.bitcoinpulse.owlcheck.OwlRenderTest' -I $pulseInitPath --offline --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Native OWL render validation failed.' }
    # A fresh test JVM avoids Compose's retained frame scheduler between the
    # full-size interaction suite and the independent compact interaction suite.
    & $pulseGradle :app:testDebugUnitTest --tests 'com.buildapptest.bitcoinpulse.owlcheck.OwlResizeTest' -I $pulseInitPath --offline --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Resizable OWL render validation failed.' }
} finally { Pop-Location }
