package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemCardCircleBinding
import com.developeek.circleon.databinding.ItemLoadingBinding
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.utils.glide.GlideProvider

class CircleAdapter(
    private val parent: Context,
    private val glideProvider: GlideProvider,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    inner class CircleAdapterItemViewHolder(
        private val binding: ItemCardCircleBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            loadCircle(position)
        }

        private fun loadCircle(position: Int) {
            binding.txtCircleName.text = diffUtil.currentList[position].name
            binding.txtCircleCategory.text = diffUtil.currentList[position].category.categoryName()
            binding.txtCircleComment.text = diffUtil.currentList[position].comment
            binding.txtCirclePeopleCount.text =
                String.format(
                    MEMBER_COUNT_UNIT, diffUtil.currentList[position].member,
                )
            diffUtil.currentList[position].thumbnailUrl?.let {
                glideProvider.callImage(it, parent, binding.imgCircleThumbnail)
            } ?: binding.imgCircleThumbnail.setImageResource(R.drawable.logo_main)
        }
    }

    inner class CircleAdapterLoadingViewHolder(
        private val binding: ItemLoadingBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_LOADING) {
            val binding =
                ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )

            return CircleAdapterLoadingViewHolder(binding)
        }
        val binding =
            ItemCardCircleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        return CircleAdapterItemViewHolder(binding)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun getItemViewType(position: Int) =
        if (diffUtil.currentList[position] == CircleModel.emptyInstance()) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is CircleAdapterItemViewHolder) holder.bind(position)
    }

    fun update(
        models: CircleModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }

    companion object {
        private const val MEMBER_COUNT_UNIT = "%d명"
        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}
