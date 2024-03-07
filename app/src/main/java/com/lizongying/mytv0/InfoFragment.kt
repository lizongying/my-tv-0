package com.lizongying.mytv0

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.lizongying.mytv0.databinding.InfoBinding
import com.lizongying.mytv0.models.TVModel


class InfoFragment : Fragment() {
    private var _binding: InfoBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler()
    private val delay: Long = 3000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InfoBinding.inflate(inflater, container, false)
        _binding!!.root.visibility = View.GONE
        return binding.root
    }

    fun show(tvViewModel: TVModel) {
        binding.textView.text = tvViewModel.tv.title

        when (tvViewModel.tv.title) {
            else -> Glide.with(this)
                .load(tvViewModel.tv.logo)
                .into(binding.infoLogo)
        }

//        val program = tvViewModel.getProgramOne()
//        if (program != null) {
//            binding.infoDesc.text = program.name
//        }

        handler.removeCallbacks(removeRunnable)
        view?.visibility = View.VISIBLE
        handler.postDelayed(removeRunnable, delay)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(removeRunnable, delay)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(removeRunnable)
    }

    private val removeRunnable = Runnable {
        view?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "InfoFragment"
    }
}