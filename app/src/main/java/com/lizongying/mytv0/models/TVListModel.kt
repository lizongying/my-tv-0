package com.lizongying.mytv0.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lizongying.mytv0.SP

class TVListModel(private val name: String, private val groupIndex: Int) : ViewModel() {
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

    private val _positionPlaying = MutableLiveData<Int>()
    val positionPlaying: LiveData<Int>
        get() = _positionPlaying
    val positionPlayingValue: Int
        get() = _positionPlaying.value ?: 0

    fun setPosition(position: Int) {
//        Log.i(TAG, "選擇頻道 $position")
        _position.value = position
    }

    fun setPositionPlaying(position: Int) {
        _positionPlaying.value = position
        SP.position = position
    }

    fun setPlaying() {
        _positionPlaying.value = positionValue
        SP.position = positionValue
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
        if (_tvList.value == null) {
            _tvList.value = mutableListOf(tvModel)
            return
        }

        val newList = _tvList.value!!.toMutableList()
        newList.add(tvModel)
        _tvList.value = newList
    }

    fun removeTVModel(id: Int) {
        if (_tvList.value == null) {
            return
        }
        val newList = _tvList.value!!.toMutableList()
        val iterator = newList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().tv.id == id) {
                iterator.remove()
            }
        }
        _tvList.value = newList
    }

    fun replaceTVModel(tvModel: TVModel) {
        if (_tvList.value == null) {
            _tvList.value = mutableListOf(tvModel)
            return
        }

        val newList = _tvList.value!!.toMutableList()
        var exists = false
        val iterator = newList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().tv.id == tvModel.tv.id) {
                exists = true
            }
        }
        if (!exists) {
            newList.add(tvModel)
            _tvList.value = newList
        }
    }

    fun initTVList() {
        _tvList.value = mutableListOf()
    }

    fun clearData() {
        initTVList()
        setPosition(0)
    }

    fun getTVModel(): TVModel? {
        return getTVModel(positionValue)
    }

    fun getTVModel(idx: Int): TVModel? {
        if (idx >= size()) {
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

        val p = (size() + positionValue - 1) % size()
        setPosition(p)
        return tvListValue[p]
    }

    fun getNext(): TVModel? {
        if (size() == 0) {
            return null
        }

        val p = (positionValue + 1) % size()
        setPosition(p)
        return tvListValue[p]
    }

    init {
        _position.value = SP.position
    }

    fun size(): Int {
        if (_tvList.value == null) {
            return 0
        }

        return tvListValue.size
    }

    companion object {
        const val TAG = "TVListModel"
    }
}