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
import com.developeek.circleon.databinding.ItemCircleMemberBinding
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.MemberModel
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class CircleMemberAdapter(
    private val context: Context,
    private val glideProvider: GlideProvider,
    private val userRole: Role,
    private val userMemberId: Int? = null,
    private val overflowListenerInitializer: ItemListenerInitializer<MemberModel>,
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

    inner class CircleMemberAdapterItemViewHolder(
        private val binding: ItemCircleMemberBinding,
        private val overflowClickListener: ItemClickListener<MemberModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val member = diffUtil.currentList[position]

            load(member)
            hideOverflowOrNot(member)
        }

        private fun load(member: MemberModel) {
            binding.txtMemberName.text = member.name
            binding.txtMemberRole.text = member.role.roleName()
            member.profileImgUrl?.let {
                glideProvider.fetchImage(it, context, binding.imgMemberProfile)
            } ?: binding.imgMemberProfile.setImageResource(R.drawable.ic_user_profile_default)
        }

        private fun hideOverflowOrNot(member: MemberModel) {
            binding.btnProfileOverflow.isVisible = userRole.isExecutive() && userMemberId != member.id
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            ItemCircleMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        val overflowClickListener =
            object : ItemClickListener<MemberModel> {
                override lateinit var item: MemberModel

                override fun onClick(view: View?) {
                    overflowListenerInitializer.initialize(item)
                }
            }

        return CircleMemberAdapterItemViewHolder(binding, overflowClickListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        (holder as CircleMemberAdapterItemViewHolder).bind(position)
    }

    fun update(
        models: MemberModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }
}
