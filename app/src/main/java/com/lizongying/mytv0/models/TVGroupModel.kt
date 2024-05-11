package com.lizongying.mytv0.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lizongying.mytv0.SP

class TVGroupModel : ViewModel() {
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
        _tvGroupModel.value = mutableListOf(getTVListModel(0)!!, getTVListModel(1)!!)
        setPosition(0)
        getTVListModel(1)?.clear()
    }

    fun getTVListModel(): TVListModel? {
        return getTVListModel(position.value as Int)
    }

    fun getTVListModel(idx: Int): TVListModel? {
        if (idx >= size()) {
            return null
        }
        return _tvGroupModel.value?.get(idx)
    }

    init {
        _position.value = SP.positionGroup
    }

    fun size(): Int {
        if (_tvGroupModel.value == null) {
            return 0
        }

        return _tvGroupModel.value!!.size
    }
}