package com.lizongying.mytv0.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lizongying.mytv0.SP
import com.lizongying.mytv0.data.Source

class Sources {
    private val type = object : TypeToken<List<Source>>() {}.type
    var version = 0
    private val _removed = MutableLiveData<Pair<Int, Int>>()
    val removed: LiveData<Pair<Int, Int>>
        get() = _removed

    private val _added = MutableLiveData<Pair<Int, Int>>()
    val added: LiveData<Pair<Int, Int>>
        get() = _added

    private val _sources = MutableLiveData<List<Source>>()
    val sources: LiveData<List<Source>>
        get() = _sources
    private val sourcesValue: List<Source>
        get() = _sources.value ?: listOf()

    private val _focused = MutableLiveData<Int>()
    val focused: LiveData<Int>
        get() = _focused
    val focusedValue: Int
        get() = _focused.value ?: DEFAULT_FOCUSED

    fun setFocused(position: Int) {
        _focused.value = position
    }

    private val _checked = MutableLiveData<Int>()
    val checked: LiveData<Int>
        get() = _checked
    val checkedValue: Int
        get() = _checked.value ?: DEFAULT_CHECKED

    fun setChecked(position: Int) {
        _checked.value = position
        SP.config = getSource(position)!!.uri
    }

    fun setChecked() {
        setChecked(focusedValue)
    }

    fun setSourceChecked(position: Int, checked: Boolean): Boolean {
        val checkedBefore = getSource(position)?.checked
        if (checkedBefore == checked) {
            return false
        } else {
            getSource(position)?.checked = checked
//            if (checked) {
//                Log.i(TAG, "setChecked $position")
//                setChecked(position)
//            }
            return true
        }
    }

    private val _change = MutableLiveData<Boolean>()
    val change: LiveData<Boolean>
        get() = _change

    fun setChange() {
        _change.value = true
    }

    private fun setSources(sources: List<Source>) {
        _sources.value = sources
        SP.sources = Gson().toJson(sources, type) ?: ""
    }

    fun addSource(source: Source) {
        if (_sources.value == null) {
            _sources.value = mutableListOf(source)
            return
        }

        val index = sourcesValue.indexOfFirst { it.uri == source.uri }
        if (index == -1) {
            val newList = sourcesValue.toMutableList().apply {
                add(source)
            }
            _sources.value = newList
            SP.sources = Gson().toJson(sources.value, type) ?: ""

            _added.value = Pair(newList.size, version)
            version++
        }
    }

    fun removeSource(id: String) {
        if (_sources.value == null) {
            return
        }

        val index = sourcesValue.indexOfFirst { it.id == id }
        if (index != -1) {
            val newList = sourcesValue.toMutableList().apply {
                removeAt(index)
            }
            _sources.value = newList
            SP.sources = Gson().toJson(sources.value, type) ?: ""

            _removed.value = Pair(index, version)
            version++
        }
    }

    fun replaceSource(source: Source) {
        if (_sources.value == null) {
            _sources.value = mutableListOf(source)
            return
        }

        val newList = sourcesValue.toMutableList()
        var exists = false
        val iterator = newList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().id == source.id) {
                exists = true
            }
        }
        if (!exists) {
            newList.add(source)
            _sources.value = newList
        }
    }

    fun getSource(): Source? {
        return getSource(focusedValue)
    }

    fun getSource(idx: Int): Source? {
        if (idx >= size()) {
            return null
        }

        setFocused(idx)
        return sourcesValue[idx]
    }

    fun getCurrent(): Source? {
        if (focusedValue < 0 || focusedValue >= size()) {
            return getSource(0)
        }

        return getSource(focusedValue)
    }

    fun getPrev(): Source? {
        if (size() == 0) {
            return null
        }

        val p = (size() + focusedValue - 1) % size()
        setFocused(p)
        return sourcesValue[p]
    }

    fun getNext(): Source? {
        if (size() == 0) {
            return null
        }

        val p = (focusedValue + 1) % size()
        setFocused(p)
        return sourcesValue[p]
    }

    fun clearData() {
        setSources(listOf())
        setFocused(DEFAULT_FOCUSED)
        setChecked(DEFAULT_CHECKED)
    }

    init {
        SP.sources?.let {
            if (it.isEmpty()) {
                Log.i(TAG, "sources is empty")
                return@let
            }

            try {
                val sources: List<Source> = Gson().fromJson(it, type)
                setSources(sources)
            } catch (e: Exception) {
                e.printStackTrace()
                SP.sources = ""
            }
        }

        if (size() == 0) {
            if (!SP.config.isNullOrEmpty()) {
                addSource(
                    Source(
                        uri = SP.config!!,
                    )
                )
            }
        }

        if (size() > -1) {
            listOf(
                "https://live.fanmingming.com/tv/m3u/ipv6.m3u",
                "https://live.fanmingming.com/tv/m3u/itv.m3u",
                "https://live.fanmingming.com/tv/m3u/index.m3u",

                "https://iptv-org.github.io/iptv/index.m3u",

                // https://github.com/Guovin/iptv-api
                "https://ghp.ci/raw.githubusercontent.com/Guovin/iptv-api/gd/output/result.m3u",
                "https://ghp.ci/raw.githubusercontent.com/Guovin/iptv-api/gd/output/result.txt",

                // https://github.com/joevess/IPTV
                "https://mirror.ghproxy.com/raw.githubusercontent.com/joevess/IPTV/main/sources/iptv_sources.m3u",
                "https://mirror.ghproxy.com/raw.githubusercontent.com/joevess/IPTV/main/sources/home_sources.m3u",
                "https://mirror.ghproxy.com/raw.githubusercontent.com/joevess/IPTV/main/iptv.m3u",
                "https://mirror.ghproxy.com/raw.githubusercontent.com/joevess/IPTV/main/home.m3u",

                // https://github.com/zbefine/iptv
                "https://cdn.jsdelivr.net/gh/zbefine/iptv/iptv.m3u",
                "https://cdn.jsdelivr.net/gh/zbefine/iptv/iptv.txt",
            ).forEach {
                addSource(
                    Source(
                        uri = it
                    ),
                )
            }
        }

        if (size() > 0) {
            _checked.value = sourcesValue.indexOfFirst { it.uri == SP.config }

            if (_checked.value != null && _checked.value!! > -1) {
                setSourceChecked(_checked.value!!, true)
            }

            _focused.value = _checked.value
        }
    }

    fun size(): Int {
        if (_sources.value == null) {
            return 0
        }

        return sourcesValue.size
    }

    companion object {
        const val TAG = "Sources"
        const val DEFAULT_FOCUSED = 0
        const val DEFAULT_CHECKED = -1
    }
}