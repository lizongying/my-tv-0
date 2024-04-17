package com.lizongying.mytv0

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
        _binding!!.root.visibility = View.GONE
        return binding.root
    }

    fun show(tvViewModel: TVModel) {
        binding.textView.text = tvViewModel.tv.title

        when (tvViewModel.tv.title) {
            else -> {
                if (tvViewModel.tv.logo.isNullOrBlank()) {
                    val width = Utils.dpToPx(100)
                    val height = Utils.dpToPx(60)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)

                    val paint = Paint().apply {
                        color = ContextCompat.getColor(context!!, R.color.blur)
                        textSize = 100f
                        textAlign = Paint.Align.CENTER
                    }
                    val text = "${tvViewModel.tv.id + 1}"
                    val x = width / 2f
                    val y = height / 2f - (paint.descent() + paint.ascent()) / 2
                    canvas.drawText(text, x, y, paint)

                    Glide.with(this)
                        .load(BitmapDrawable(context?.resources, bitmap))
//                        .centerInside()
                        .into(binding.infoLogo)
                } else {
                    Glide.with(this)
                        .load(tvViewModel.tv.logo)
//                        .centerInside()
                        .into(binding.infoLogo)
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