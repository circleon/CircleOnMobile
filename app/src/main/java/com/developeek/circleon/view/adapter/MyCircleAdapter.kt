package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemCardCircleSummaryBinding
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class MyCircleAdapter(
    private val context: Context,
    private val glideProvider: GlideProvider,
    private val circles: CircleSummaryModels,
    private val itemListenerInitializer: ItemListenerInitializer<CircleSummaryModel>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class MyCircleAdapterItemViewHolder(
        private val binding: ItemCardCircleSummaryBinding,
        private val itemClickListener: ItemClickListener<CircleSummaryModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val circle = circles.get(position)

            loadCircle(circle)
            notifyListenerItemChanged(circle)
        }

        private fun loadCircle(circle: CircleSummaryModel) {
            binding.txtCircleName.text = circle.name
            binding.txtCircleCategory.text = circle.category.categoryName()
            circle.thumbnailUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgCircleThumbnail)
            } ?: binding.imgCircleThumbnail.setImageResource(R.drawable.ic_circle_thumbnail_default)
        }

        private fun notifyListenerItemChanged(circle: CircleSummaryModel) {
            itemClickListener.item = circle
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
        return MyCircleAdapterItemViewHolder(binding, itemClickListener)
    }

    override fun getItemCount() = circles.size()

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is MyCircleAdapterItemViewHolder) holder.bind(position)
    }
}
