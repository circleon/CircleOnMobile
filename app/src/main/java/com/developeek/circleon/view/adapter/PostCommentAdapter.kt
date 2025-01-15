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
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer
import java.time.format.DateTimeFormatter
import java.util.Locale

class PostCommentAdapter(
    private val activity: Activity,
    private val glideProvider: GlideProvider,
    private val overflowListenerInitializer: ItemListenerInitializer<CommentModel>,
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

    inner class PostCommentAdapterItemViewHolder(
        private val binding: ItemPostCommentBinding,
        private val overflowClickListener: ItemClickListener<CommentModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            val comment = diffUtil.currentList[position]

            loadAuthor(comment)
            loadComment(comment)
            hideOverFlowOrNot(comment)
            notifyListenerItemChanged(comment)
        }

        private fun loadAuthor(comment: CommentModel) {
            binding.txtAuthorName.text = comment.author.name
            comment.author.profileUrl?.let {
                glideProvider.callImage(it, activity, binding.imgAuthorProfile)
            } ?: binding.imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
            binding.txtCreated.text =
                comment.createdAt.format(
                    DateTimeFormatter
                        .ofPattern(CREATED_DATE_FORMAT)
                        .withLocale(Locale.KOREAN),
                )
        }

        private fun loadComment(comment: CommentModel) {
            binding.txtComment.text = comment.content
        }

        private fun hideOverFlowOrNot(comment: CommentModel) {
            binding.btnCommentOverflow.isVisible = (userId != null) && userId == comment.author.id
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
        if (viewType == VIEW_TYPE_LOADING) {
            val binding =
                ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )

            return PostCommentAdapterLoadingViewHolder(binding)
        }

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
                    overflowListenerInitializer.initialize(item, view)
                }
            }
        binding.btnCommentOverflow.setOnClickListener(overflowClickListener)
        return PostCommentAdapterItemViewHolder(binding, overflowClickListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun getItemViewType(position: Int) =
        if (diffUtil.currentList[position] == CommentModel.emptyInstance()) {
            VIEW_TYPE_LOADING
        } else {
            VIEW_TYPE_ITEM
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is PostCommentAdapterItemViewHolder) holder.onBind(position)
    }

    fun update(
        models: CommentModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }

    companion object {
        private const val CREATED_DATE_FORMAT = "M월 d일 a hh:mm"
        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}
