package com.lizongying.mytv0.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lizongying.mytv0.SP

class TVGroupModel : ViewModel() {
    private val _tvGroupModel = MutableLiveData<MutableList<TVListModel>>()
    val tvGroupModel: LiveData<MutableList<TVListModel>>
        get() = _tvGroupModel

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    fun setPosition(position: Int) {
        _position.value = position
    }

    fun setTVListModelList(tvTVListModelList: MutableList<TVListModel>) {
        _tvGroupModel.value = tvTVListModelList
    }

    fun addTVListModel(tvListModel: TVListModel) {
        if (_tvGroupModel.value == null) {
            _tvGroupModel.value = mutableListOf(tvListModel)
            return
        }
        _tvGroupModel.value?.add(tvListModel)
    }

    fun getTVListModel(idx: Int): TVListModel? {
        return _tvGroupModel.value?.get(idx)
    }

    init {
        _position.value = SP.positionCategory
    }

    fun size(): Int {
        if (_tvGroupModel.value == null) {
            return 0
        }

        return _tvGroupModel.value!!.size
    }
}