package com.lizongying.mytv0

import MainViewModel
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lizongying.mytv0.databinding.MenuBinding
import com.lizongying.mytv0.models.TVListModel
import com.lizongying.mytv0.models.TVModel

class MenuFragment : Fragment(), GroupAdapter.ItemListener, ListAdapter.ItemListener {
    private var _binding: MenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupAdapter: GroupAdapter
    private lateinit var listAdapter: ListAdapter

    private var groupWidth = 0
    private var listWidth = 0

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = MenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireActivity()
        val application = context.applicationContext as MyTVApplication
        viewModel = ViewModelProvider(context)[MainViewModel::class.java]

        Log.i(TAG, "group size ${viewModel.groupModel.size()}")
        groupAdapter = GroupAdapter(
            context,
            binding.group,
            viewModel.groupModel,
        )
        binding.group.adapter = groupAdapter
        binding.group.layoutManager =
            LinearLayoutManager(context)
        groupWidth = application.px2Px(binding.group.layoutParams.width)
        binding.group.layoutParams.width = if (SP.compactMenu) {
            groupWidth * 2 / 3
        } else {
            groupWidth
        }
        groupAdapter.setItemListener(this)

        listAdapter = ListAdapter(
            context,
            binding.list,
            getList(),
        )
        binding.list.adapter = listAdapter
        binding.list.layoutManager =
            LinearLayoutManager(context)
        listWidth = application.px2Px(binding.list.layoutParams.width)
        binding.list.layoutParams.width = if (SP.compactMenu) {
            listWidth * 4 / 5
        } else {
            listWidth
        }
        listAdapter.focusable(false)
        listAdapter.setItemListener(this)

        binding.menu.setOnClickListener {
            hideSelf()
        }
    }

    private fun getList(): TVListModel? {
        if (!this::viewModel.isInitialized) {
            Log.e(TAG, "viewModel is not initialized")
            return null
        }

        // 如果不存在當前組，則切換到收藏組
        if (viewModel.groupModel.getCurrentList() == null) {
            viewModel.groupModel.setPosition(0)
        }

        return viewModel.groupModel.getCurrentList()
    }

    fun update() {
        view?.post {
            groupAdapter.changed()

            getList()?.let {
                (binding.list.adapter as ListAdapter).update(it)
            }
        }
    }

    fun updateSize() {
        view?.post {
            binding.group.layoutParams.width = if (SP.compactMenu) {
                groupWidth * 2 / 3
            } else {
                groupWidth
            }

            binding.list.layoutParams.width = if (SP.compactMenu) {
                listWidth * 4 / 5
            } else {
                listWidth
            }
        }
    }

    fun updateList(position: Int) {
        if (!this::viewModel.isInitialized) {
            Log.e(TAG, "viewModel is not initialized")
            return
        }

        viewModel.groupModel.setPosition(position)
        SP.positionGroup = position

        viewModel.groupModel.getCurrentList()?.let {
            (binding.list.adapter as ListAdapter).update(it)
        }
    }

    private fun hideSelf() {
        requireActivity().supportFragmentManager.beginTransaction()
            .hide(this)
            .commitAllowingStateLoss()
    }

    override fun onItemFocusChange(listTVModel: TVListModel, hasFocus: Boolean) {
        if (hasFocus) {
            (binding.list.adapter as ListAdapter).update(listTVModel)
            (activity as MainActivity).menuActive()
        }
    }

    override fun onItemFocusChange(tvModel: TVModel, hasFocus: Boolean) {
        if (hasFocus) {
            (activity as MainActivity).menuActive()
        }
    }

    override fun onItemClicked(position: Int) {
//        Log.i(TAG, "onItemClicked ${tvModel.tv.id} ${tvModel.tv.title}")
//        TVList.setPosition(tvModel.tv.id)
//        (activity as MainActivity).hideMenuFragment()
    }

    override fun onItemClicked(position: Int, type: String) {
        if (!this::viewModel.isInitialized) {
            Log.e(TAG, "viewModel is not initialized")
            return
        }

        viewModel.groupModel.setPositionPlaying()
        viewModel.groupModel.getCurrentList()?.let {
            it.setPosition(position)
            it.setPositionPlaying()
            it.getCurrent()?.setReady()
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .hide(this)
            .commitAllowingStateLoss()
    }

    override fun onKey(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (listAdapter.itemCount == 0) {
                    R.string.channel_not_exist.showToast()
                    return true
                }
                binding.group.visibility = GONE
                groupAdapter.focusable(false)
                listAdapter.focusable(true)

                if (viewModel.groupModel.positionPlayingValue == viewModel.groupModel.positionValue) {
                    viewModel.groupModel.getCurrentList()?.let {
                        listAdapter.toPosition(it.positionPlayingValue)
                    }
                } else {
                    listAdapter.toPosition(0)
                }

                return true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
//                (activity as MainActivity).hideMenuFragment()
                return true
            }
        }
        return false
    }

    override fun onKey(listAdapter: ListAdapter, keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                binding.group.visibility = VISIBLE
                groupAdapter.focusable(true)
                listAdapter.focusable(false)
                listAdapter.clear()
                groupAdapter.scrollToPositionAndSelect(viewModel.groupModel.positionValue)
                return true
            }
//            KeyEvent.KEYCODE_DPAD_RIGHT -> {
//                binding.group.visibility = VISIBLE
//                groupAdapter.focusable(true)
//                listAdapter.focusable(false)
//                listAdapter.clear()
//                Log.i(TAG, "group toPosition on left")
//                groupAdapter.toPosition(TVList.groupModel.positionValue)
//                return true
//            }
        }
        return false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            if (binding.list.isVisible) {
//                if (binding.group.isVisible) {
//                    groupAdapter.focusable(true)
//                    listAdapter.focusable(false)
//                } else {
//                    groupAdapter.focusable(false)
//                    listAdapter.focusable(true)
//                }

                if (viewModel.groupModel.tvGroupValue.size < 2 || viewModel.groupModel.getAllList()
                        ?.size() == 0
                ) {
                    R.string.channel_not_exist.showToast()
                    return
                }

                val position = viewModel.groupModel.positionPlayingValue
                if (position != viewModel.groupModel.positionValue
                ) {
                    updateList(position)
                }
                viewModel.groupModel.getCurrentList()?.let {
                    listAdapter.toPosition(it.positionPlayingValue)
                }
            }
            if (binding.group.isVisible) {
//                groupAdapter.focusable(true)
//                listAdapter.focusable(false)

                val position = viewModel.groupModel.positionPlayingValue
                if (position >= viewModel.groupModel.tvGroupValue.size) {
                    viewModel.groupModel.setPositionPlaying(viewModel.groupModel.defaultPosition())
                }
                Log.i(
                    TAG,
                    "group position ${viewModel.groupModel.positionPlayingValue}/${viewModel.groupModel.tvGroupValue.size - 1}"
                )
                if (viewModel.groupModel.positionPlayingValue != viewModel.groupModel.positionValue) {
                    viewModel.groupModel.setPosition(position)
                }
                groupAdapter.scrollToPositionAndSelect(position)
            }
            (activity as MainActivity).menuActive()
        } else {
            view?.post {
                groupAdapter.visiable = false
                listAdapter.visiable = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MenuFragment"
    }
}