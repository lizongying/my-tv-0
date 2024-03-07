package com.lizongying.mytv0

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import com.google.android.exoplayer2.SimpleExoPlayer
import com.lizongying.mytv0.databinding.PlayerBinding
import com.lizongying.mytv0.models.TVList
import com.lizongying.mytv0.models.TVModel


class PlayerFragment : Fragment(), SurfaceHolder.Callback {
    private var _binding: PlayerBinding? = null

    private var player: ExoPlayer? = null
    private var exoPlayer: SimpleExoPlayer? = null

    private var videoUrl = ""
    private var tvModel: TVModel? = null
    private val aspectRatio = 16f / 9f

    private lateinit var surfaceView: SurfaceView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayerBinding.inflate(inflater, container, false)
        val playerView = _binding!!.playerView
        surfaceView = _binding!!.surfaceView

        if (Utils.isTmallDevice()) {
            playerView.visibility = View.GONE
            surfaceView.holder.addCallback(this)
        } else {
            surfaceView.visibility = View.GONE
            playerView.viewTreeObserver?.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                @OptIn(UnstableApi::class)
                override fun onGlobalLayout() {
                    playerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val renderersFactory = context?.let { DefaultRenderersFactory(it) }
                    val playerMediaCodecSelector = PlayerMediaCodecSelector()
                    renderersFactory?.setMediaCodecSelector(playerMediaCodecSelector)

                    player = context?.let {
                        ExoPlayer.Builder(it)
                            .setRenderersFactory(renderersFactory!!)
                            .build()
                    }
                    playerView.player = player
                    player?.playWhenReady = true
                    player?.addListener(object : Player.Listener {
                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            val ratio = playerView.measuredWidth.div(playerView.measuredHeight)
                            val layoutParams = playerView.layoutParams
                            if (ratio < aspectRatio) {
                                layoutParams?.height =
                                    (playerView.measuredWidth.div(aspectRatio)).toInt()
                                playerView.layoutParams = layoutParams
                            } else if (ratio > aspectRatio) {
                                layoutParams?.width =
                                    (playerView.measuredHeight.times(aspectRatio)).toInt()
                                playerView.layoutParams = layoutParams
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            super.onPlayerError(error)
                            tvModel?.setReady()
                        }
                    })
                    Log.i(TAG, "player ready")
                    ready()
                }
            })
        }

        return _binding!!.root
    }

    fun ready() {
        TVList.listModel.forEach { tvModel ->
            tvModel.ready.observe(this) { _ ->

                // not first time
                if (tvModel.ready.value != null
                    && tvModel.tv.id == TVList.position.value
                    && tvModel.videoUrl.value != null
//                    && tvModel.videoUrl.value != videoUrl
                ) {
                    play(tvModel)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun play(tvModel: TVModel) {
        videoUrl = tvModel.videoUrl.value ?: return
        this.tvModel = tvModel
        Log.i(TAG, "play ${tvModel.tv.title} $videoUrl")
        player?.run {
            IgnoreSSLCertificate.ignore()
            val httpDataSource = DefaultHttpDataSource.Factory()
            httpDataSource.setTransferListener(object : TransferListener {
                override fun onTransferInitializing(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean
                ) {
//                    TODO("Not yet implemented")
                }

                override fun onTransferStart(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean
                ) {
                    Log.d(TAG, "onTransferStart uri ${source.uri}")
//                    TODO("Not yet implemented")
                }

                override fun onBytesTransferred(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean,
                    bytesTransferred: Int
                ) {
//                    TODO("Not yet implemented")
                }

                override fun onTransferEnd(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean
                ) {
//                    TODO("Not yet implemented")
                }
            })

            val hlsMediaSource = HlsMediaSource.Factory(httpDataSource).createMediaSource(
                MediaItem.fromUri(videoUrl)
            )

            setMediaSource(hlsMediaSource)

//            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
        exoPlayer?.run {
            setMediaItem(com.google.android.exoplayer2.MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    @OptIn(UnstableApi::class)
    class PlayerMediaCodecSelector : MediaCodecSelector {
        override fun getDecoderInfos(
            mimeType: String,
            requiresSecureDecoder: Boolean,
            requiresTunnelingDecoder: Boolean
        ): MutableList<androidx.media3.exoplayer.mediacodec.MediaCodecInfo> {
            val infos = MediaCodecUtil.getDecoderInfos(
                mimeType,
                requiresSecureDecoder,
                requiresTunnelingDecoder
            )
            if (mimeType == MimeTypes.VIDEO_H265 && !requiresSecureDecoder && !requiresTunnelingDecoder) {
                if (infos.size > 0) {
                    val infosNew = infos.find { it.name == "c2.android.hevc.decoder" }
                        ?.let { mutableListOf(it) }
                    if (infosNew != null) {
                        return infosNew
                    }
                }
            }
            return infos
        }
    }

    class ExoplayerMediaCodecSelector :
        com.google.android.exoplayer2.mediacodec.MediaCodecSelector {
        override fun getDecoderInfos(
            mimeType: String,
            requiresSecureDecoder: Boolean,
            requiresTunnelingDecoder: Boolean
        ): MutableList<com.google.android.exoplayer2.mediacodec.MediaCodecInfo> {
            val infos = com.google.android.exoplayer2.mediacodec.MediaCodecUtil.getDecoderInfos(
                mimeType,
                requiresSecureDecoder,
                requiresTunnelingDecoder
            )
            if (mimeType == MimeTypes.VIDEO_H265 && !requiresSecureDecoder && !requiresTunnelingDecoder) {
                if (infos.size > 0) {
                    val infosNew = infos.find { it.name == "c2.android.hevc.decoder" }
                        ?.let { mutableListOf(it) }
                    if (infosNew != null) {
                        return infosNew
                    }
                }
            }
            return infos
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val renderersFactory =
            context?.let { com.google.android.exoplayer2.DefaultRenderersFactory(it) }
        val exoplayerMediaCodecSelector = ExoplayerMediaCodecSelector()
        renderersFactory?.setMediaCodecSelector(exoplayerMediaCodecSelector)

        exoPlayer = SimpleExoPlayer.Builder(requireContext(), renderersFactory!!).build()
        exoPlayer?.setVideoSurfaceHolder(holder)
        exoPlayer?.playWhenReady = true
        exoPlayer?.addListener(object : com.google.android.exoplayer2.Player.EventListener {
            override fun onPlayerError(error: com.google.android.exoplayer2.ExoPlaybackException) {
                super.onPlayerError(error)
                tvModel?.setReady()
            }
        })
        exoPlayer?.addVideoListener(object : com.google.android.exoplayer2.video.VideoListener {
            override fun onVideoSizeChanged(
                width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float
            ) {
                val ratio = surfaceView.measuredWidth.div(surfaceView.measuredHeight)
                val layoutParams = surfaceView.layoutParams
                if (ratio < aspectRatio) {
                    layoutParams?.height =
                        (surfaceView.measuredWidth.div(aspectRatio)).toInt()
                    surfaceView.layoutParams = layoutParams
                } else if (ratio > aspectRatio) {
                    layoutParams?.width =
                        (surfaceView.measuredHeight.times(aspectRatio)).toInt()
                    surfaceView.layoutParams = layoutParams
                }
            }
        })
        Log.i(TAG, "exoPlayer ready")
        ready()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun onStart() {
        Log.i(TAG, "onStart")
        super.onStart()
        if (player?.isPlaying == false) {
            Log.i(TAG, "replay")
            player?.prepare()
            player?.play()
        }
        if (exoPlayer?.isPlaying == false) {
            Log.i(TAG, "replay")
            exoPlayer?.prepare()
            exoPlayer?.play()
        }
    }

    override fun onPause() {
        super.onPause()
        if (player?.isPlaying == true) {
            player?.stop()
        }
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        exoPlayer?.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "PlayerFragment"
    }
}