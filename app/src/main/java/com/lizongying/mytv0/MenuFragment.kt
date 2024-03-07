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

class MenuFragment : Fragment(), CategoryAdapter.ItemListener, ListAdapter.ItemListener {
    private var _binding: MenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
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

        categoryAdapter = CategoryAdapter(
            context!!,
            binding.category,
            TVList.categoryModel,
        )
        binding.category.adapter = categoryAdapter
        binding.category.layoutManager =
            LinearLayoutManager(context)
        categoryAdapter.setItemListener(this)

        listAdapter = ListAdapter(
            context!!,
            binding.list,
            TVList.categoryModel.getTVListModel(TVList.categoryModel.position.value!!)!!,
        )
        binding.list.adapter = listAdapter
        binding.list.layoutManager =
            LinearLayoutManager(context)
        listAdapter.focusable(false)
        listAdapter.setItemListener(this)
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
                binding.category.visibility = GONE
                categoryAdapter.focusable(false)
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
                binding.category.visibility = VISIBLE
                categoryAdapter.focusable(true)
                listAdapter.focusable(false)
                listAdapter.clear()
                Log.i(TAG, "category toPosition on left")
                categoryAdapter.toPosition(TVList.categoryModel.position.value!!)
                return true
            }
        }
        return false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            if (binding.list.isVisible) {
//                if (binding.category.isVisible) {
//                    categoryAdapter.focusable(true)
//                    listAdapter.focusable(false)
//                } else {
//                    categoryAdapter.focusable(false)
//                    listAdapter.focusable(true)
//                }
                Log.i(TAG, "list on show toPosition ${listAdapter.tvListModel.position.value!!}")
                listAdapter.toPosition(listAdapter.tvListModel.position.value!!)
            }
            if (binding.category.isVisible) {
//                categoryAdapter.focusable(true)
//                listAdapter.focusable(false)
                Log.i(TAG, "category on show toPosition ${TVList.categoryModel.position.value!!}")
                categoryAdapter.toPosition(TVList.categoryModel.position.value!!)
            }
        } else {
            view?.post {
                categoryAdapter.visiable = false
                listAdapter.visiable = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        categoryAdapter.toPosition(TVList.categoryModel.position.value!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MenuFragment"
    }
}