package com.lizongying.mytv0.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TVListModel(private val name: String, private val index: Int) : ViewModel() {
    fun getName(): String {
        return name
    }

    fun getIndex(): Int {
        return index
    }

    private val _tvListModel = MutableLiveData<List<TVModel>>()
    val tvListModel: LiveData<List<TVModel>>
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
        _tvListModel.value = tvListModel
    }

    fun addTVModel(tvModel: TVModel) {
        if (_tvListModel.value == null) {
            _tvListModel.value = mutableListOf(tvModel)
            return
        }

        val newList = _tvListModel.value!!.toMutableList()
        newList.add(tvModel)
        _tvListModel.value = newList
    }

    fun removeTVModel(id: Int) {
        if (_tvListModel.value == null) {
            return
        }
        val newList = _tvListModel.value!!.toMutableList()
        val iterator = newList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().tv.id == id) {
                iterator.remove()
            }
        }
        _tvListModel.value = newList
    }

    fun replaceTVModel(tvModel: TVModel) {
        if (_tvListModel.value == null) {
            _tvListModel.value = mutableListOf(tvModel)
            return
        }

        val newList = _tvListModel.value!!.toMutableList()
        var exists = false
        val iterator = newList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().tv.id == tvModel.tv.id) {
                exists = true
            }
        }
        if (!exists) {
            newList.add(tvModel)
            _tvListModel.value = newList
        }
    }

    fun clear() {
        _tvListModel.value = mutableListOf()
        setPosition(0)
    }

    fun getTVModel(): TVModel? {
        return getTVModel(position.value as Int)
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