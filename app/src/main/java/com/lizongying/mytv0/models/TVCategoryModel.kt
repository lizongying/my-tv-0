package com.lizongying.mytv0.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lizongying.mytv0.SP

class TVCategoryModel : ViewModel() {
    private val _tvCategoryModel = MutableLiveData<MutableList<TVListModel>>()
    val tvCategoryModel: LiveData<MutableList<TVListModel>>
        get() = _tvCategoryModel

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    fun setPosition(position: Int) {
        _position.value = position
    }

    fun addTVListModel(tvListModel: TVListModel) {
        if (_tvCategoryModel.value == null) {
            _tvCategoryModel.value = mutableListOf(tvListModel)
            return
        }
        _tvCategoryModel.value?.add(tvListModel)
    }

    fun getTVListModel(idx: Int): TVListModel? {
        return _tvCategoryModel.value?.get(idx)
    }

    init {
        _position.value = SP.positionCategory
        _position.value = 2
    }

    fun size(): Int {
        if (_tvCategoryModel.value == null) {
            return 0
        }

        return _tvCategoryModel.value!!.size
    }
}