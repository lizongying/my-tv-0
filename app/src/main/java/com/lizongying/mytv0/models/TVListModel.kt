package com.lizongying.mytv0.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lizongying.mytv0.SP

class TVListModel(private val name: String, private val groupIndex: Int) : ViewModel() {
    var version = 0

    private val _removed = MutableLiveData<Pair<Int, Int>>()
    val removed: LiveData<Pair<Int, Int>>
        get() = _removed

    private val _added = MutableLiveData<Pair<Int, Int>>()
    val added: LiveData<Pair<Int, Int>>
        get() = _added

    private val _changed = MutableLiveData<Pair<Int, Int>>()
    val changed: LiveData<Pair<Int, Int>>
        get() = _changed

    fun getName(): String {
        return name
    }

    // position in tvGroup. No filters
    fun getGroupIndex(): Int {
        return groupIndex
    }

    private val _tvList = MutableLiveData<List<TVModel>>()
    val tvList: LiveData<List<TVModel>>
        get() = _tvList
    private val tvListValue: List<TVModel>
        get() = _tvList.value ?: listOf()

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position
    val positionValue: Int
        get() = _position.value ?: 0

    fun setPosition(position: Int) {
        _position.value = position
    }

    private val _positionPlaying = MutableLiveData<Int>()
    val positionPlaying: LiveData<Int>
        get() = _positionPlaying
    val positionPlayingValue: Int
        get() = _positionPlaying.value ?: 0

    fun setPositionPlaying(position: Int) {
        _positionPlaying.value = position
        SP.position = position
    }

    fun setPositionPlaying() {
        setPositionPlaying(positionValue)
    }

    private val _change = MutableLiveData<Boolean>()
    val change: LiveData<Boolean>
        get() = _change

    fun setChange() {
        _change.value = true
    }

    fun setTVListModel(tvList: List<TVModel>) {
        _tvList.value = tvList
    }

    fun addTVModel(tvModel: TVModel) {
        _tvList.value = tvListValue.toMutableList().apply {
            add(tvModel)
        }

        _added.value = Pair(tvListValue.size - 1, version)
        version++
    }

    fun removeTVModel(id: Int) {
        if (tvListValue.isEmpty()) {
            return
        }

        val index = tvListValue.indexOfFirst { it.tv.id == id }
        if (index != -1) {
            _tvList.value = tvListValue.toMutableList().apply {
                removeAt(index)
            }

            _removed.value = Pair(index, version)
            version++
        }
    }

    fun replaceTVModel(tvModel: TVModel) {
        if (_tvList.value == null) {
            _tvList.value = mutableListOf(tvModel)
        }

        val index = tvListValue.indexOfFirst { it.tv.id == tvModel.tv.id }
        if (index == -1) {
            _tvList.value = tvListValue.toMutableList().apply {
                add(tvModel)
            }

            _added.value = Pair(tvListValue.size - 1, version)
            version++
        }
    }

    fun getTVModel(): TVModel? {
        return getTVModel(positionValue)
    }

    fun getTVModel(idx: Int): TVModel? {
        if (idx < 0 || idx >= size()) {
            return null
        }

        setPosition(idx)
        return tvListValue[idx]
    }

    fun getCurrent(): TVModel? {
        if (positionValue < 0 || positionValue >= size()) {
            return getTVModel(0)
        }

        return getTVModel(positionValue)
    }

    fun getPrev(): TVModel? {
        if (size() == 0) {
            return null
        }

        val p = (size() + positionPlayingValue - 1) % size()
        setPositionPlaying(p)
        setPosition(p)
        return tvListValue[p]
    }

    fun getNext(): TVModel? {
        if (size() == 0) {
            return null
        }

        val p = (positionPlayingValue + 1) % size()
        setPositionPlaying(p)
        setPosition(p)
        return tvListValue[p]
    }

    fun initTVList() {
        _tvList.value = mutableListOf()
    }

    init {
        _position.value = SP.position
    }

    fun size(): Int {
        return tvListValue.size
    }

    companion object {
        const val TAG = "TVListModel"
    }
}