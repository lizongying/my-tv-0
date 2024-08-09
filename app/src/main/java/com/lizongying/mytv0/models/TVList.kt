package com.lizongying.mytv0.models

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonSyntaxException
import com.lizongying.mytv0.ISP
import com.lizongying.mytv0.R
import com.lizongying.mytv0.SP
import com.lizongying.mytv0.requests.HttpClient
import com.lizongying.mytv0.showToast
import io.github.lizongying.Gua
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object TVList {
    private const val TAG = "TVList"
    const val FILE_NAME = "channels.txt"
    private lateinit var appDirectory: File
    private lateinit var serverUrl: String
    private lateinit var list: List<TV>
    var listModel: List<TVModel> = listOf()
    val groupModel = TVGroupModel()
    private var epg = SP.epg

    private var isp = ISP.UNKNOWN

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    fun setISP(isp: ISP) {
        this.isp = isp
    }

    fun init(context: Context) {
        _position.value = 0

        groupModel.addTVListModel(TVListModel("我的收藏", 0))
        groupModel.addTVListModel(TVListModel("全部頻道", 1))

        appDirectory = context.filesDir
        val file = File(appDirectory, FILE_NAME)
        val str = if (file.exists()) {
            Log.i(TAG, "read $file")
            file.readText()
        } else {
            Log.i(TAG, "read resource")
            context.resources.openRawResource(R.raw.channels).bufferedReader()
                .use { it.readText() }
        }

        try {
            str2List(str)
        } catch (e: Exception) {
            e.printStackTrace()
            file.deleteOnExit()
            R.string.channel_read_error.showToast()
        }

        if (SP.configAutoLoad) {
            SP.config?.let {
                if (it.startsWith("http")) {
                    update(it)
                }
            }
        } else if (!epg.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                updateEPG()
            }
        }
    }

    private suspend fun updateEPG() {
        try {
            val request = okhttp3.Request.Builder().url(epg!!).build()
            val response = HttpClient.okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val epg = EPGXmlParser().parse(response.body!!.byteStream())

                withContext(Dispatchers.Main) {
                    for (m in listModel) {
                        epg[m.tv.name]?.let { m.setEpg(it) }
                    }
                }
            } else {
                Log.e(TAG, "EPG ${response.code}")
                R.string.epg_status_err.showToast()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            R.string.epg_request_err.showToast()
        }
    }

    private fun update() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "request $serverUrl")
                val request = okhttp3.Request.Builder().url(serverUrl).build()
                val response = HttpClient.okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    val file = File(appDirectory, FILE_NAME)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val str = response.body!!.string()
                    withContext(Dispatchers.Main) {
                        if (str2List(str)) {
                            file.writeText(str)
                            SP.config = serverUrl
                            R.string.channel_import_success.showToast()

                            if (!epg.isNullOrEmpty()) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    updateEPG()
                                }
                            }
                        } else {
                            R.string.channel_import_error.showToast()
                        }
                    }
                } else {
                    Log.e(TAG, "Request status ${response.code}")
                    R.string.channel_status_error.showToast()
                }
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                Log.e("JSON Parse Error", e.toString())
                R.string.channel_format_error.showToast()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                Log.e("Null Pointer Error", e.toString())
                R.string.channel_read_error.showToast()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Request error $e")
                R.string.channel_request_error.showToast()
            }
        }
    }

    private fun update(serverUrl: String) {
        this.serverUrl = serverUrl
        update()
    }

    fun parseUri(uri: Uri) {
        if (uri.scheme == "file") {
            val file = uri.toFile()
            Log.i(TAG, "file $file")
            val str = if (file.exists()) {
                file.readText()
            } else {
                R.string.file_not_exist.showToast()
                return
            }

            try {
                if (str2List(str)) {
                    SP.config = uri.toString()
                    R.string.channel_import_success.showToast()
                } else {
                    R.string.channel_import_error.showToast()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                file.deleteOnExit()
                R.string.channel_read_error.showToast()
            }
        } else {
            update(uri.toString())
        }
    }

    fun str2List(str: String): Boolean {
        var string = str
        val g = Gua()
        if (g.verify(str)) {
            string = g.decode(str)
        }
        if (string.isBlank()) {
            return false
        }

        when (string[0]) {
            '[' -> {
                try {
                    val type = object : com.google.gson.reflect.TypeToken<List<TV>>() {}.type
                    list = com.google.gson.Gson().fromJson(string, type)
                    Log.i(TAG, "导入频道 ${list.size}")
                } catch (e: Exception) {
                    Log.i(TAG, "parse error $string")
                    Log.i(TAG, e.message, e)
                    return false
                }
            }

            '#' -> {
                val lines = string.lines()
                val nameRegex = Regex("""tvg-name="([^"]+)"""")
                val logRegex = Regex("""tvg-logo="([^"]+)"""")
                val epgRegex = Regex("""x-tvg-url="([^"]+)"""")
                val groupRegex = Regex("""group-title="([^"]+)"""")

                val l = mutableListOf<TV>()
                val tvMap = mutableMapOf<String, List<TV>>()
                for ((index, line) in lines.withIndex()) {
                    val trimmedLine = line.trim()
                    if (trimmedLine.startsWith("#EXTM3U")) {
                        epg = epgRegex.find(trimmedLine)?.groupValues?.get(1)?.trim()
                        SP.epg = epg
                    } else if (trimmedLine.startsWith("#EXTINF")) {
                        val info = trimmedLine.split(",")
                        val title = info.last().trim()
                        var name = nameRegex.find(info.first())?.groupValues?.get(1)?.trim()
                        name = name ?: title
                        var group = groupRegex.find(info.first())?.groupValues?.get(1)?.trim()
                        group = group ?: ""
                        val logo = logRegex.find(info.first())?.groupValues?.get(1)?.trim()
                        val uris =
                            if (index + 1 < lines.size) listOf(lines[index + 1].trim()) else emptyList()
                        val tv = TV(
                            -1,
                            name,
                            title,
                            "",
                            logo ?: "",
                            "",
                            uris,
                            mapOf(),
                            group,
                            SourceType.UNKNOWN,
                            listOf(),
                        )

                        if (!tvMap.containsKey(group + name)) {
                            tvMap[group + name] = listOf()
                        }
                        tvMap[group + name] = tvMap[group + name]!! + tv
                    }
                }
                for ((_, tv) in tvMap) {
                    val uris = tv.map { t -> t.uris }.flatten()
                    val t0 = tv[0]
                    val t1 = TV(
                        -1,
                        t0.name,
                        t0.name,
                        "",
                        t0.logo,
                        "",
                        uris,
                        mapOf(),
                        t0.group,
                        SourceType.UNKNOWN,
                        listOf(),
                    )
                    l.add(t1)
                }
                list = l
                Log.i(TAG, "导入频道 ${list.size}")
            }

            else -> {
                val lines = string.lines()
                var group = ""
                val l = mutableListOf<TV>()
                val tvMap = mutableMapOf<String, List<String>>()
                for (line in lines) {
                    val trimmedLine = line.trim()
                    if (trimmedLine.isNotEmpty()) {
                        if (trimmedLine.contains("#genre#")) {
                            group = trimmedLine.split(',', limit = 2)[0].trim()
                        } else {
                            val arr = trimmedLine.split(',').map { it.trim() }
                            val title = arr.first().trim()
                            val uris = arr.drop(1)

                            if (!tvMap.containsKey(group + title)) {
                                tvMap[group + title] = listOf()
                            }
                            tvMap[group + title] = tvMap[group + title]!! + uris
                        }
                    }
                }
                for ((title, uris) in tvMap) {
                    val tv = TV(
                        -1,
                        "",
                        title.removePrefix(group),
                        "",
                        "",
                        "",
                        uris,
                        mapOf(),
                        group,
                        SourceType.UNKNOWN,
                        listOf(),
                    )

                    l.add(tv)
                }
                list = l
                Log.i(TAG, "导入频道 ${list.size}")
            }
        }

        groupModel.clearNotFilter()

        val map: MutableMap<String, MutableList<TVModel>> = mutableMapOf()
        for (v in list) {
            if (v.group !in map) {
                map[v.group] = mutableListOf()
            }
            map[v.group]?.add(TVModel(v))
        }

        val listModelNew: MutableList<TVModel> = mutableListOf()
        var groupIndex = 2
        var id = 0
        for ((k, v) in map) {
            val tvListModel = TVListModel(k, groupIndex)
            for ((listIndex, v1) in v.withIndex()) {
                v1.tv.id = id
                v1.setLike(SP.getLike(id))
                v1.groupIndex = groupIndex
                v1.listIndex = listIndex
                tvListModel.addTVModel(v1)
                listModelNew.add(v1)
                id++
            }
            groupModel.addTVListModel(tvListModel)
            groupIndex++
        }

        listModel = listModelNew

        // 全部频道
        (groupModel.tvGroupModel.value as List<TVListModel>)[1].setTVListModel(listModel)

        groupModel.setChange()

        return true
    }

    fun getTVModel(): TVModel {
        return getTVModel(position.value!!)
    }

    private fun getTVModel(idx: Int): TVModel {
        return listModel[idx]
    }

    fun setPosition(position: Int): Boolean {
        if (position >= size()) {
            return false
        }

        if (_position.value != position) {
            _position.value = position
        }

        val tvModel = getTVModel(position)

        // set a new position or retry when position same
        tvModel.setReady()

        groupModel.setPosition(tvModel.groupIndex)

        SP.positionGroup = tvModel.groupIndex
        SP.position = position
        return true
    }

    fun size(): Int {
        return listModel.size
    }
}