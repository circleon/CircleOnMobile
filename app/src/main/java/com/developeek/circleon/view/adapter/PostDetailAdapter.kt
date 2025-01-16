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
import com.developeek.circleon.databinding.ItemLoadingBinding
import com.developeek.circleon.databinding.ItemPostCommentBinding
import com.developeek.circleon.databinding.ItemPostContentBinding
import com.developeek.circleon.domain.model.AuthorModel
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.PostDetailViewType.*
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer
import java.time.format.DateTimeFormatter
import java.util.Locale

class PostDetailAdapter(
    private val activity: Activity,
    private val glideProvider: GlideProvider,
    private val post: PostModel,
    private val postOverflowListenerInitializer: ItemListenerInitializer<PostModel>,
    private val commentOverflowListenerInitializer: ItemListenerInitializer<CommentModel>,
    private val userId: Int?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<CommentModel>() {
                override fun areItemsTheSame(
                    oldItem: CommentModel,
                    newItem: CommentModel,
                ): Boolean {
                    return oldItem.isSame(newItem)
                }

                override fun areContentsTheSame(
                    oldItem: CommentModel,
                    newItem: CommentModel,
                ): Boolean {
                    return oldItem.areContentsSame(newItem)
                }
            },
        )

    inner class PostContentAdapterItemViewHolder(
        private val binding: ItemPostContentBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            loadAuthor(post.author)
            loadContent(post)
            hideOverflowOrNot(post.author)
        }

        private fun loadAuthor(author: AuthorModel) {
            binding.txtAuthorName.text = author.name
            author.profileUrl?.let {
                glideProvider.callImage(it, activity, binding.imgAuthorProfile)
            } ?: binding.imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
        }

        private fun loadContent(post: PostModel) {
            binding.txtPostContent.text = post.content
            post.imgUrl?.let {
                glideProvider.callImage(it, activity, binding.imgPost)
            }
            binding.txtCreated.text =
                post.createdAt.format(
                    DateTimeFormatter
                        .ofPattern(CREATED_DATE_FORMAT)
                        .withLocale(Locale.KOREAN),
                )
        }

        private fun hideOverflowOrNot(author: AuthorModel) {
            userId?.let {
                binding.btnPostOverflow.isVisible = author.id == it
            }
        }
    }

    inner class PostCommentAdapterItemViewHolder(
        private val binding: ItemPostCommentBinding,
        private val overflowClickListener: ItemClickListener<CommentModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            val comment = diffUtil.currentList[position]

            loadAuthor(comment.author)
            loadComment(comment)
            hideOverFlowOrNot(comment.author)
            notifyListenerItemChanged(comment)
        }

        private fun loadAuthor(author: AuthorModel) {
            binding.txtAuthorName.text = author.name
            author.profileUrl?.let {
                glideProvider.callImage(it, activity, binding.imgAuthorProfile)
            } ?: binding.imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
        }

        private fun loadComment(comment: CommentModel) {
            binding.txtComment.text = comment.content
            binding.txtCreated.text =
                comment.createdAt.format(
                    DateTimeFormatter
                        .ofPattern(CREATED_DATE_FORMAT)
                        .withLocale(Locale.KOREAN),
                )
        }

        private fun hideOverFlowOrNot(author: AuthorModel) {
            userId?.let {
                binding.btnCommentOverflow.isVisible = author.id == it
            }
        }

        private fun notifyListenerItemChanged(comment: CommentModel) {
            overflowClickListener.item = comment
        }
    }

    inner class PostCommentAdapterLoadingViewHolder(
        private val binding: ItemLoadingBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        return when (PostDetailViewType.findOrDefault(viewType)) {
            VIEW_TYPE_CONTENT, VIEW_TYPE_CONTENT_WITH_IMAGE -> {
                val binding =
                    ItemPostContentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    )
                val overflowClickListener =
                    object : ItemClickListener<PostModel> {
                        override lateinit var item: PostModel

                        override fun onClick(view: View?) {
                            postOverflowListenerInitializer.initialize(item, view)
                        }
                    }
                binding.btnPostOverflow.setOnClickListener(overflowClickListener)
                binding.imgPost.isVisible = viewType == VIEW_TYPE_CONTENT_WITH_IMAGE.typeValue
                PostContentAdapterItemViewHolder(binding)
            }
            VIEW_TYPE_COMMENT -> {
                val binding =
                    ItemPostCommentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    )
                val overflowClickListener =
                    object : ItemClickListener<CommentModel> {
                        override lateinit var item: CommentModel

                        override fun onClick(view: View?) {
                            commentOverflowListenerInitializer.initialize(item, view)
                        }
                    }
                binding.btnCommentOverflow.setOnClickListener(overflowClickListener)
                PostCommentAdapterItemViewHolder(binding, overflowClickListener)
            }
            VIEW_TYPE_COMMENT_LOADING -> {
                val binding =
                    ItemLoadingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false,
                    )

                PostCommentAdapterLoadingViewHolder(binding)
            }
        }
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun getItemViewType(position: Int) =
        if (position == 0) {
            if (post.imgUrl == null) {
                VIEW_TYPE_CONTENT.typeValue
            } else {
                VIEW_TYPE_CONTENT_WITH_IMAGE.typeValue
            }
        } else if (diffUtil.currentList[position] == CommentModel.emptyInstance()) {
            VIEW_TYPE_COMMENT_LOADING.typeValue
        } else {
            VIEW_TYPE_COMMENT.typeValue
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is PostContentAdapterItemViewHolder) {
            holder.onBind(position)
        } else if (holder is PostCommentAdapterItemViewHolder) {
            holder.onBind(position)
        }
    }

    fun update(
        models: CommentModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }

    companion object {
        private const val CREATED_DATE_FORMAT = "M월 d일 a hh:mm"
    }
}

enum class PostDetailViewType(val typeValue: Int) {
    VIEW_TYPE_CONTENT(0),
    VIEW_TYPE_CONTENT_WITH_IMAGE(1),
    VIEW_TYPE_COMMENT_LOADING(2),
    VIEW_TYPE_COMMENT(3),
    ;

    companion object {
        private val default = VIEW_TYPE_COMMENT_LOADING

        fun findOrDefault(typeValue: Int) = PostDetailViewType.entries.find { it.typeValue == typeValue } ?: default
    }
}
