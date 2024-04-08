package com.lizongying.mytv0

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
    var defaultFocus: Int = -1

    var visiable = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = GroupItemBinding.inflate(inflater, parent, false)
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
        val tvListModel = tvGroupModel.getTVListModel(position)!!
        val view = viewHolder.itemView
        view.tag = position

        if (!defaultFocused && position == defaultFocus) {
            view.requestFocus()
            defaultFocused = true
        }

        val onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            listener?.onItemFocusChange(tvListModel, hasFocus)

            if (hasFocus) {
                viewHolder.focus(true)
                focused = view
                if (visiable) {
                    if (position != tvGroupModel.position.value) {
                        tvGroupModel.setPosition(position)
                    }
                } else {
                    visiable = true
                }
            } else {
                viewHolder.focus(false)
            }
        }

        view.onFocusChangeListener = onFocusChangeListener

        view.setOnClickListener { _ ->
            listener?.onItemClicked(tvListModel)
        }

        view.setOnKeyListener { _, keyCode, event: KeyEvent? ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) {
                    recyclerView.smoothScrollToPosition(getItemCount() - 1)
                }

                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == getItemCount() - 1) {
                    recyclerView.smoothScrollToPosition(0)
                }

                return@setOnKeyListener listener?.onKey(keyCode) ?: false
            }
            false
        }

        viewHolder.bind(tvListModel.getName())
    }

    override fun getItemCount() = tvGroupModel.size()

    class ViewHolder(private val context: Context, private val binding: GroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.textView.text = text
        }

        fun focus(hasFocus: Boolean) {
            if (hasFocus) {
                binding.textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.textView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.description_blur
                    )
                )
            }
        }
    }

    fun toPosition(position: Int) {
        recyclerView.post {
            Log.i(TAG, "group smoothScrollToPosition $position")
            recyclerView.scrollToPosition(position)
            recyclerView.getChildAt(position)?.isSelected
            recyclerView.getChildAt(position)?.requestFocus()
        }
    }

    interface ItemListener {
        fun onItemFocusChange(tvListModel: TVListModel, hasFocus: Boolean)
        fun onItemClicked(tvListModel: TVListModel)
        fun onKey(keyCode: Int): Boolean
    }

    fun setItemListener(listener: ItemListener) {
        this.listener = listener
    }

    fun update(tvGroupModel: TVGroupModel) {
        this.tvGroupModel = tvGroupModel
        notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "CategoryAdapter"
    }
}

