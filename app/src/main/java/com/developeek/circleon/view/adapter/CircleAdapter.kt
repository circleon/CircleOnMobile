package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemCardCircleBinding
import com.developeek.circleon.databinding.ItemLoadingBinding
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class CircleAdapter(
    private val context: Context,
    private val glideProvider: GlideProvider,
    private val itemListenerInitializer: ItemListenerInitializer<CircleModel>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffUtil = CustomAsyncListDiffer<CircleModel>(this)

    inner class CircleAdapterItemViewHolder(
        private val binding: ItemCardCircleBinding,
        private val itemClickListener: ItemClickListener<CircleModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val circle = diffUtil.currentList[position]

            loadCircle(circle)
            notifyListenerItemChanged(circle)
        }

        private fun loadCircle(circle: CircleModel) {
            binding.txtCircleName.text = circle.name
            binding.txtCircleCategory.text = circle.category.categoryName
            binding.icOfficial.isVisible = circle.isOfficial()
            binding.txtCircleComment.text = circle.comment
            binding.txtCircleMemberCount.text =
                String.format(
                    MEMBER_COUNT_UNIT, circle.memberCount,
                )
            // ?.let ?: 구조인 경우 ?: 뒤에 블록 형태로 코드를 작성하면 실행이 안 되는데
            // 싱글 라인인 경우에는 작동
            circle.thumbnailUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgCircleThumbnail)
            } ?: binding.imgCircleThumbnail.setImageResource(R.drawable.img_circle_thumbnail_default)
        }

        private fun notifyListenerItemChanged(circle: CircleModel) {
            itemClickListener.item = circle
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

        val itemClickListener =
            object : ItemClickListener<CircleModel> {
                override lateinit var item: CircleModel

                override fun onClick(p0: View?) {
                    itemListenerInitializer.initialize(item)
                }
            }
        binding.clItemCircle.setOnClickListener(itemClickListener)
        return CircleAdapterItemViewHolder(binding, itemClickListener)
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
        models: Models<CircleModel>,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }

    fun addLoadingItem() {
        if (diffUtil.currentList.last() == CircleModel.emptyInstance()) return

        diffUtil.submitList(diffUtil.currentList + CircleModel.emptyInstance())
    }

    companion object {
        private const val MEMBER_COUNT_UNIT = "%d명"
        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}
