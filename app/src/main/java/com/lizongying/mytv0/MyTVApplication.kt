package com.lizongying.mytv0

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import java.util.Locale

class MyTVApplication : Application() {

    companion object {
        private const val TAG = "MyTVApplication"
        private lateinit var instance: MyTVApplication

        @JvmStatic
        fun getInstance(): MyTVApplication {
            return instance
        }
    }

    private lateinit var displayMetrics: DisplayMetrics
    private lateinit var realDisplayMetrics: DisplayMetrics

    private var width = 0
    private var height = 0
    private var shouldWidth = 0
    private var shouldHeight = 0
    private var ratio = 1.0
    private var density = 2.0f
    private var scale = 1.0f

    override fun onCreate() {
        super.onCreate()
        instance = this

        displayMetrics = DisplayMetrics()
        realDisplayMetrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        windowManager.defaultDisplay.getRealMetrics(realDisplayMetrics)

        if (realDisplayMetrics.heightPixels > realDisplayMetrics.widthPixels) {
            width = realDisplayMetrics.heightPixels
            height = realDisplayMetrics.widthPixels
        } else {
            width = realDisplayMetrics.widthPixels
            height = realDisplayMetrics.heightPixels
        }

        density = Resources.getSystem().displayMetrics.density
        scale = displayMetrics.scaledDensity

        if ((width.toDouble() / height) < (16.0 / 9.0)) {
            ratio = width * 2 / 1920.0 / density
            shouldWidth = width
            shouldHeight = (width * 9.0 / 16.0).toInt()
        } else {
            ratio = height * 2 / 1080.0 / density
            shouldHeight = height
            shouldWidth = (height * 16.0 / 9.0).toInt()
        }

        Thread.setDefaultUncaughtExceptionHandler(MyTVExceptionHandler(this))
    }

    fun getDisplayMetrics(): DisplayMetrics {
        return displayMetrics
    }

    fun toast(message: CharSequence = "", duration: Int = Toast.LENGTH_SHORT) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, duration).show()
        }
    }

    fun shouldWidthPx(): Int {
        return shouldWidth
    }

    fun shouldHeightPx(): Int {
        return shouldHeight
    }

    fun dp2Px(dp: Int): Int {
        return (dp * ratio * density + 0.5f).toInt()
    }

    fun px2Px(px: Int): Int {
        return (px * ratio + 0.5f).toInt()
    }

    fun px2PxFont(px: Float): Float {
        return (px * ratio / scale).toFloat()
    }

    fun sp2Px(sp: Float): Float {
        return (sp * ratio * scale).toFloat()
    }

    override fun attachBaseContext(base: Context) {
        try {
            val locale = Locale.TRADITIONAL_CHINESE
            val config = Configuration()
            config.setLocale(locale)
            super.attachBaseContext(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    base.createConfigurationContext(config)
                } else {
                    val resources = base.resources
                    resources.updateConfiguration(config, resources.displayMetrics)
                    base
                }
            )
        } catch (_: Exception) {
            super.attachBaseContext(base)
        }
    }
}