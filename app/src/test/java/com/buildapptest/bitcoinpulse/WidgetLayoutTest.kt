package com.buildapptest.bitcoinpulse

import android.app.Application
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetLayoutTest {
    @Test fun expandedLayoutInflatesAndKeepsRefreshVisible() { verify(R.layout.widget_expanded, 280, 620) }
    @Test fun expandedComboLayoutKeepsRefreshVisible() { verify(R.layout.widget_expanded, 320, 840) }
    @Test fun compactLayoutInflatesAndKeepsRefreshVisible() { verify(R.layout.widget_compact, 160, 160) }

    private fun verify(layout: Int, widthDp: Int, heightDp: Int) {
        val context = RuntimeEnvironment.getApplication()
        val density = context.resources.displayMetrics.density
        val remote = RemoteViews(context.packageName, layout)
        remote.setTextViewText(R.id.widget_price, if (layout == R.layout.widget_compact) "$104.2k" else "$104,230")
        remote.setTextViewText(R.id.widget_updated, "Oldest update 14:32")
        val parent = FrameLayout(context)
        val root = remote.apply(context, parent)
        parent.addView(root)
        val width = (widthDp * density).toInt()
        val height = (heightDp * density).toInt()
        parent.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        parent.layout(0, 0, width, height)
        val refresh = root.findViewById<View>(R.id.widget_refresh)
        val bounds = Rect()
        refresh.getDrawingRect(bounds)
        parent.offsetDescendantRectToMyCoords(refresh, bounds)
        val sizes = (0 until (root as android.view.ViewGroup).childCount).joinToString { index ->
            root.getChildAt(index).let { "${it.javaClass.simpleName} ${it.top}..${it.bottom}" }
        }
        assertTrue("Refresh must not clip at $widthDp x $heightDp: $bounds; root=${root.height}; children=$sizes", bounds.bottom <= height && bounds.top >= 0 && bounds.right <= width)
        assertTrue("Refresh must retain a 48dp touch target", refresh.measuredHeight >= (48 * density).toInt())
    }
}
