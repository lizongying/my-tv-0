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
    private val gson = Gson()

    private val _removed = MutableLiveData<Pair<Int, Int>>()
    val removed: LiveData<Pair<Int, Int>>
        get() = _removed

    private val _added = MutableLiveData<Pair<Int, Int>>()
    val added: LiveData<Pair<Int, Int>>
        get() = _added

    private val _changed = MutableLiveData<Int>()
    val changed: LiveData<Int>
        get() = _changed

    private val _sources = MutableLiveData<List<Source>>()
    val sources: LiveData<List<Source>>
        get() = _sources
    private val sourcesValue: List<Source>
        get() = _sources.value ?: listOf()

    private val _checked = MutableLiveData<Int>()
    val checked: LiveData<Int>
        get() = _checked
    val checkedValue: Int
        get() = _checked.value ?: DEFAULT_CHECKED

    fun setChecked(position: Int) {
        _checked.value = position
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

    private fun setSources(sources: List<Source>) {
        _sources.value = sources
        SP.sources = gson.toJson(sources, type) ?: ""
    }

    fun addSource(source: Source) {
        if (_sources.value == null) {
            _sources.value = mutableListOf(source)
        }

        val index = sourcesValue.indexOfFirst { it.uri == source.uri }
        if (index == -1) {
            _sources.value = sourcesValue.toMutableList().apply {
                add(0, source)
            }
            SP.sources = gson.toJson(sourcesValue, type) ?: ""

            _added.value = Pair(sourcesValue.size - 1, version)
            version++
        }
    }

    fun removeSource(id: String) {
        if (sourcesValue.isEmpty()) {
            return
        }

        val index = sourcesValue.indexOfFirst { it.id == id }
        if (index != -1) {
            _sources.value = sourcesValue.toMutableList().apply {
                removeAt(index)
            }
            SP.sources = gson.toJson(sourcesValue, type) ?: ""

            _removed.value = Pair(index, version)
            version++
        }
    }

    fun getSource(idx: Int): Source? {
        if (idx >= size()) {
            return null
        }

        if (sourcesValue.isEmpty()) {
            return null
        }

        return sourcesValue[idx]
    }

    fun init() {
        if (!SP.sources.isNullOrEmpty()) {
            try {
                val sources: List<Source> = gson.fromJson(SP.sources!!, type)
                setSources(sources)
            } catch (e: Exception) {
                e.printStackTrace()
                SP.sources = SP.DEFAULT_SOURCES
            }
        }

        if (size() > 0) {
            _checked.value = sourcesValue.indexOfFirst { it.uri == SP.configUrl }

            if (checkedValue > -1) {
                setSourceChecked(checkedValue, true)
            }
        }

        _changed.value = version
        version++
    }

    init {
        init()
    }

    fun size(): Int {
        return sourcesValue.size
    }

    companion object {
        const val TAG = "Sources"
        const val DEFAULT_CHECKED = -1
    }
}