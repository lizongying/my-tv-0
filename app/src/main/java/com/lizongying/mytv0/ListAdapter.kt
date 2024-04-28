package com.lizongying.mytv0

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.FOCUS_BEFORE_DESCENDANTS
import android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lizongying.mytv0.databinding.ListItemBinding
import com.lizongying.mytv0.models.TVListModel
import com.lizongying.mytv0.models.TVModel


class ListAdapter(
    private val context: Context,
    private val recyclerView: RecyclerView,
    var tvListModel: TVListModel,
) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    private var listener: ItemListener? = null
    private var focused: View? = null
    private var defaultFocused = false
    var defaultFocus: Int = -1

    var visiable = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(context, binding)
    }

    fun focusable(able: Boolean) {
        recyclerView.isFocusable = able
        recyclerView.isFocusableInTouchMode = able
        if (able) {
            recyclerView.descendantFocusability = FOCUS_BEFORE_DESCENDANTS
        } else {
            recyclerView.descendantFocusability = FOCUS_BLOCK_DESCENDANTS
        }
    }

    fun update(tvListModel: TVListModel) {
        this.tvListModel = tvListModel
        recyclerView.post {
            notifyDataSetChanged()
        }
    }

    fun clear() {
        focused?.clearFocus()
        recyclerView.invalidate()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val tvModel = tvListModel.getTVModel(position)!!
        val view = viewHolder.itemView

        view.isFocusable = true
        view.isFocusableInTouchMode = true
//        view.alpha = 0.8F

        if (!defaultFocused && position == defaultFocus) {
            view.requestFocus()
            defaultFocused = true
        }

        val onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            listener?.onItemFocusChange(tvModel, hasFocus)

            if (hasFocus) {
                viewHolder.focus(true)
                focused = view
                if (visiable) {
                    if (position != tvListModel.position.value) {
                        tvListModel.setPosition(position)
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
            listener?.onItemClicked(tvModel)
        }

        view.setOnKeyListener { _, keyCode, event: KeyEvent? ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) {
                    recyclerView.smoothScrollToPosition(getItemCount() - 1)
                }

                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == getItemCount() - 1) {
                    recyclerView.smoothScrollToPosition(0)
                }

                return@setOnKeyListener listener?.onKey(this, keyCode) ?: false
            }
            false
        }

        viewHolder.bindText(tvModel.tv.title)

        viewHolder.bindImage(tvModel.tv.logo, tvModel.tv.id)
    }

    override fun getItemCount() = tvListModel.size()

    class ViewHolder(private val context: Context, private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindText(text: String) {
            binding.textView.text = text
        }

        fun bindImage(url: String?, id: Int) {
            if (url.isNullOrBlank()) {
                val width = Utils.dpToPx(40)
                val height = Utils.dpToPx(40)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                val paint = Paint().apply {
                    color = Color.WHITE
                    textSize = 32f
                    textAlign = Paint.Align.CENTER
                }
                val text = String.format("%3d", id + 1)
                val x = width / 2f
                val y = height / 2f - (paint.descent() + paint.ascent()) / 2
                canvas.drawText(text, x, y, paint)
                Glide.with(context)
                    .load(BitmapDrawable(context.resources, bitmap))
                    .centerInside()
                    .into(binding.imageView)
//                binding.imageView.setImageDrawable(null)
            } else {
                Glide.with(context)
                    .load(url)
                    .centerInside()
//                    .error(BitmapDrawable(context.resources, bitmap))
                    .into(binding.imageView)
            }
        }

        fun focus(hasFocus: Boolean) {
            if (hasFocus) {
                binding.textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.description.setTextColor(ContextCompat.getColor(context, R.color.white))
//                binding.root.alpha = 1.0F
                binding.root.setBackgroundResource(R.color.focus)
            } else {
                binding.textView.setTextColor(ContextCompat.getColor(context, R.color.title_blur))
                binding.description.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.description_blur
                    )
                )
//                binding.root.alpha = 0.8F
                binding.root.setBackgroundResource(R.color.blur)
            }
        }
    }

    fun toPosition(position: Int) {
        recyclerView.post {
            recyclerView.scrollToPosition(position)
            recyclerView.getChildAt(position)?.isSelected
            recyclerView.getChildAt(position)?.requestFocus()
        }
    }

    interface ItemListener {
        fun onItemFocusChange(tvModel: TVModel, hasFocus: Boolean)
        fun onItemClicked(tvModel: TVModel)
        fun onKey(listAdapter: ListAdapter, keyCode: Int): Boolean
    }

    fun setItemListener(listener: ItemListener) {
        this.listener = listener
    }

    companion object {
        private const val TAG = "ListAdapter"
    }
}

