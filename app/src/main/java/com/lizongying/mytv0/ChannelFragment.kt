package com.lizongying.mytv0

import MainViewModel
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginEnd
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lizongying.mytv0.databinding.ChannelBinding
import com.lizongying.mytv0.models.TVModel

class ChannelFragment : Fragment() {
    private var _binding: ChannelBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler()
    private val delay: Long = 5000
    private var channel = 0
    private var channelCount = 0

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChannelBinding.inflate(inflater, container, false)
        _binding!!.root.visibility = View.GONE

        val application = requireActivity().applicationContext as MyTVApplication

        binding.channel.layoutParams.width = application.px2Px(binding.channel.layoutParams.width)
        binding.channel.layoutParams.height = application.px2Px(binding.channel.layoutParams.height)

        val layoutParams = binding.channel.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = application.px2Px(binding.channel.marginTop)
        layoutParams.marginEnd = application.px2Px(binding.channel.marginEnd)
        binding.channel.layoutParams = layoutParams

        binding.content.textSize = application.px2PxFont(binding.content.textSize)
        binding.time.textSize = application.px2PxFont(binding.time.textSize)

        binding.main.layoutParams.width = application.shouldWidthPx()
        binding.main.layoutParams.height = application.shouldHeightPx()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireActivity()
        viewModel = ViewModelProvider(context)[MainViewModel::class.java]
    }

    fun show(tvViewModel: TVModel) {
        handler.removeCallbacks(hideRunnable)
        handler.removeCallbacks(playRunnable)
        if (_binding != null) {
            binding.content.text = (tvViewModel.tv.id.plus(1)).toString()
        }
        view?.visibility = View.VISIBLE
        handler.postDelayed(hideRunnable, delay)
    }

    fun show(channel: String) {
        if (viewModel.groupModel.getCurrent()!!.tv.id > 10 && viewModel.groupModel.getCurrent()!!.tv.id == this.channel - 1) {
            this.channel = 0
            channelCount = 0
        }
        if (channelCount > 2) {
            return
        }
        channelCount++
        this.channel = "${this.channel}$channel".toInt()
        handler.removeCallbacks(hideRunnable)
        handler.removeCallbacks(playRunnable)
        if (channelCount < 3) {
            binding.content.text = this.channel.toString()
            view?.visibility = View.VISIBLE
            handler.postDelayed(playRunnable, delay)
        } else {
            handler.postDelayed(playRunnable, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        if (view?.visibility == View.VISIBLE) {
            handler.postDelayed(hideRunnable, delay)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(hideRunnable)
        handler.removeCallbacks(playRunnable)
    }

    private val hideRunnable = Runnable {
        if (_binding != null) {
            binding.content.text = BLANK
        }

        view?.visibility = View.GONE
        channel = 0
        channelCount = 0
    }

    private val playRunnable = Runnable {
        (activity as MainActivity).play(channel - 1)
        handler.postDelayed(hideRunnable, delay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ChannelFragment"
        private const val BLANK = ""
    }
}