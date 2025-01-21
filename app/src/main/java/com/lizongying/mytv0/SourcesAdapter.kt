package com.lizongying.mytv0

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lizongying.mytv0.databinding.SourcesItemBinding
import com.lizongying.mytv0.models.Sources
import java.util.Locale


class SourcesAdapter(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private var sources: Sources,
) :
    RecyclerView.Adapter<SourcesAdapter.ViewHolder>() {
    private var listener: ItemListener? = null

    val application = context.applicationContext as MyTVApplication

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = SourcesItemBinding.inflate(inflater, parent, false)

        binding.num.layoutParams.width = application.px2Px(binding.num.layoutParams.width)
        binding.num.layoutParams.height = application.px2Px(binding.num.layoutParams.height)
        binding.num.textSize = application.px2PxFont(binding.num.textSize)

        binding.title.layoutParams.width = application.px2Px(binding.title.layoutParams.width)
        binding.title.layoutParams.height = application.px2Px(binding.title.layoutParams.height)
        binding.title.textSize = application.px2PxFont(binding.title.textSize)

        binding.heart.layoutParams.width = application.px2Px(binding.heart.layoutParams.width)
        binding.heart.layoutParams.height = application.px2Px(binding.heart.layoutParams.height)
        binding.heart.setPadding(application.px2Px(binding.heart.paddingTop))

        return ViewHolder(context, binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        sources.let {
            val source = it.getSource(position)!!

            val view = viewHolder.itemView
            view.isFocusable = true
            view.isFocusableInTouchMode = true

            viewHolder.checked(source.checked)

            view.setOnFocusChangeListener { _, hasFocus ->
                listener?.onItemFocusChange(position, hasFocus)

                viewHolder.focus(hasFocus)
            }

            view.setOnClickListener { _ ->
                it.setChecked(position)
                // ui
                check(position)
                listener?.onItemClicked(position)
            }

            view.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(
                    v: View?,
                    event: MotionEvent?
                ): Boolean {
                    v ?: return false
                    event ?: return false

                    when (event.action) {
                        MotionEvent.ACTION_UP -> {
                            v.performClick()
                            return true
                        }
                    }

                    return false
                }
            })

            view.setOnKeyListener { _, keyCode, event: KeyEvent? ->
                if (event?.action == KeyEvent.ACTION_DOWN) {
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

            viewHolder.bindNum(String.format(Locale.getDefault(), "%02d", position))
            viewHolder.bindTitle(source.uri)
        }
    }

    override fun getItemCount() = sources.size()

    class ViewHolder(private val context: Context, val binding: SourcesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindNum(text: String) {
            binding.num.text = text
        }

        fun bindTitle(text: String) {
            binding.title.text = text
        }

        fun focus(hasFocus: Boolean) {
            if (hasFocus) {
                binding.title.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding.root.setBackgroundResource(R.color.focus)
            } else {
                binding.title.setTextColor(ContextCompat.getColor(context, R.color.title_blur))
                binding.root.setBackgroundResource(R.color.blur)
            }
        }

        // show done icon
        fun checked(isChecked: Boolean) {
            binding.heart.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun check(position: Int) {
        recyclerView.post {
            for (i in 0 until getItemCount()) {
                val changed = this.sources.setSourceChecked(i, i == position)
                if (changed) {
                    notifyItemChanged(i)
                }
            }
        }
    }

    fun removed(position: Int) {
        recyclerView.post {
            notifyItemRemoved(position)
        }
    }

    fun added(position: Int) {
        recyclerView.post {
            notifyItemInserted(position)
        }
    }

    fun changed() {
        recyclerView.post {
            notifyDataSetChanged()
        }
    }

    fun toPosition(position: Int) {
        recyclerView.post {
            (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                position,
                0
            )
            recyclerView.postDelayed({
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                viewHolder?.itemView?.isSelected = true
                viewHolder?.itemView?.requestFocus()
            }, 0)
        }
    }

    interface ItemListener {
        fun onItemFocusChange(position: Int, hasFocus: Boolean, tag: String = TAG)
        fun onItemClicked(position: Int, tag: String = TAG)
        fun onKey(keyCode: Int, tag: String = TAG): Boolean
    }

    fun setItemListener(listener: ItemListener) {
        this.listener = listener
    }

    companion object {
        private const val TAG = "SourcesAdapter"
    }
}

