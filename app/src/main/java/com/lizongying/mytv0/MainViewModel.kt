import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.lizongying.mytv0.R
import com.lizongying.mytv0.SP
import com.lizongying.mytv0.Utils.getDateFormat
import com.lizongying.mytv0.data.SourceType
import com.lizongying.mytv0.data.TV
import com.lizongying.mytv0.models.EPGXmlParser
import com.lizongying.mytv0.models.TVGroupModel
import com.lizongying.mytv0.models.TVListModel
import com.lizongying.mytv0.models.TVModel
import com.lizongying.mytv0.requests.HttpClient
import com.lizongying.mytv0.showToast
import io.github.lizongying.Gua
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MainViewModel : ViewModel() {
    private var timeFormat = if (SP.displaySeconds) "HH:mm:ss" else "HH:mm"

    private lateinit var appDirectory: File
    var listModel: List<TVModel> = listOf()
    val groupModel = TVGroupModel()
    private var cacheFile: File? = null
    private var cacheConfig = ""
    private var initialized = false

    private val _channelsOk = MutableLiveData<Boolean>()
    val channelsOk: LiveData<Boolean>
        get() = _channelsOk

    fun setDisplaySeconds(displaySeconds: Boolean) {
        timeFormat = if (displaySeconds) "HH:mm:ss" else "HH:mm"
        SP.displaySeconds = displaySeconds
    }

    fun getTime(): String {
        return getDateFormat(timeFormat)
    }

    fun updateEPG() {
        if (!SP.epg.isNullOrEmpty()) {
            viewModelScope.launch {
                updateEPG(SP.epg!!)
            }
        }
    }

    fun updateConfig() {
        if (SP.configAutoLoad) {
            SP.config?.let {
                if (it.startsWith("http")) {
                    viewModelScope.launch {
                        Log.i(TAG, "updateConfig $it")
                        update(it)
                        SP.epg?.let { i ->
                            updateEPG(i)
                        }
                    }
                }
            }
        }
    }

    private fun getCache(): String {
        return if (cacheFile!!.exists()) {
            cacheFile!!.readText()
        } else {
            ""
        }
    }

    fun init(context: Context) {
        groupModel.addTVListModel(TVListModel("我的收藏", 0))
        groupModel.addTVListModel(TVListModel("全部頻道", 1))

        appDirectory = context.filesDir
        cacheFile = File(appDirectory, FILE_NAME)
        if (!cacheFile!!.exists()) {
            cacheFile!!.createNewFile()
        }

        cacheConfig = getCache()
        Log.i(TAG, "cacheConfig $cacheConfig")

        if (cacheConfig.isEmpty()) {
            cacheConfig = context.resources.openRawResource(R.raw.channels).bufferedReader()
                .use { it.readText() }
            Log.i(TAG, "cacheConfig $cacheConfig")
        }

        try {
            str2List(cacheConfig)
        } catch (e: Exception) {
            e.printStackTrace()
            cacheFile!!.deleteOnExit()
            R.string.channel_read_error.showToast()
        }

        initialized = true

        _channelsOk.value = true
    }

    private suspend fun updateEPG(epg: String) {
        try {
            withContext(Dispatchers.IO) {
                val request = okhttp3.Request.Builder().url(epg).build()
                val response = HttpClient.okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    val res = EPGXmlParser().parse(response.body!!.byteStream())

                    withContext(Dispatchers.Main) {
                        for (m in listModel) {
                            res[m.tv.name]?.let { m.setEpg(it) }
                        }
                    }
                } else {
                    Log.e(TAG, "EPG ${response.code}")
                    R.string.epg_status_err.showToast()
                }
            }
        } catch (e: Exception) {
            Log.i(TAG, "EPG request error:", e)
//            R.string.epg_request_err.showToast()
        }
    }

    suspend fun update(serverUrl: String) {
        Log.i(TAG, "request $serverUrl")
        try {
            withContext(Dispatchers.IO) {
                val request = okhttp3.Request.Builder().url(serverUrl).build()
                val response = HttpClient.okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    val str = response.body?.string() ?: ""
                    withContext(Dispatchers.Main) {
                        tryStr2List(str, null, serverUrl)
                    }
                } else {
                    Log.e(TAG, "Request status ${response.code}")
                    R.string.channel_status_error.showToast()
                }
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

    fun reset(context: Context) {
        val str = context.resources.openRawResource(R.raw.channels).bufferedReader()
            .use { it.readText() }

        try {
            str2List(str)
        } catch (e: Exception) {
            e.printStackTrace()
            R.string.channel_read_error.showToast()
        }
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

            tryStr2List(str, file, uri.toString())
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                update(uri.toString())
            }
        }
    }

    fun tryStr2List(str: String, file: File?, url: String) {
        try {
            if (str2List(str)) {
                cacheFile!!.writeText(str)
                cacheConfig = str
                SP.config = url
                _channelsOk.value = true
                R.string.channel_import_success.showToast()
            } else {
                R.string.channel_import_error.showToast()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            file?.deleteOnExit()
            R.string.channel_read_error.showToast()
        }
    }

    private fun str2List(str: String): Boolean {
        var string = str
        if (initialized && string == cacheConfig) {
            return false
        }
        val g = Gua()
        if (g.verify(str)) {
            string = g.decode(str)
        }
        if (string.isEmpty()) {
            return false
        }
        if (initialized && string == cacheConfig) {
            return false
        }

        val list: List<TV>

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
                        SP.epg = epgRegex.find(trimmedLine)?.groupValues?.get(1)?.trim()
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
                                tvMap[group + title] = tvMap[group + title]!! + group
                            }
                            tvMap[group + title] = tvMap[group + title]!! + uris
                        }
                    }
                }
                for ((title, uris) in tvMap) {
                    val channelGroup = uris.first();
                    uris.drop(1);
                    val tv = TV(
                        -1,
                        "",
                        title.removePrefix(channelGroup),
                        "",
                        "",
                        "",
                        uris,
                        mapOf(),
                        channelGroup,
                        SourceType.UNKNOWN,
                        listOf(),
                    )

                    l.add(tv)
                }
                list = l
                Log.i(TAG, "导入频道 ${list.size}")
            }
        }

        groupModel.initTVGroup()

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
            val listTVModel = TVListModel(k, groupIndex)
            for ((listIndex, v1) in v.withIndex()) {
                v1.tv.id = id
                v1.setLike(SP.getLike(id))
                v1.setGroupIndex(groupIndex)
                v1.listIndex = listIndex
                listTVModel.addTVModel(v1)
                listModelNew.add(v1)
                id++
            }
            groupModel.addTVListModel(listTVModel)
            groupIndex++
        }

        listModel = listModelNew

        // 全部频道
        (groupModel.tvGroup.value as List<TVListModel>)[1].setTVListModel(listModel)

        groupModel.setChange()

        return true
    }

    companion object {
        private const val TAG = "MainViewModel"
        const val FILE_NAME = "channels.txt"
    }
}