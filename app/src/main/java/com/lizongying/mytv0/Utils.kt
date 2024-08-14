package com.lizongying.mytv0

import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import com.google.gson.Gson
import com.lizongying.mytv0.ISP.CHINA_MOBILE
import com.lizongying.mytv0.ISP.CHINA_TELECOM
import com.lizongying.mytv0.ISP.CHINA_UNICOM
import com.lizongying.mytv0.ISP.UNKNOWN
import com.lizongying.mytv0.models.TVList
import com.lizongying.mytv0.requests.TimeResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ISP {
    UNKNOWN,
    CHINA_MOBILE,
    CHINA_UNICOM,
    CHINA_TELECOM;

    fun fromName(name: String): ISP {
        val isp = when (name) {
            "ChinaMobile" -> CHINA_MOBILE
            "ChinaUnicom" -> CHINA_UNICOM
            "ChinaTelecom" -> CHINA_TELECOM
            else -> UNKNOWN
        }
        return isp
    }
}

data class IpInfo(
    val ip: String,
    val location: Location
)

data class Location(
    val city_name: String,
    val country_name: String,
    val isp_domain: String,
    val latitude: String,
    val longitude: String,
    val owner_domain: String,
    val region_name: String,
)


object Utils {
    private var between: Long = 0

    fun getDateFormat(format: String): String {
        return SimpleDateFormat(
            format,
            Locale.CHINA
        ).format(Date(System.currentTimeMillis() - between))
    }

    fun getDateTimestamp(): Long {
        return (System.currentTimeMillis() - between) / 1000
    }

    suspend fun init() {
        try {
            val currentTimeMillis = getTimestampFromServer()
            if (currentTimeMillis > 0) {
                between = System.currentTimeMillis() - currentTimeMillis
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//
//        try {
//            val isp = getISP()
//            TVList.setISP(isp)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            init()
        }
    }

    /**
     * 从服务器获取时间戳
     * @return Long 时间戳
     */
    private suspend fun getTimestampFromServer(): Long {
        return withContext(Dispatchers.IO) {
            val client = okhttp3.OkHttpClient.Builder().build()
            val request = okhttp3.Request.Builder()
                .url("https://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp")
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext 0
                    val string = response.body?.string()
                    Gson().fromJson(string, TimeResponse::class.java).data.t.toLong()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }

    suspend fun getISP(): ISP {
        return withContext(Dispatchers.IO) {
            val client = okhttp3.OkHttpClient.Builder().build()
            val request = okhttp3.Request.Builder()
                .url("https://api.myip.la/json")
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext UNKNOWN
                    val string = response.body?.string()
                    val isp = Gson().fromJson(string, IpInfo::class.java).location.isp_domain
                    when (isp) {
                        "ChinaMobile" -> CHINA_MOBILE
                        "ChinaUnicom" -> CHINA_UNICOM
                        "ChinaTelecom" -> CHINA_TELECOM
                        else -> UNKNOWN
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                UNKNOWN
            }
        }
    }

    fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics
        ).toInt()
    }

    fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics
        ).toInt()
    }

    fun isTmallDevice() = Build.MANUFACTURER.equals("Tmall", ignoreCase = true)

    fun formatUrl(url: String): String {
        // Check if the URL already starts with "http://" or "https://"
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
            return url
        }

        // Check if the URL starts with "//"
        if (url.startsWith("//")) {
            return "http://$url"
        }

        // Otherwise, add "http://" to the beginning of the URL
        return "http://${url}"
    }
}