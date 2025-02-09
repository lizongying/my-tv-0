package com.lizongying.mytv0.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lizongying.mytv0.SP

class TVGroupModel : ViewModel() {
    var version = 0
    var isInLikeMode = false

    private val _tvGroup = MutableLiveData<List<TVListModel>>()
    val tvGroup: LiveData<List<TVListModel>>
        get() = _tvGroup
    val tvGroupValue: List<TVListModel>
        get() = _tvGroup.value ?: emptyList()

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
        get() = _positionPlaying.value ?: DEFAULT_POSITION_PLAYING

    fun setPositionPlaying(position: Int) {
        _positionPlaying.value = position
        SP.positionGroup = position
    }

    fun setPositionPlaying() {
        setPositionPlaying(positionValue)
    }

    private val _change = MutableLiveData<Int>()
    val change: LiveData<Int>
        get() = _change

    fun setChange() {
        _change.value = version
        version++
    }

    fun setTVListModelList(tvGroup: List<TVListModel>) {
        _tvGroup.value = tvGroup
    }

    fun addTVListModel(listTVModel: TVListModel) {
        _tvGroup.value = tvGroupValue.toMutableList().apply {
            add(listTVModel)
        }
    }

    fun getTVListModel(): TVListModel? {
        return getTVListModel(positionValue)
    }

    fun getTVListModel(idx: Int): TVListModel? {
        if (idx < 0 || idx >= size()) {
            return null
        }

        if (SP.showAllChannels) {
            return tvGroupValue[idx]
        }

        return tvGroupValue.filterIndexed { index, _ -> index != 1 }[idx]
    }

    private fun getTVListModelNotFilter(idx: Int): TVListModel? {
        if (idx < 0 || idx >= tvGroupValue.size) {
            return null
        }

        return tvGroupValue[idx]
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
        if (tvGroupValue.size < 3 || tvGroupValue[1].size() == 0) {
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
        if (tvGroupValue.size < 3 || tvGroupValue[1].size() == 0) {
            return null
        }

        var tvListModel = getCurrentList() ?: return null

        if (keep) {
            return tvListModel.getPrev()
        }

        // Prev tvListModel
        if (tvListModel.positionPlayingValue == 0) {
            var p = (tvGroupValue.size + positionPlayingValue - 1) % tvGroupValue.size
            if (p == 1 || p == 0) {
                // 最後一組
                p = (tvGroupValue.size - 1) % tvGroupValue.size
            }
            setPositionPlaying(p)
            setPosition(p)

//            Log.i(TAG, "group positionPlaying $p/${tvGroupValue.size - 1}")
            tvListModel = getTVListModelNotFilter(p)!!
            return tvListModel.getTVModel(tvListModel.size() - 1)
        }

        return tvListModel.getPrev()
    }

    // get & set
    fun getNext(keep: Boolean = false): TVModel? {
        // No item
        if (tvGroupValue.size < 3 || tvGroupValue[1].size() == 0) {
            return null
        }

        var tvListModel = getCurrentList() ?: return null

        if (keep) {
            return tvListModel.getNext()
        }

        // Next tvListModel
        if (tvListModel.positionPlayingValue == tvListModel.size() - 1) {
            var p = (positionPlayingValue + 1) % tvGroupValue.size
            if (p == 0) {
                // 第一組
                p = 2
            }
            setPositionPlaying(p)
            setPosition(p)

//            Log.i(TAG, "group positionPlaying $p/${tvGroupValue.size - 1}")
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

    fun initTVGroup() {
        _tvGroup.value = mutableListOf(
            tvGroupValue[0],
            tvGroupValue[1]
        )
        tvGroupValue[1].initTVList()
    }

    fun initPosition() {
        setPosition(defaultPosition())
        setPositionPlaying()
    }

    init {
        setPosition(SP.positionGroup)
        setPositionPlaying()
        isInLikeMode = SP.defaultLike && positionValue == 0
    }

    fun size(): Int {
        if (SP.showAllChannels) {
            return tvGroupValue.size
        }

        return tvGroupValue.size - 1
    }

    companion object {
        const val TAG = "TVGroupModel"
        const val DEFAULT_POSITION_PLAYING = -1
    }
}