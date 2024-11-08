package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemCardCircleBinding
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.viewmodel.HomeViewModel

class CircleAdapter(
    private val viewModel: HomeViewModel,
    private val parent: Context,
    private val glideProvider: GlideProvider,
) : RecyclerView.Adapter<CircleAdapter.CircleAdapterViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<CircleModel>() {
                override fun areItemsTheSame(
                    oldItem: CircleModel,
                    newItem: CircleModel,
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: CircleModel,
                    newItem: CircleModel,
                ): Boolean {
                    return oldItem.id == newItem.id
                }
            },
        )

    inner class CircleAdapterViewHolder(
        private val binding: ItemCardCircleBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            loadCircle(position)
        }

        private fun loadCircle(position: Int) {
            binding.txtCircleName.text = diffUtil.currentList[position].name
            binding.txtCircleCategory.text = diffUtil.currentList[position].category.categoryName()
            binding.txtCirclePeopleCount.text =
                String.format(
                    MEMBER_COUNT_UNIT, diffUtil.currentList[position].member,
                )
            diffUtil.currentList[position].thumbnailUrl?.let {
                glideProvider.callImage(it, parent, binding.imgCircleThumbnail)
            } ?: binding.imgCircleThumbnail.setImageResource(R.drawable.logo_main)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CircleAdapterViewHolder {
        val binding =
            ItemCardCircleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        return CircleAdapterViewHolder(binding)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun onBindViewHolder(
        holder: CircleAdapterViewHolder,
        position: Int,
    ) {
        holder.bind(position)
    }

    fun update(commitCallback: Runnable) {
        diffUtil.submitList(viewModel.circles.get(), commitCallback)
    }

    companion object {
        private const val MEMBER_COUNT_UNIT = "%d명"
    }
}
