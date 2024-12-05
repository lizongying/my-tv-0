package com.lizongying.mytv0

import MainViewModel
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lizongying.mytv0.data.Source
import com.lizongying.mytv0.databinding.SourcesBinding


class SourcesFragment : DialogFragment(), SourcesAdapter.ItemListener {
    private var _binding: SourcesBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.myLooper()!!)
    private val delayHideFragment = 10000L

    private lateinit var viewModel: MainViewModel

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
        _binding = SourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val context = requireActivity()
        val application = context.applicationContext as MyTVApplication
        val sourcesAdapter = SourcesAdapter(
            context,
            binding.list,
            viewModel.sources,
        )
        binding.list.adapter = sourcesAdapter
        binding.list.layoutManager =
            LinearLayoutManager(context)
        val listWidth = application.px2Px(binding.list.layoutParams.width)
        binding.list.layoutParams.width = listWidth
        sourcesAdapter.setItemListener(this)
        sourcesAdapter.toPosition(if (viewModel.sources.checkedValue > -1) viewModel.sources.checkedValue else 0)

        handler.postDelayed(hideFragment, delayHideFragment)

        viewModel.sources.removed.observe(this) { p ->
            Log.i(TAG, "sources changed")
            sourcesAdapter.removed(p.first)
        }

        viewModel.sources.added.observe(this) { p ->
            Log.i(TAG, "sources changed")
            sourcesAdapter.added(p.first)
        }
    }

    private val hideFragment = Runnable {
        if (!this.isHidden) {
            this.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        const val TAG = "SourcesFragment"
    }

    override fun onItemFocusChange(source: Source, hasFocus: Boolean) {
//        TODO("Not yet implemented")
    }

    override fun onItemClicked(position: Int, tag: String) {
        viewModel.sources.getSource(position)?.let {
            val uri = Uri.parse(it.uri)
            handler.post {
                viewModel.parseUri(uri)
            }
        }
    }

    override fun onKey(listAdapter: SourcesAdapter, keyCode: Int): Boolean {
        handler.removeCallbacks(hideFragment)
        handler.postDelayed(hideFragment, delayHideFragment)
        return false
    }
}