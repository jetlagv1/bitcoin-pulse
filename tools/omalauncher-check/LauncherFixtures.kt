package dev.omalauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// Test services only. Render.kt, Props.kt and Canvas.kt are upstream files.
val Mono = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
val NerdSymbols = FontFamily.Monospace
object LauncherStyle { val font = FontFamily.Monospace; const val cornerScale = 1f }
object Palette {
    val foreground = Color(0xff00ff00); val accent = Color(0xff00ff00)
    val muted = Color(0xff444444); val border = Color(0xff00ff00); val surface = Color(0xffeeeeee)
}
class TerminalTiles
@Composable fun TerminalView(terminal: Any, modifier: Modifier) { error("This widget must not use a terminal") }
