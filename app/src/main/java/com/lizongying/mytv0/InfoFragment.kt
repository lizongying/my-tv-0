package com.lizongying.mytv0

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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


        val application = requireActivity().applicationContext as MyTVApplication

        binding.info.layoutParams.width = application.px2Px(binding.info.layoutParams.width)
        binding.info.layoutParams.height = application.px2Px(binding.info.layoutParams.height)

        val layoutParams = binding.info.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = application.px2Px(binding.info.marginBottom)
        binding.info.layoutParams = layoutParams

        binding.logo.layoutParams.width = application.px2Px(binding.logo.layoutParams.width)
        var padding = application.px2Px(binding.logo.paddingTop)
        binding.logo.setPadding(padding, padding, padding, padding)
        binding.main.layoutParams.width = application.px2Px(binding.main.layoutParams.width)
        padding = application.px2Px(binding.main.paddingTop)
        binding.main.setPadding(padding, padding, padding, padding)

        val layoutParamsMain = binding.main.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsMain.marginStart = application.px2Px(binding.main.marginStart)
        binding.main.layoutParams = layoutParamsMain

        val layoutParamsDesc = binding.desc.layoutParams as ViewGroup.MarginLayoutParams
        layoutParamsDesc.topMargin = application.px2Px(binding.desc.marginTop)
        binding.desc.layoutParams = layoutParamsDesc

        binding.title.textSize = application.px2PxFont(binding.title.textSize)
        binding.desc.textSize = application.px2PxFont(binding.desc.textSize)

        binding.container.layoutParams.width = application.shouldWidthPx()
        binding.container.layoutParams.height = application.shouldHeightPx()

        _binding!!.root.visibility = View.GONE
        return binding.root
    }

    fun show(tvViewModel: TVModel) {
        val context = requireContext()
        binding.title.text = tvViewModel.tv.title

        when (tvViewModel.tv.title) {
            else -> {
                if (tvViewModel.tv.logo.isNullOrBlank()) {
                    val width = Utils.dpToPx(100)
                    val height = Utils.dpToPx(60)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)

                    val text = "${tvViewModel.tv.id + 1}"
                    var size = 100f
                    if (tvViewModel.tv.id > 999) {
                        size = 90f
                    }
                    val paint = Paint().apply {
                        color = ContextCompat.getColor(context, R.color.blur)
                        textSize = size
                        textAlign = Paint.Align.CENTER
                    }
                    val x = width / 2f
                    val y = height / 2f - (paint.descent() + paint.ascent()) / 2
                    canvas.drawText(text, x, y, paint)

                    Glide.with(this)
                        .load(BitmapDrawable(context.resources, bitmap))
//                        .centerInside()
                        .into(binding.logo)
                } else {
                    Glide.with(this)
                        .load(tvViewModel.tv.logo)
//                        .centerInside()
                        .into(binding.logo)
                }
            }
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