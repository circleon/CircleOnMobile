package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemCardCircleSummaryBinding
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class MyCircleAdapter(
    private val context: Context,
    private val glideProvider: GlideProvider,
    private val membershipStatus: MembershipStatus,
    private val itemListenerInitializer: ItemListenerInitializer<CircleSummaryModel>,
    private val overflowListenerInitializer: ItemListenerInitializer<CircleSummaryModel>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<CircleSummaryModel>() {
                override fun areItemsTheSame(
                    oldItem: CircleSummaryModel,
                    newItem: CircleSummaryModel,
                ): Boolean {
                    return oldItem.isSame(newItem)
                }

                override fun areContentsTheSame(
                    oldItem: CircleSummaryModel,
                    newItem: CircleSummaryModel,
                ): Boolean {
                    return oldItem.areContentsSame(newItem)
                }
            },
        )

    inner class MyCircleAdapterItemViewHolder(
        private val binding: ItemCardCircleSummaryBinding,
        private val itemClickListener: ItemClickListener<CircleSummaryModel>,
        private val overflowClickListener: ItemClickListener<CircleSummaryModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val circle = diffUtil.currentList[position]

            loadCircle(circle)
            hideOverflowOrNot()
            notifyListenerItemChanged(circle)
        }

        private fun loadCircle(circle: CircleSummaryModel) {
            binding.txtCircleName.text = circle.name
            binding.txtCircleCategory.text = circle.category.categoryName()
            circle.thumbnailUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgCircleThumbnail)
            } ?: binding.imgCircleThumbnail.setImageResource(R.drawable.img_circle_profile_default)
        }

        private fun hideOverflowOrNot() {
            if (membershipStatus.isJoinRequested()) {
                binding.btnCancel.isVisible = true
            }
        }

        private fun notifyListenerItemChanged(circle: CircleSummaryModel) {
            itemClickListener.item = circle
            overflowClickListener.item = circle
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            ItemCardCircleSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        val itemClickListener =
            object : ItemClickListener<CircleSummaryModel> {
                override lateinit var item: CircleSummaryModel

                override fun onClick(p0: View?) {
                    itemListenerInitializer.initialize(item)
                }
            }
        binding.clItemCircleSummary.setOnClickListener(itemClickListener)
        val overflowClickListener =
            object : ItemClickListener<CircleSummaryModel> {
                override lateinit var item: CircleSummaryModel

                override fun onClick(p0: View?) {
                    overflowListenerInitializer.initialize(item)
                }
            }
        binding.btnCancel.setOnClickListener(overflowClickListener)
        return MyCircleAdapterItemViewHolder(binding, itemClickListener, overflowClickListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is MyCircleAdapterItemViewHolder) holder.bind(position)
    }

    fun update(
        models: CircleSummaryModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }
}
