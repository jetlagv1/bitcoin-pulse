package com.buildapptest.bitcoinpulse

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
    @Test fun dashboardCurrencyAndWidgetSetup() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        ActivityScenario.launch(MainActivity::class.java).use {
            assertTrue("Dashboard should open", device.wait(Until.hasObject(By.text("Bitcoin Pulse")), 30_000))
            device.wait(Until.findObject(By.text("USD")), 10_000).click()
            UiScrollable(UiSelector().scrollable(true)).scrollIntoView(UiSelector().text("1 dag"))
            device.findObject(By.text("1 dag")).click()
            assertTrue(device.wait(Until.hasObject(By.textContains("1 dag per candle")),10_000))
            UiScrollable(UiSelector().scrollable(true)).scrollToBeginning(10)
            device.wait(Until.findObject(By.text("Widgets")), 10_000).click()
            assertTrue("Currency preference must reach widget previews", device.wait(Until.hasObject(By.text("Bitcoin · USD")), 10_000))
            assertTrue("Expanded widget pin action should exist", UiScrollable(UiSelector().scrollable(true)).scrollIntoView(UiSelector().text("Add network widget")))
            val evidence = File(instrumentation.targetContext.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
            device.takeScreenshot(File(evidence, "app-widgets.png"))
            UiScrollable(UiSelector().scrollable(true)).scrollToBeginning(10)
            device.findObject(By.text("Dashboard")).click()
            assertTrue(UiScrollable(UiSelector().scrollable(true)).scrollIntoView(UiSelector().text("Network fees")))
            device.takeScreenshot(File(evidence, "app-dashboard.png"))
        }
    }

    @Test fun nativeWidgetLayoutsFitTheirMinimumSizes() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.runOnMainSync {
            val density = context.resources.displayMetrics.density
            listOf(Triple(R.layout.widget_expanded, 280, 360), Triple(R.layout.widget_compact, 160, 160)).forEach { (layout, widthDp, heightDp) ->
                val compact = layout == R.layout.widget_compact
                val remote = RemoteViews(context.packageName, layout)
                remote.setTextViewText(R.id.widget_price, if (compact) "$104.2k" else "$104,230")
                remote.setTextViewText(R.id.widget_next, if (compact) "Next 3 sat/vB" else "3")
                remote.setTextViewText(R.id.widget_updated, "Oldest update 14:32")
                val parent = FrameLayout(context)
                val view = remote.apply(context, parent)
                parent.addView(view)
                val width = (widthDp * density).toInt()
                val height = (heightDp * density).toInt()
                parent.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
                parent.layout(0, 0, width, height)
                val refresh = view.findViewById<View>(R.id.widget_refresh)
                val bounds = Rect()
                refresh.getDrawingRect(bounds)
                parent.offsetDescendantRectToMyCoords(refresh, bounds)
                assertTrue("Refresh control must fit inside $widthDp x $heightDp widget: $bounds", bounds.bottom <= height && bounds.right <= width && bounds.top >= 0)
                assertTrue("Refresh target should be at least 48dp", refresh.measuredWidth >= (48 * density).toInt())
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                parent.draw(Canvas(bitmap))
                val folder = File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
                File(folder, if (compact) "native-widget-compact.png" else "native-widget-network.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        }
    }
}
