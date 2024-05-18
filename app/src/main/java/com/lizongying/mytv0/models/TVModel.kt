package com.lizongying.mytv0.models

import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.lizongying.mytv0.SP
import java.util.Locale

class TVModel(var tv: TV) : ViewModel() {
    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int>
        get() = _position

    var retryTimes = 0
    var retryMaxTimes = 8
    var programUpdateTime = 0L

    var groupIndex = 0
    var listIndex = 0

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

    private fun getVideoUrl(): String? {
        if (_videoIndex.value == null || tv.uris.isEmpty()) {
            return null
        }

        if (videoIndex.value!! >= tv.uris.size) {
            return null
        }

        return tv.uris[_videoIndex.value!!]
    }

    private val _like = MutableLiveData<Boolean>()
    val like: LiveData<Boolean>
        get() = _like

    fun setLike(liked: Boolean) {
        _like.value = liked
    }

    private val _ready = MutableLiveData<Boolean>()
    val ready: LiveData<Boolean>
        get() = _ready

    fun setReady() {
        _ready.value = true
    }

    private val _videoIndex = MutableLiveData<Int>()
    private val videoIndex: LiveData<Int>
        get() = _videoIndex

    init {
        _position.value = 0
        _videoIndex.value = 0
        _like.value = SP.getLike(tv.id)
        _videoUrl.value = getVideoUrl()
        _program.value = mutableListOf()
    }

    fun update(t: TV) {
        tv = t
    }

    @OptIn(UnstableApi::class)
    fun buildSource(): MediaSource? {
        val url = getVideoUrl() ?: return null
        val uri = Uri.parse(url) ?: return null
        val path = uri.path ?: return null
        val scheme = uri.scheme ?: return null

        var userAgent = ""
        val httpDataSource = DefaultHttpDataSource.Factory()
        httpDataSource.setKeepPostFor302Redirects(true)
        httpDataSource.setAllowCrossProtocolRedirects(true)
        tv.headers?.let {
            httpDataSource.setDefaultRequestProperties(it)
            it.forEach { (key, value) ->
                if (key.equals("user-agent", ignoreCase = true)) {
                    userAgent = value
                    return@forEach
                }
            }
        }

        val mediaItem = MediaItem.fromUri(uri.toString())
        return if (path.lowercase().endsWith(".m3u8")) {
            HlsMediaSource.Factory(httpDataSource).createMediaSource(mediaItem)
        } else if (path.lowercase().endsWith(".mpd")) {
            DashMediaSource.Factory(httpDataSource).createMediaSource(mediaItem)
        } else if (scheme.lowercase() == "rtsp") {
            if (userAgent.isEmpty()) {
                RtspMediaSource.Factory().createMediaSource(mediaItem)
            } else {
                RtspMediaSource.Factory().setUserAgent(userAgent).createMediaSource(mediaItem)
            }
        } else if (isVideoFile(uri)) {
            ProgressiveMediaSource.Factory(httpDataSource).createMediaSource(mediaItem)
        } else {
            HlsMediaSource.Factory(httpDataSource).createMediaSource(mediaItem)
        }
    }

    private fun isVideoFile(uri: Uri): Boolean {
        val path = uri.lastPathSegment ?: return false
        return if (path.contains(".")) {
            val fileExtension =
                path.substring(path.lastIndexOf('.') + 1)
                    .lowercase(
                        Locale.getDefault()
                    )
            fileExtension == "mp4" || fileExtension == "avi" || fileExtension == "mov" || fileExtension == "wmv" || fileExtension == "mkv" || fileExtension == "flv" || fileExtension == "mpeg" || fileExtension == "webm"
        } else {
            false
        }
    }

    companion object {
        private const val TAG = "TVModel"
    }
}