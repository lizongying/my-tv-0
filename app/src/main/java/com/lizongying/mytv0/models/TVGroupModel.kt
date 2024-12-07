package com.lizongying.mytv0.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lizongying.mytv0.SP

class TVGroupModel : ViewModel() {
    var isInLikeMode = false
    private val _tvGroup = MutableLiveData<List<TVListModel>>()
    val tvGroup: LiveData<List<TVListModel>>
        get() = _tvGroup
    val tvGroupValue: List<TVListModel>
        get() = _tvGroup.value ?: listOf()

    // Filtered
    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position
    val positionValue: Int
        get() = _position.value ?: 0

    private val _positionPlaying = MutableLiveData<Int>()
    val positionPlaying: LiveData<Int>
        get() = _positionPlaying
    val positionPlayingValue: Int
        get() = _positionPlaying.value ?: DEFAULT_POSITION_PLAYING

    private val _change = MutableLiveData<Boolean>()
    val change: LiveData<Boolean>
        get() = _change

    fun setPosition(position: Int) {
        _position.value = position
    }

    fun setPositionPlaying(position: Int) {
        _positionPlaying.value = position
        SP.positionGroup = position
    }

    fun setPlaying() {
        _positionPlaying.value = positionValue
        SP.positionGroup = positionValue
    }

    fun setPrevPosition() {
        _position.value = (positionValue - 1) % size()
    }

    fun setNextPosition() {
        _position.value = (positionValue + 1) % size()
    }

    fun setChange() {
        _change.value = true
    }

    fun setTVListModelList(tvGroup: List<TVListModel>) {
        _tvGroup.value = tvGroup
    }

    fun addTVListModel(listTVModel: TVListModel) {
        if (_tvGroup.value == null) {
            _tvGroup.value = mutableListOf(listTVModel)
            return
        }

        val newList = _tvGroup.value!!.toMutableList()
        newList.add(listTVModel)
        _tvGroup.value = newList
    }

    fun initTVGroup() {
        _tvGroup.value = mutableListOf(
            (_tvGroup.value as List<TVListModel>)[0],
            (_tvGroup.value as List<TVListModel>)[1]
        )
        (_tvGroup.value as List<TVListModel>)[1].initTVList()
    }

    fun initPosition() {
        setPosition(defaultPosition())
        setPositionPlaying(defaultPosition())
    }

    fun clearData() {
        if (SP.showAllChannels) {
            _tvGroup.value =
                mutableListOf(getFavoritesList()!!, getAllList()!!)
            setPosition(0)
            getAllList()?.clearData()
        } else {
            _tvGroup.value = mutableListOf(getFavoritesList()!!)
            setPosition(0)
        }
    }

    fun getTVListModel(): TVListModel? {
        return getTVListModel(positionValue)
    }

    fun getTVListModel(idx: Int): TVListModel? {
        if (idx >= size()) {
            return null
        }
        if (SP.showAllChannels) {
            return _tvGroup.value?.get(idx)
        }
        return _tvGroup.value?.filter { it.getName() != "全部頻道" }?.get(idx)
    }

    fun getTVListModelNotFilter(idx: Int): TVListModel? {
        if (idx >= tvGroupValue.size) {
            return null
        }

        return _tvGroup.value?.get(idx)
    }

    // get & set
    fun getPosition(position: Int): TVModel? {

        // No item
        if (tvGroupValue[1].size() == 0) {
            return null
        }

        var count = 0
        for ((index, i) in tvGroupValue.withIndex()) {
            val countBefore = count
            count += i.size()
            if (count > position) {
                setPosition(index)
                val listPosition = position - countBefore
                i.setPosition(listPosition)
                return i.getTVModel(listPosition)
            }
        }

        return null
    }

    fun getCurrent(): TVModel? {

        // No item
        if (tvGroupValue.size < 2 || tvGroupValue[1].size() == 0) {
            return null
        }

        return getCurrentList()?.getCurrent()
    }

    fun getCurrentList(): TVListModel? {
        return getTVListModelNotFilter(positionValue)
    }

    fun getFavoritesList(): TVListModel? {
        return getTVListModelNotFilter(0)
    }

    fun getAllList(): TVListModel? {
        return getTVListModelNotFilter(1)
    }

    // get & set
    // keep: In the current list loop
    fun getPrev(keep: Boolean = false): TVModel? {
        // No item
        if (tvGroupValue.size < 2 || tvGroupValue[1].size() == 0) {
            return null
        }

        var tvListModel = getCurrentList() ?: return null

        if (keep) {
            Log.i(TAG, "group position $positionValue")
            return tvListModel.getPrev()
        }

        // Prev tvListModel
        if (tvListModel.positionValue == 0) {
            var p = (tvGroupValue.size + positionValue - 1) % tvGroupValue.size
            setPosition(p)
            if (p == 1) {
                p = (tvGroupValue.size + positionValue - 1) % tvGroupValue.size
                setPosition(p)
            }
            if (p == 0) {
                p = (tvGroupValue.size + positionValue - 1) % tvGroupValue.size
                setPosition(p)
            }

            Log.i(TAG, "group position $p/${tvGroupValue.size}")
            tvListModel = getTVListModelNotFilter(p)!!
            return tvListModel.getTVModel(tvListModel.size() - 1)
        }

        return tvListModel.getPrev()
    }

    // get & set
    fun getNext(keep: Boolean = false): TVModel? {
        // No item
        if (tvGroupValue.size < 2 || tvGroupValue[1].size() == 0) {
            return null
        }

        var tvListModel = getCurrentList() ?: return null

        if (keep) {
            return tvListModel.getNext()
        }

        // Next tvListModel
        if (tvListModel.positionValue == tvListModel.size() - 1) {
            var p = (positionValue + 1) % tvGroupValue.size
            setPosition(p)
            if (p == 0) {
                p = (tvGroupValue.size + positionValue + 1) % tvGroupValue.size
                setPosition(p)
            }
            if (p == 1) {
                p = (tvGroupValue.size + positionValue + 1) % tvGroupValue.size
                setPosition(p)
            }

            Log.i(TAG, "group position $p/${tvGroupValue.size}")
            tvListModel = getTVListModelNotFilter(p)!!
            return tvListModel.getTVModel(0)
        }

        return tvListModel.getNext()
    }

    fun defaultPosition(): Int {
        // 1 全部
        // 2 第一組
        return if (tvGroupValue.size > 2) 2 else 1
    }

    init {
        _position.value = SP.positionGroup
        isInLikeMode = SP.defaultLike && _position.value == 0
    }

    fun size(): Int {
        if (_tvGroup.value == null) {
            return 0
        }
        if (SP.showAllChannels) {
            return _tvGroup.value!!.size
        }
        return _tvGroup.value!!.filter { it.getName() != "全部頻道" }.size
    }

    companion object {
        const val TAG = "TVGroupModel"
        const val DEFAULT_POSITION_PLAYING = -1
    }
}