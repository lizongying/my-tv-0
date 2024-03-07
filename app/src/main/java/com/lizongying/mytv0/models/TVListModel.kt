package com.lizongying.mytv0.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TVListModel(private val name: String) : ViewModel() {
    fun getName(): String {
        return name
    }

    private val _tvListModel = MutableLiveData<MutableList<TVModel>>()
    val tvListModel: LiveData<MutableList<TVModel>>
        get() = _tvListModel

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    fun setPosition(position: Int) {
        _position.value = position
    }

    private val _change = MutableLiveData<Boolean>()
    val change: LiveData<Boolean>
        get() = _change

    fun setChange() {
        _change.value = true
    }

    fun setTVListModel(tvListModel: List<TVModel>) {
        _tvListModel.value = tvListModel.toMutableList()
    }

    fun addTVModel(tvModel: TVModel) {
        if (_tvListModel.value == null) {
            _tvListModel.value = mutableListOf(tvModel)
            return
        }
        _tvListModel.value?.add(tvModel)
    }

    fun getTVModel(idx: Int): TVModel? {
        return _tvListModel.value?.get(idx)
    }

    init {
        _position.value = 0
    }

    fun size(): Int {
        if (_tvListModel.value == null) {
            return 0
        }

        return _tvListModel.value!!.size
    }
}