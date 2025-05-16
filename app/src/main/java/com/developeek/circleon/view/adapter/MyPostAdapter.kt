package com.developeek.circleon.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.databinding.ItemCardMyPostBinding
import com.developeek.circleon.databinding.ItemLoadingBinding
import com.developeek.circleon.domain.model.MyPostModel
import com.developeek.circleon.domain.model.MyPostModels
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer
import java.time.format.DateTimeFormatter
import java.util.Locale

class MyPostAdapter(
    private val itemListenerInitializer: ItemListenerInitializer<MyPostModel>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<MyPostModel>() {
                override fun areItemsTheSame(
                    oldItem: MyPostModel,
                    newItem: MyPostModel,
                ): Boolean {
                    return oldItem.isSame(newItem)
                }

                override fun areContentsTheSame(
                    oldItem: MyPostModel,
                    newItem: MyPostModel,
                ): Boolean {
                    return oldItem.areContentsSame(newItem)
                }
            },
        )

    inner class MyPostAdapterItemViewHolder(
        private val binding: ItemCardMyPostBinding,
        private val itemClickListener: ItemClickListener<MyPostModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val myPost = diffUtil.currentList[position]

            loadPost(myPost)
            notifyItemListenerChanged(myPost)
        }

        private fun loadPost(myPost: MyPostModel) {
            binding.txtCircleName.text = myPost.circleName
            binding.txtCreated.text =
                myPost.post.createdAt.format(
                    DateTimeFormatter
                        .ofPattern(CREATED_DATE_FORMAT)
                        .withLocale(Locale.KOREAN),
                )
            binding.txtContent.text = myPost.post.content
        }

        private fun notifyItemListenerChanged(post: MyPostModel) {
            itemClickListener.item = post
        }
    }

    inner class MyPostAdapterLoadingViewHolder(
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

            return MyPostAdapterLoadingViewHolder(binding)
        }
        val binding =
            ItemCardMyPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        val itemClickListener =
            object : ItemClickListener<MyPostModel> {
                override lateinit var item: MyPostModel

                override fun onClick(p0: View?) {
                    itemListenerInitializer.initialize(item)
                }
            }
        binding.llItemMyPost.setOnClickListener(itemClickListener)
        return MyPostAdapterItemViewHolder(binding, itemClickListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun getItemViewType(position: Int) =
        if (diffUtil.currentList[position] == MyPostModel.emptyInstance()) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is MyPostAdapterItemViewHolder) holder.bind(position)
    }

    fun update(
        models: MyPostModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }

    fun addLoadingItem() {
        if (diffUtil.currentList.last() == MyPostModel.emptyInstance()) return

        diffUtil.submitList(diffUtil.currentList + MyPostModel.emptyInstance())
    }

    companion object {
        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_ITEM = 1
        private const val CREATED_DATE_FORMAT = "M월 d일"
    }
}
