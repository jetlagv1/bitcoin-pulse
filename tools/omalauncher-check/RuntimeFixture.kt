package dev.omalauncher.widgets.runtime

import android.graphics.Picture
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import dev.omalauncher.widgets.lang.*

/** Deterministic HTTP/runtime host; no launcher services or network jobs run in this fixture. */
class WidgetInstance(val program: Program) : Host {
    private val stored = mutableStateMapOf<Int, Value>()
    private val dimensions = mutableStateMapOf<String, Value>()
    private val derived = mutableMapOf<Int, State<Value>>()
    val root = Env.root(this)
    override val budget = Budget()
    val updates = mutableIntStateOf(0)
    val problems = mutableListOf<String>()
    val actions = mutableListOf<String>()
    var time = 0.0; var timeRead = false; val slow = false
    override fun time(): Double { timeRead = true; return time }
    override fun theme(name: String) = 0xff00ff00.toInt()
    override fun read(slot: Int): Value = when (val g = program.globals[slot]) {
        is Global.Let -> derived.getOrPut(slot) { derivedStateOf { g.value(root) } }.value
        is Global.State -> stored[slot] ?: g.init(root)
        else -> stored[slot] ?: Null
    }
    override fun write(slot: Int, value: Value) { stored[slot] = value }
    fun set(name: String, value: Value) { write(program.globals.indexOfFirst { it.name == name }.also { check(it >= 0) }, value) }
    fun get(name: String) = read(program.globals.indexOfFirst { it.name == name })
    override fun tile(name: String) = dimensions[name] ?: Null
    fun setTile(cols: Int?, rows: Int?, width: Double?, height: Double?) {
        listOf("cols" to cols?.toDouble(), "rows" to rows?.toDouble(), "tileWidth" to width, "tileHeight" to height).forEach { (k,v) -> if (v != null) dimensions[k] = Num(v) }
    }
    fun setScreen(width: Double, height: Double) { dimensions["screenWidth"] = Num(width); dimensions["screenHeight"] = Num(height) }
    override fun act(name: String, args: List<Value>) { actions += name }
    override fun report(message: String) { problems += message }
    fun eval(ev: Ev, env: Env, where: String): Value = budget.run { ev(env) }
    fun recordDraw(nanos: Long) {}
    suspend fun awaitTimeRead(start: Long) {}
    fun image(src: String): WidgetImage? = null
    fun sprite(rows: Value) = Sprite.parse(rows)
    fun terminal(el: WNode.Element, command: String?, fontSize: Double?): Any = error("Unexpected terminal")
}
sealed interface WidgetImage {
    class Raster(val bitmap: ImageBitmap) : WidgetImage
    class Vector(val picture: Picture) : WidgetImage
}
class PicturePainter(val picture: Picture) : Painter() {
    override val intrinsicSize = Size.Unspecified
    override fun DrawScope.onDraw() { error("Unexpected picture") }
}
fun drawPicture(canvas: android.graphics.Canvas, picture: Picture, x: Float, y: Float, w: Float, h: Float, tint: android.graphics.ColorFilter?) { error("Unexpected picture") }
