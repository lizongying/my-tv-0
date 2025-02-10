package com.lizongying.mytv0

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.lizongying.mytv0.Utils.getDateTimestamp
import com.lizongying.mytv0.databinding.ModalBinding


class ModalFragment : DialogFragment() {

    private var _binding: ModalBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.myLooper()!!)
    private val delayHideAppreciateModal = 10000L

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString(KEY_URL)
        if (!url.isNullOrEmpty()) {
            val size = Utils.dpToPx(200)
            val u = "$url?${getDateTimestamp().toString().reversed()}"
            val img = QrCodeUtil().createQRCodeBitmap(u, size, size)

            Glide.with(requireContext())
                .load(img)
                .into(binding.modalImage)
            binding.modalText.text = u.removePrefix("http://")
            binding.modalText.visibility = View.VISIBLE
            if (!isTV()) {
                binding.modal.setOnClickListener {
                    try {
                        val mainActivity = (activity as MainActivity)
                        mainActivity.showWebViewPopup(u)
                        handler.postDelayed(hideAppreciateModal, 0)
                    } catch (e: Exception) {
                        Log.e(TAG, "onViewCreated", e)
                    }
                }
            }
        } else {
            Glide.with(requireContext())
                .load(arguments?.getInt(KEY_DRAWABLE_ID))
                .into(binding.modalImage)
            binding.modalText.visibility = View.GONE
        }

        handler.postDelayed(hideAppreciateModal, delayHideAppreciateModal)
    }

    private val hideAppreciateModal = Runnable {
        if (!this.isHidden) {
            this.dismiss()
        }
    }

    private fun isTV(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        return uiMode == Configuration.UI_MODE_TYPE_TELEVISION
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        const val KEY_DRAWABLE_ID = "drawable_id"
        const val KEY_URL = "url"
        const val TAG = "ModalFragment"
    }
}