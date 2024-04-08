package com.lizongying.mytv0

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.lizongying.mytv0.databinding.MenuBinding
import com.lizongying.mytv0.models.TVList
import com.lizongying.mytv0.models.TVListModel
import com.lizongying.mytv0.models.TVModel

class MenuFragment : Fragment(), GroupAdapter.ItemListener, ListAdapter.ItemListener {
    private var _binding: MenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupAdapter: GroupAdapter
    private lateinit var listAdapter: ListAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuBinding.inflate(inflater, container, false)

        groupAdapter = GroupAdapter(
            context!!,
            binding.group,
            TVList.groupModel,
        )
        binding.group.adapter = groupAdapter
        binding.group.layoutManager =
            LinearLayoutManager(context)
        groupAdapter.setItemListener(this)

        var tvListModel = TVList.groupModel.getTVListModel(TVList.groupModel.position.value!!)
        if (tvListModel == null) {
            TVList.groupModel.setPosition(0)
        }

        tvListModel = TVList.groupModel.getTVListModel(TVList.groupModel.position.value!!)

        listAdapter = ListAdapter(
            context!!,
            binding.list,
            tvListModel!!,
        )
        binding.list.adapter = listAdapter
        binding.list.layoutManager =
            LinearLayoutManager(context)
        listAdapter.focusable(false)
        listAdapter.setItemListener(this)

        TVList.groupModel.tvGroupModel.observe(viewLifecycleOwner) { _ ->

            // not first time
            if (TVList.groupModel.tvGroupModel.value != null) {
                groupAdapter.update(TVList.groupModel)
            }
        }

        return binding.root
    }

    override fun onItemFocusChange(tvListModel: TVListModel, hasFocus: Boolean) {
        if (hasFocus) {
            (binding.list.adapter as ListAdapter).update(tvListModel)
            (activity as MainActivity).menuActive()
        }
    }

    override fun onItemClicked(tvListModel: TVListModel) {
    }

    override fun onItemFocusChange(tvModel: TVModel, hasFocus: Boolean) {
        if (hasFocus) {
            (activity as MainActivity).menuActive()
        }
    }

    override fun onItemClicked(tvModel: TVModel) {
        TVList.setPosition(tvModel.tv.id)
        (activity as MainActivity).hideMenuFragment()
    }

    override fun onKey(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (listAdapter.itemCount == 0) {
                    Toast.makeText(context, "暂无频道", Toast.LENGTH_LONG).show()
                    return true
                }
                binding.group.visibility = GONE
                groupAdapter.focusable(false)
                listAdapter.focusable(true)
                listAdapter.toPosition(listAdapter.tvListModel.position.value!!)
                return true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                (activity as MainActivity).hideMenuFragment()
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
                Log.i(TAG, "group toPosition on left")
                groupAdapter.toPosition(TVList.groupModel.position.value!!)
                return true
            }
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
                Log.i(TAG, "list on show toPosition ${listAdapter.tvListModel.position.value!!}")
                listAdapter.toPosition(listAdapter.tvListModel.position.value!!)
            }
            if (binding.group.isVisible) {
//                groupAdapter.focusable(true)
//                listAdapter.focusable(false)
                Log.i(TAG, "group on show toPosition ${TVList.groupModel.position.value!!}")
                groupAdapter.toPosition(TVList.groupModel.position.value!!)
            }
        } else {
            view?.post {
                groupAdapter.visiable = false
                listAdapter.visiable = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        groupAdapter.toPosition(TVList.groupModel.position.value!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MenuFragment"
    }
}