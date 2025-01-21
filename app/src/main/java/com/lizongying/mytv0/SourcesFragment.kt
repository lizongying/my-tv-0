package com.lizongying.mytv0

import MainViewModel
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

        viewModel.sources.removed.observe(this) { items ->
            sourcesAdapter.removed(items.first)
            checkEmpty()
        }

        viewModel.sources.added.observe(this) { items ->
            sourcesAdapter.added(items.first)
        }

        viewModel.sources.changed.observe(this) { _ ->
            sourcesAdapter.changed()
            checkEmpty()
        }

        checkEmpty()
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

    override fun onItemFocusChange(position: Int, hasFocus: Boolean, tag: String) {
    }

    override fun onItemClicked(position: Int, tag: String) {
        viewModel.sources.getSource(position)?.let {
            val uri = Uri.parse(it.uri)
            handler.post {
                viewModel.importFromUri(uri)
            }
        }

        handler.postDelayed(hideFragment, 0)
    }

    override fun onKey(keyCode: Int, tag: String): Boolean {
        handler.removeCallbacks(hideFragment)
        handler.postDelayed(hideFragment, delayHideFragment)
        return false
    }

    private fun checkEmpty() {
        if (viewModel.sources.size() == 0) {
            binding.content.visibility = View.VISIBLE
        } else {
            binding.content.visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "SourcesFragment"
    }
}