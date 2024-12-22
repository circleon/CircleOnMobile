package com.developeek.circleon.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemCirclePostBinding
import com.developeek.circleon.databinding.ItemLoadingBinding
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.model.PostModels
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.listener.OverflowClickListener
import java.time.format.DateTimeFormatter

class CirclePostAdapter(
    private val activity: Activity,
    private val glideProvider: GlideProvider,
    private val itemListenerInitializer: ItemListenerInitializer<PostModel>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<PostModel>() {
                override fun areItemsTheSame(
                    oldItem: PostModel,
                    newItem: PostModel,
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: PostModel,
                    newItem: PostModel,
                ): Boolean {
                    return oldItem.id == newItem.id
                }
            },
        )

    /**
     * CirclePostAdapterItemViewHolder
     *
     * 아이템 리스너의 경우 뷰홀더를 만들 때 설정을 하는 것이 올바르지만, 리스너에서 아이템 각각의 데이터를 필요로하는 경우
     * onCreateViewHolder 가 아닌 onBind 에서 리스너가 계속 재설정되는 비효율적 방식을 개선하기 위해
     *
     * 1. 커스텀 리스너 객체를 뷰홀더에서 저장
     * 2. 커스텀 리스너는 아이템 id 값만 갱신하는 별도 기능을 통해 onBind 에서 아이템 내용이 바뀌었을 때 식별값만 갱신
     */
    inner class CirclePostAdapterItemViewHolder(
        private val binding: ItemCirclePostBinding,
        private val overflowClickListener: OverflowClickListener,
        private val itemClickListener: ItemClickListener<PostModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            loadAuthor(position)
            loadPost(position)
            // TODO: post id 로 수정 필요
            overflowClickListener.onBind(0)
            notifyListenerItemChanged(position)
        }

        private fun loadAuthor(position: Int) {
            binding.txtAuthorName.text = diffUtil.currentList[position].author.name
            binding.txtCreated.text =
                diffUtil.currentList[position].createdAt.format(
                    DateTimeFormatter.ofPattern(CREATED_DATE_FORMAT),
                )
            diffUtil.currentList[position].author.profileUrl?.let {
                glideProvider.callImage(it, activity, binding.imgAuthorProfile)
            } ?: binding.imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
        }

        private fun loadPost(position: Int) {
            binding.txtNoticeContent.text = diffUtil.currentList[position].content
            binding.txtCommentCount.text =
                String.format(
                    COMMENT_COUNT_UNIT,
                    diffUtil.currentList[position].commentCount,
                )
            diffUtil.currentList[position].postImgUrl?.let {
                glideProvider.callImage(it, activity, binding.imgPost)
            }
        }

        private fun notifyListenerItemChanged(position: Int) {
            itemClickListener.item = diffUtil.currentList[position]
        }
    }

    inner class CirclePostAdapterLoadingViewHolder(
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

            return CirclePostAdapterLoadingViewHolder(binding)
        }

        val binding =
            ItemCirclePostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        val overflowClickListener = OverflowClickListener(activity)
        binding.btnNoticeOverflow.setOnClickListener(overflowClickListener)
        val itemClickListener =
            object : ItemClickListener<PostModel> {
                override lateinit var item: PostModel

                override fun onClick(p0: View?) {
                    itemListenerInitializer.initialize(item)
                }
            }
        binding.imgPost.isVisible = viewType == VIEW_TYPE_ITEM_WITH_IMAGE
        return CirclePostAdapterItemViewHolder(binding, overflowClickListener, itemClickListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun getItemViewType(position: Int) =
        if (diffUtil.currentList[position] == PostModel.emptyInstance()) {
            VIEW_TYPE_LOADING
        } else if (diffUtil.currentList[position].postImgUrl == null) {
            VIEW_TYPE_ITEM
        } else {
            VIEW_TYPE_ITEM_WITH_IMAGE
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is CirclePostAdapterItemViewHolder) holder.onBind(position)
    }

    fun update(
        models: PostModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }

    companion object {
        private const val COMMENT_COUNT_UNIT = "%d개"
        private const val CREATED_DATE_FORMAT = "M월 d일 hh:mm"
        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_ITEM_WITH_IMAGE = 2
    }
}
