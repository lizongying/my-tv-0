package com.lizongying.mytv0

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast

class MyTvApplication : Application() {

    companion object {
        private const val TAG = "MyTvApplication"
        private lateinit var instance: MyTvApplication

        fun getInstance(): MyTvApplication {
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


        Log.i(TAG, "width $width height $height")

        if ((width.toDouble() / height) < (16.0 / 9.0)) {
            ratio = width * 2 / 1920.0 / density
            shouldWidth = width
            shouldHeight = (width * 9.0 / 16.0).toInt()
        } else {
            ratio = height * 2 / 1080.0 / density
            Log.i(TAG, "ratio $ratio ｜ $density")
            shouldHeight = height
            shouldWidth = (height * 16.0 / 9.0).toInt()
        }

        Log.i(TAG, "shouldWidth $shouldWidth shouldHeight $shouldHeight")
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
}

fun String.showToast(duration: Int = Toast.LENGTH_SHORT) {
    MyTvApplication.getInstance().toast(this, duration)
}