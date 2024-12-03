package com.developeek.circleon.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.databinding.ItemCirclePostBinding

class CircleNoticeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class CircleNoticeAdapterItemViewHolder(
        private val binding: ItemCirclePostBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
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
        // TODO("Not yet implemented")
    }
}
