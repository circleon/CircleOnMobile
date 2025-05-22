package com.developeek.circleon.view.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.domain.model.Identifiable

class CustomAsyncListDiffer<T : Identifiable>(
    private val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
) : AsyncListDiffer<T>(
        adapter,
        object : ItemCallback<T>() {
            override fun areItemsTheSame(
                oldItem: T,
                newItem: T,
            ): Boolean {
                return oldItem.isSame(newItem)
            }

            override fun areContentsTheSame(
                oldItem: T,
                newItem: T,
            ): Boolean {
                return oldItem.areContentsSame(newItem)
            }
        },
    )
