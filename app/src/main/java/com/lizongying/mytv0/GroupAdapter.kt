package com.lizongying.mytv0

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lizongying.mytv0.databinding.GroupItemBinding
import com.lizongying.mytv0.models.TVGroupModel
import com.lizongying.mytv0.models.TVListModel


class GroupAdapter(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private var tvGroupModel: TVGroupModel,
) :
    RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    private var listener: ItemListener? = null
    private var focused: View? = null
    private var defaultFocused = false
    private var defaultFocus: Int = -1

    var visible = false

    private var first = true

    val application = context.applicationContext as MyTVApplication

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = GroupItemBinding.inflate(inflater, parent, false)

        val layoutParams = binding.title.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = application.px2Px(binding.title.marginStart)
        layoutParams.bottomMargin = application.px2Px(binding.title.marginBottom)
        binding.title.layoutParams = layoutParams

        binding.title.textSize = application.px2PxFont(binding.title.textSize)

        binding.root.isFocusable = true
        binding.root.isFocusableInTouchMode = true
        return ViewHolder(context, binding)
    }

    fun focusable(able: Boolean) {
        recyclerView.isFocusable = able
        recyclerView.isFocusableInTouchMode = able
        if (able) {
            recyclerView.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        } else {
            recyclerView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
    }

    fun clear() {
        focused?.clearFocus()
        recyclerView.invalidate()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val listTVModel = tvGroupModel.getTVListModel(position)!!
        val view = viewHolder.itemView

        if (!defaultFocused && position == defaultFocus) {
            view.requestFocus()
            defaultFocused = true
        }

        val onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            listener?.onItemFocusChange(listTVModel, hasFocus)

            if (hasFocus) {
                viewHolder.focus(true)
                focused = view

                val p = listTVModel.getGroupIndex()
                if (p != tvGroupModel.positionValue) {
                    tvGroupModel.setPosition(p)
                }

//                if (visible) {
//
//                    // "position" should not be used here, as the "list" may have been filtered out.
//                    val p = listTVModel.getGroupIndex()
//                    Log.e(TAG, "group getGroupIndex $p")
//                    Log.e(TAG, "group positionValue ${tvGroupModel.positionValue}")
//                    if (p != tvGroupModel.positionValue) {
//                        tvGroupModel.setPosition(p)
//                    }
//                } else {
//                    visible = true
//                }
            } else {
                viewHolder.focus(false)
            }
        }

        view.onFocusChangeListener = onFocusChangeListener

        view.setOnClickListener { _ ->
            listener?.onItemClicked(position)
        }

        view.setOnKeyListener { _, keyCode, event: KeyEvent? ->
            if (event?.action == KeyEvent.ACTION_UP) {
                recyclerView.postDelayed({
                    val oldLikeMode = tvGroupModel.isInLikeMode;
                    tvGroupModel.isInLikeMode = position == 0
                    if (tvGroupModel.isInLikeMode) {
//                        R.string.favorite_mode.showToast()
                    } else if (oldLikeMode) {
//                        R.string.standard_mode.showToast()
                    }
                }, 500)
            }
            if (event?.action == KeyEvent.ACTION_DOWN) {

                // If it is already the first item and you continue to move up...
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) {
                    val p = getItemCount() - 1

                    (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                        p,
                        0
                    )

                    recyclerView.postDelayed({
                        val v = recyclerView.findViewHolderForAdapterPosition(p)
                        v?.itemView?.isSelected = true
                        v?.itemView?.requestFocus()
                    }, 0)
                }

                // If it is the last item and you continue to move down...
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == getItemCount() - 1) {
                    val p = 0

                    (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                        p,
                        0
                    )

                    recyclerView.postDelayed({
                        val v = recyclerView.findViewHolderForAdapterPosition(p)
                        v?.itemView?.isSelected = true
                        v?.itemView?.requestFocus()
                    }, 0)
                }

                return@setOnKeyListener listener?.onKey(keyCode) ?: false
            }
            false
        }

        viewHolder.bindTitle(listTVModel.getName())
    }

    override fun getItemCount() = tvGroupModel.size()

    class ViewHolder(private val context: Context, private val binding: GroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindTitle(text: String) {
            binding.title.text = when (text) {
                "我的收藏" -> context.getString(R.string.my_favorites)
                "全部頻道" -> context.getString(R.string.all_channels)
                else -> text
            }
        }

        fun focus(hasFocus: Boolean) {
            if (hasFocus) {
                binding.title.setTextColor(ContextCompat.getColor(context, R.color.focus))
            } else {
                binding.title.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.title_blur
                    )
                )
            }
        }
    }

    fun scrollToPositionAndSelect(position: Int) {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            val delay = if (first) {
                100L
            } else {
                first = false
                0
            }

            recyclerView.postDelayed({
                val groupPosition =
                    if (SP.showAllChannels || position == 0) position else position - 1
                it.scrollToPositionWithOffset(groupPosition, 0)

                val viewHolder = recyclerView.findViewHolderForAdapterPosition(groupPosition)
                viewHolder?.itemView?.apply {
                    isSelected = true
                    requestFocus()
                }
            }, delay)
        }
    }

    interface ItemListener {
        fun onItemFocusChange(listTVModel: TVListModel, hasFocus: Boolean)
        fun onItemClicked(position: Int)
        fun onKey(keyCode: Int): Boolean
    }

    fun setItemListener(listener: ItemListener) {
        this.listener = listener
    }

    fun changed() {
        recyclerView.post {
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val TAG = "GroupAdapter"
    }
}

