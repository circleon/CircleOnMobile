package com.developeek.circleon.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.databinding.ItemCirclePostBinding
import com.developeek.circleon.view.listener.OverflowClickListener

class CircleNoticeAdapter(
    private val activity: Activity,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class CircleNoticeAdapterItemViewHolder(
        private val binding: ItemCirclePostBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            binding.btnNoticeOverflow.setOnClickListener(
                OverflowClickListener().apply {
                    // TODO: post id 값으로 변경 필요
                    onOverflowSelectedListener(0, activity)
                },
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            ItemCirclePostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        return CircleNoticeAdapterItemViewHolder(binding)
    }

    override fun getItemCount() = 10

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        (holder as CircleNoticeAdapterItemViewHolder).onBind(position)
    }
}
