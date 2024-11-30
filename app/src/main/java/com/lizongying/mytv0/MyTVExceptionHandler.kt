package com.lizongying.mytv0

import android.content.Context
import android.os.Build
import android.util.Log
import com.lizongying.mytv0.requests.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.system.exitProcess

class MyTVExceptionHandler(val context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        val crashInfo =
            "APP: ${context.appVersionName}, PRODUCT: ${Build.PRODUCT}, DEVICE: ${Build.DEVICE}, SUPPORTED_ABIS: ${Build.SUPPORTED_ABIS.joinToString()}, BOARD: ${Build.BOARD}, MANUFACTURER: ${Build.MANUFACTURER}, MODEL: ${Build.MODEL}, VERSION: ${Build.VERSION.SDK_INT}\nThread: ${t.name}\nException: ${e.message}\nStackTrace: ${
                Log.getStackTraceString(
                    e
                )
            }\n"

        runBlocking {
            launch {
                saveCrashInfoToFile(crashInfo)

                withContext(Dispatchers.Main) {
                    android.os.Process.killProcess(android.os.Process.myPid())
                    exitProcess(1)
                }
            }
        }
    }

    private suspend fun saveCrashInfoToFile(crashInfo: String) {
        if (isLimit()) {
            Log.e(TAG, crashInfo)
        } else {
            try {
                saveLog(crashInfo)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isLimit(): Boolean {
        if (context.appVersionName != SP.version) {
            SP.version = context.appVersionName
            SP.logTimes = SP.DEFAULT_LOG_TIMES
            return false
        } else {
            SP.logTimes--
            return SP.logTimes < 0
        }
    }

    private suspend fun saveLog(crashInfo: String) {
        withContext(Dispatchers.IO) {
            val request = okhttp3.Request.Builder()
                .url("https://lyrics.run/my-tv-0/v1/log")
                .method("POST", crashInfo.toRequestBody("text/plain".toMediaType()))
                .build()
            try {
                HttpClient.okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.i(TAG, "log success")
                    } else {
                        Log.e(TAG, "log failed: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "MyTVException"
    }
}