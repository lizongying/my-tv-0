package com.lizongying.mytv0

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class MyApplication : Application() {

    companion object {
        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun toast(message: CharSequence = "", duration: Int = Toast.LENGTH_SHORT) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, duration).show()
        }
    }
}

fun String.showToast(duration: Int = Toast.LENGTH_SHORT) {
    MyApplication.getInstance().toast(this, duration)
}