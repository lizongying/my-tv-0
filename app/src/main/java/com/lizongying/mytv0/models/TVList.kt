package com.lizongying.mytv0.models

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lizongying.mytv0.R
import java.io.File

object TVList {
    private const val FILE_NAME = "channels.json"
    private lateinit var appDirectory: File
    private lateinit var serverUrl: String
    private lateinit var list: List<TV>
    lateinit var listModel: List<TVModel>
    lateinit var categoryModel: TVCategoryModel

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    fun init(context: Context) {
        _position.value = 0
        appDirectory = context.filesDir
        serverUrl = context.resources.getString(R.string.server_url)
        val file = File(appDirectory, FILE_NAME)
        val str = if (file.exists()) {
            file.readText()
        } else {
            context.resources.openRawResource(R.raw.channels).bufferedReader()
                .use { it.readText() }
        }
        str2List(str)
    }

    fun update() {
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder().url(serverUrl).build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val file = File(appDirectory, FILE_NAME)
                if (!file.exists()) {
                    file.createNewFile()
                }
                val str = response.body()!!.string()
                file.writeText(str)
                str2List(str)
            }
        }
    }

    private fun str2List(str: String) {
        val type = object : com.google.gson.reflect.TypeToken<List<TV>>() {}.type
        list = com.google.gson.Gson().fromJson(str, type)
        Log.i("TVList", "$list")

        listModel = list.map { tv ->
            TVModel(tv)
        }

        val category: MutableList<TVListModel> = mutableListOf()

        var tvListModel = TVListModel("我的收藏")
        category.add(tvListModel)

        tvListModel = TVListModel("全部频道")
        tvListModel.setTVListModel(listModel)
        category.add(tvListModel)

        val map: MutableMap<String, MutableList<TVModel>> = mutableMapOf()
        for ((id, v) in list.withIndex()) {
            if (v.category !in map) {
                map[v.category] = mutableListOf()
            }
            v.id = id
            map[v.category]?.add(TVModel(v))
        }

        for ((k, v) in map) {
            tvListModel = TVListModel(k)
            for (v1 in v) {
                tvListModel.addTVModel(v1)
            }
            category.add(tvListModel)
        }

        categoryModel = TVCategoryModel()
        for (v in category) {
            categoryModel.addTVListModel(v)
        }
    }

    fun getTVModel(idx: Int): TVModel {
        return listModel[idx]
    }

    fun setPosition(position: Int) {
        if (_position.value != position) {
            _position.value = position
        }

        // set a new position or retry when position same
        listModel[position].setReady()
    }

    fun size(): Int {
        return listModel.size
    }
}