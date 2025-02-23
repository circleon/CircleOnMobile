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
import com.developeek.circleon.databinding.ItemLoadingBinding
import com.developeek.circleon.databinding.ItemPostCommentBinding
import com.developeek.circleon.databinding.ItemPostContentBinding
import com.developeek.circleon.domain.model.AuthorModel
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.Identifiable
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.ItemViewType.*
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer
import java.time.format.DateTimeFormatter
import java.util.Locale

class PostDetailAdapter(
    private val context: Context,
    private val glideProvider: GlideProvider,
    private val postOverflowListenerInitializer: ItemListenerInitializer<PostModel>,
    private val commentOverflowListenerInitializer: ItemListenerInitializer<CommentModel>,
    private val userId: Int?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<Identifiable>() {
                override fun areItemsTheSame(
                    oldItem: Identifiable,
                    newItem: Identifiable,
                ): Boolean {
                    return oldItem.isSame(newItem)
                }

                override fun areContentsTheSame(
                    oldItem: Identifiable,
                    newItem: Identifiable,
                ): Boolean {
                    return oldItem.areContentsSame(newItem)
                }
            },
        )

    inner class PostContentAdapterItemViewHolder(
        private val binding: ItemPostContentBinding,
        private val overflowClickListener: ItemClickListener<PostModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            val post = diffUtil.currentList[position] as PostModel

            loadAuthor(post.author)
            loadContent(post)
            hideOverflowOrNot(post.author)
            notifyListenerItemChanged(post)
        }

        private fun loadAuthor(author: AuthorModel) {
            binding.txtAuthorName.text = author.name
            author.profileUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgAuthorProfile)
            } ?: binding.imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
        }

        private fun loadContent(post: PostModel) {
            binding.txtPostContent.text = post.content
            post.imgUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgPost)
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

        private fun notifyListenerItemChanged(post: PostModel) {
            overflowClickListener.item = post
        }
    }

    inner class PostCommentAdapterItemViewHolder(
        private val binding: ItemPostCommentBinding,
        private val overflowClickListener: ItemClickListener<CommentModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            val comment = diffUtil.currentList[position] as CommentModel

            loadAuthor(comment.author)
            loadComment(comment)
            hideOverFlowOrNot(comment.author)
            notifyListenerItemChanged(comment)
        }

        private fun loadAuthor(author: AuthorModel) {
            binding.txtAuthorName.text = author.name
            author.profileUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgAuthorProfile)
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
        return when (ItemViewType.findOrDefault(viewType)) {
            VIEW_TYPE_POST_CONTENT, VIEW_TYPE_POST_CONTENT_WITH_IMAGE -> {
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
                binding.cvPostImage.isVisible = viewType == VIEW_TYPE_POST_CONTENT_WITH_IMAGE.typeValue
                PostContentAdapterItemViewHolder(binding, overflowClickListener)
            }
            VIEW_TYPE_POST_COMMENT -> {
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
            VIEW_TYPE_LOADING -> {
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

    override fun getItemViewType(position: Int): Int {
        val item = diffUtil.currentList[position]

        return if (item is PostModel) {
            if (item.imgUrl == null) {
                VIEW_TYPE_POST_CONTENT.typeValue
            } else {
                VIEW_TYPE_POST_CONTENT_WITH_IMAGE.typeValue
            }
        } else if (!item.isSame(CommentModel.emptyInstance())) {
            VIEW_TYPE_POST_COMMENT.typeValue
        } else {
            VIEW_TYPE_LOADING.typeValue
        }
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
        models: List<Identifiable>,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models, commitCallback)
    }

    companion object {
        private const val CREATED_DATE_FORMAT = "M월 d일 a hh:mm"
    }
}

enum class ItemViewType(val typeValue: Int) {
    VIEW_TYPE_POST_CONTENT(0),
    VIEW_TYPE_POST_CONTENT_WITH_IMAGE(1),
    VIEW_TYPE_POST_COMMENT(2),
    VIEW_TYPE_LOADING(3), ;

    companion object {
        private val default = VIEW_TYPE_LOADING

        fun findOrDefault(typeValue: Int) = ItemViewType.entries.find { it.typeValue == typeValue } ?: default
    }
}
