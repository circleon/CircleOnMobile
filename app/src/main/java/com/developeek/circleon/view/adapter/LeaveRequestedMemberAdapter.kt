package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemLeaveRequestedMemberBinding
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class LeaveRequestedMemberAdapter(
    private val context: Context,
    private val glideProvider: GlideProvider,
    private val showMessageListenerInitializer: ItemListenerInitializer<MemberModel>,
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

    inner class LeaveRequestedMemberAdapterItemViewHolder(
        private val binding: ItemLeaveRequestedMemberBinding,
        private val showMessageListener: ItemClickListener<MemberModel>,
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
            } ?: binding.imgMemberProfile.setImageResource(R.drawable.ic_user_profile_default)
        }

        private fun notifyListenerItemChanged(member: MemberModel) {
            showMessageListener.item = member
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            ItemLeaveRequestedMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        val showMessageListener =
            object : ItemClickListener<MemberModel> {
                override lateinit var item: MemberModel

                override fun onClick(p0: View?) {
                    showMessageListenerInitializer.initialize(item)
                }
            }
        binding.btnShowMemberMessage.setOnClickListener(showMessageListener)
        return LeaveRequestedMemberAdapterItemViewHolder(binding, showMessageListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        (holder as LeaveRequestedMemberAdapterItemViewHolder).bind(position)
    }

    fun update(
        models: MemberModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }
}
