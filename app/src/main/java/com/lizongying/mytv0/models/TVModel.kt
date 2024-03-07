package com.lizongying.mytv0.models

import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource

class TVModel(var tv: TV) : ViewModel() {
    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    var retryTimes = 0
    var retryMaxTimes = 8
    var programUpdateTime = 0L

    private val _errInfo = MutableLiveData<String>()
    val errInfo: LiveData<String>
        get() = _errInfo

    fun setErrInfo(info: String) {
        _errInfo.value = info
    }

    private var _program = MutableLiveData<MutableList<Program>>()
    val program: LiveData<MutableList<Program>>
        get() = _program

    private val _videoUrl = MutableLiveData<String>()
    val videoUrl: LiveData<String>
        get() = _videoUrl

    fun setVideoUrl(url: String) {
        _videoUrl.value = url
    }

    fun getVideoUrl(): String? {
        return _videoIndex.value?.let { tv.videoUrl[it] }
    }

    private val _ready = MutableLiveData<Boolean>()
    val ready: LiveData<Boolean>
        get() = _ready

    fun setReady() {
        _ready.value = true
    }

    private val _videoIndex = MutableLiveData<Int>()
    val videoIndex: LiveData<Int>
        get() = _videoIndex

    init {
        _position.value = 0
        _videoIndex.value = 0
        _videoUrl.value = getVideoUrl()
        _program.value = mutableListOf()
    }

    fun update(t: TV) {
        tv = t
    }

    @OptIn(UnstableApi::class)
    fun buildSource(): HlsMediaSource {
        val httpDataSource = DefaultHttpDataSource.Factory()
        tv.headers?.let { httpDataSource.setDefaultRequestProperties(it) }

        return HlsMediaSource.Factory(httpDataSource).createMediaSource(
                    MediaItem.fromUri(_videoUrl.value!!)
        )
    }

    companion object {
        private const val TAG = "TVModel"
    }
}