package com.lizongying.mytv0

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.DISCONTINUITY_REASON_AUTO_TRANSITION
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import com.lizongying.mytv0.databinding.PlayerBinding
import com.lizongying.mytv0.models.SourceType
import com.lizongying.mytv0.models.TVModel


class PlayerFragment : Fragment() {
    private var _binding: PlayerBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null

    private var tvModel: TVModel? = null
    private val aspectRatio = 16f / 9f

    private lateinit var mainActivity: MainActivity

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        mainActivity = activity as MainActivity
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayerBinding.inflate(inflater, container, false)
        val playerView = _binding!!.playerView

        playerView.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            @OptIn(UnstableApi::class)
            override fun onGlobalLayout() {
                playerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val renderersFactory = context?.let { DefaultRenderersFactory(it) }
                val playerMediaCodecSelector = PlayerMediaCodecSelector()
                renderersFactory?.setMediaCodecSelector(playerMediaCodecSelector)
                renderersFactory?.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

                player = context?.let {
                    ExoPlayer.Builder(it)
                        .setRenderersFactory(renderersFactory!!)
                        .build()
                }
                playerView.player = player
                player?.repeatMode = REPEAT_MODE_ALL
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

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        if (isPlaying) {
                            tvModel?.confirmSourceType()
                            tvModel?.setErrInfo("")
                            tvModel!!.retryTimes = 0
                        } else {
                            Log.i(TAG, "${tvModel?.tv?.title} 播放停止")
//                                tvModel?.setErrInfo("播放停止")
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d(TAG, "playbackState $playbackState")
                        super.onPlaybackStateChanged(playbackState)
                    }


                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        if (reason == DISCONTINUITY_REASON_AUTO_TRANSITION) {
                            mainActivity.onPlayEnd()
                        }
                        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        tvModel?.setErrInfo(R.string.play_error.getString())
                        if (tvModel?.getSourceType() == SourceType.UNKNOWN) {
                            tvModel?.nextSource()
                        }
                        if (tvModel!!.retryTimes < tvModel!!.retryMaxTimes) {
                            tvModel?.setReady()
                            tvModel!!.retryTimes++
                        }
                    }
                })

                (activity as MainActivity).ready(TAG)
                Log.i(TAG, "player ready")
            }
        })

        return _binding!!.root
    }

    @OptIn(UnstableApi::class)
    fun play(tvModel: TVModel) {
        this.tvModel = tvModel
        player?.run {
            IgnoreSSLCertificate.ignore()
            val httpDataSource = DefaultHttpDataSource.Factory()
            httpDataSource.setKeepPostFor302Redirects(true)
            httpDataSource.setAllowCrossProtocolRedirects(true)
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

            val dataSource = tvModel.getSource()
            if (dataSource != null) {
                setMediaSource(dataSource)
            } else {
                val mediaItem = tvModel.getMediaItem()
                if (mediaItem == null) {
                    tvModel.setErrInfo(R.string.play_error.getString())
                    if (tvModel.getSourceType() == SourceType.UNKNOWN) {
                        tvModel.nextSource()
                    }
                    if (tvModel.retryTimes < tvModel.retryMaxTimes) {
                        tvModel.setReady()
                        tvModel.retryTimes++
                    }
                    return
                }
                setMediaItem(mediaItem)
            }

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

    override fun onStart() {
        Log.i(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {//move from onstart to onresume, for fix bug on sharp tv
        Log.i(TAG, "play onResume")
        super.onResume()
        if (player?.isPlaying == false) {
            Log.i(TAG, "replay")
            player?.prepare()
            player?.play()
        }
    }

    override fun onPause() {
        super.onPause()
        if (player?.isPlaying == true) {
            player?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "PlayerFragment"
    }
}