package com.lizongying.mytv0.models

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonSyntaxException
import com.lizongying.mytv0.R
import com.lizongying.mytv0.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object TVList {
    private const val TAG = "TVList"
    private const val FILE_NAME = "channels.json"
    private lateinit var appDirectory: File
    private lateinit var serverUrl: String
    private lateinit var list: List<TV>
    var listModel: List<TVModel> = listOf()
    val groupModel = TVGroupModel()

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    fun init(context: Context) {
        _position.value = 0
        appDirectory = context.filesDir
        serverUrl = context.resources.getString(R.string.server_url)
        val file = File(appDirectory, FILE_NAME)
        val str = if (file.exists()) {
            Log.i(TAG, "local file")
            file.readText()
        } else {
            Log.i(TAG, "read resource")
            context.resources.openRawResource(R.raw.channels).bufferedReader()
                .use { it.readText() }
        }
        Log.i("", "channel $str")

        groupModel.addTVListModel(TVListModel("我的收藏"))

        groupModel.addTVListModel(TVListModel("全部频道"))

        try {
            str2List(str)
        } catch (e: Exception) {
            Log.e("", "error $e")
            file.deleteOnExit()
            Toast.makeText(context, "读取频道失败，请在菜单中进行设置", Toast.LENGTH_LONG).show()
        }
    }

    private fun update() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i("", "do request $serverUrl")
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder().url(serverUrl).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val file = File(appDirectory, FILE_NAME)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val str = response.body()!!.string()
                    Log.i("", "request str $str")

                    file.writeText(str)
                    withContext(Dispatchers.Main) {
                        str2List(str)
                    }
                    "频道读取成功".showToast()
                } else {
                    Log.e("", "request status ${response.code()}")
                    "频道状态错误".showToast()
                }
            } catch (e: JsonSyntaxException) {
                Log.e("JSON Parse Error", e.toString())
                "频道格式错误".showToast()
            } catch (e: NullPointerException) {
                Log.e("Null Pointer Error", e.toString())
                "无法读取频道".showToast()
            } catch (e: Exception) {
                Log.e("", "request error $e")
                "频道请求错误".showToast()
            }
        }
    }

    fun update(serverUrl: String) {
        this.serverUrl = serverUrl
        Log.i("", "update $serverUrl")
        update()
    }

    private fun str2List(str: String) {
        val type = object : com.google.gson.reflect.TypeToken<List<TV>>() {}.type
        list = com.google.gson.Gson().fromJson(str, type)
        Log.i("TVList", "$list")

        listModel = list.map { tv ->
            TVModel(tv)
        }

        groupModel.clear()

        // 全部频道
        groupModel.getTVListModel(1)?.setTVListModel(listModel)

        val map: MutableMap<String, MutableList<TVModel>> = mutableMapOf()
        for ((id, v) in list.withIndex()) {
            if (v.group !in map) {
                map[v.group] = mutableListOf()
            }
            v.id = id
            map[v.group]?.add(TVModel(v))
        }

        for ((k, v) in map) {
            val tvListModel = TVListModel(k)
            for (v1 in v) {
                tvListModel.addTVModel(v1)
            }
            groupModel.addTVListModel(tvListModel)
        }
    }

    fun getTVModel(idx: Int): TVModel {
        return listModel[idx]
    }

    fun setPosition(position: Int): Boolean {
        if (position >= size()) {
            return false
        }

        if (_position.value != position) {
            _position.value = position
        }

        // set a new position or retry when position same
        listModel[position].setReady()
        return true
    }

    fun size(): Int {
        return listModel.size
    }
}