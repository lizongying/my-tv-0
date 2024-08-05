package com.lizongying.mytv0.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lizongying.mytv0.SP

class TVGroupModel : ViewModel() {
    var isInLikeMode = false
    private val _tvGroupModel = MutableLiveData<List<TVListModel>>()
    val tvGroupModel: LiveData<List<TVListModel>>
        get() = _tvGroupModel

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    private val _change = MutableLiveData<Boolean>()
    val change: LiveData<Boolean>
        get() = _change

    fun setPosition(position: Int) {
        _position.value = position
    }

    fun setChange() {
        _change.value = true
    }

    fun setTVListModelList(tvListModelList: List<TVListModel>) {
        _tvGroupModel.value = tvListModelList
    }

    fun addTVListModel(tvListModel: TVListModel) {
        if (_tvGroupModel.value == null) {
            _tvGroupModel.value = mutableListOf(tvListModel)
            return
        }

        val newList = _tvGroupModel.value!!.toMutableList()
        newList.add(tvListModel)
        _tvGroupModel.value = newList
    }

    fun clear() {
        if (SP.showAllChannels) {
            _tvGroupModel.value = mutableListOf(getTVListModel(0)!!, getTVListModel(1)!!)
            setPosition(0)
            getTVListModel(1)?.clear()
        } else {
            _tvGroupModel.value = mutableListOf(getTVListModel(0)!!)
            setPosition(0)
        }
    }

    fun getTVListModel(): TVListModel? {
        return getTVListModel(position.value as Int)
    }

    fun getTVListModel(idx: Int): TVListModel? {
        if (idx >= size()) {
            return null
        }
        if (SP.showAllChannels) {
            return _tvGroupModel.value?.get(idx)
        }
        return _tvGroupModel.value?.filter { it.getName() != "全部頻道" }?.get(idx)
    }

    init {
        _position.value = SP.positionGroup
        isInLikeMode = SP.defaultLike
    }

    fun size(): Int {
        if (_tvGroupModel.value == null) {
            return 0
        }
        if (SP.showAllChannels) {
            return _tvGroupModel.value!!.size
        }
        return _tvGroupModel.value!!.filter { it.getName() != "全部頻道" }.size
    }
}