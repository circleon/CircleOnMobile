package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemJoinRequestedMemberBinding
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class JoinRequestedMemberAdapter(
    private val context: Context,
    private val glideProvider: GlideProvider,
    private val positiveListenerInitializer: ItemListenerInitializer<MemberModel>,
    private val negativeListenerInitializer: ItemListenerInitializer<MemberModel>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<MemberModel>() {
                override fun areItemsTheSame(
                    oldItem: MemberModel,
                    newItem: MemberModel,
                ): Boolean {
                    return oldItem.isSame(newItem)
                }

                override fun areContentsTheSame(
                    oldItem: MemberModel,
                    newItem: MemberModel,
                ): Boolean {
                    return oldItem.areContentsSame(newItem)
                }
            },
        )

    inner class JoinRequestedMemberAdapterItemViewHolder(
        private val binding: ItemJoinRequestedMemberBinding,
        private val positiveListener: ItemClickListener<MemberModel>,
        private val negativeListener: ItemClickListener<MemberModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val member = diffUtil.currentList[position]

            load(member)
            notifyListenerItemChanged(member)
        }

        private fun load(member: MemberModel) {
            binding.txtMemberName.text = member.name
            member.profileImgUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgMemberProfile)
            } ?: binding.imgMemberProfile.setImageResource(R.drawable.img_user_profile_default)
        }

        private fun notifyListenerItemChanged(member: MemberModel) {
            positiveListener.item = member
            negativeListener.item = member
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            ItemJoinRequestedMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        val positiveListener =
            object : ItemClickListener<MemberModel> {
                override lateinit var item: MemberModel

                override fun onClick(p0: View?) {
                    positiveListenerInitializer.initialize(item)
                }
            }
        val negativeListener =
            object : ItemClickListener<MemberModel> {
                override lateinit var item: MemberModel

                override fun onClick(p0: View?) {
                    negativeListenerInitializer.initialize(item)
                }
            }
        binding.btnAcceptJoinRequest.setOnClickListener(positiveListener)
        binding.btnRejectJoinRequest.setOnClickListener(negativeListener)
        return JoinRequestedMemberAdapterItemViewHolder(binding, positiveListener, negativeListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        (holder as JoinRequestedMemberAdapterItemViewHolder).bind(position)
    }

    fun update(
        models: MemberModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }
}
