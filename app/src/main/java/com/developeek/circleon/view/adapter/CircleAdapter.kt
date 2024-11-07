package com.developeek.circleon.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.databinding.ItemCardCircleBinding
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.view.viewmodel.HomeViewModel

class CircleAdapter(
    private val viewModel: HomeViewModel,
) : RecyclerView.Adapter<CircleAdapter.CircleAdapterViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<CircleModel>() {
                override fun areItemsTheSame(
                    oldItem: CircleModel,
                    newItem: CircleModel,
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: CircleModel,
                    newItem: CircleModel,
                ): Boolean {
                    return oldItem.id == newItem.id
                }
            },
        )

    inner class CircleAdapterViewHolder(
        private val binding: ItemCardCircleBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            loadCircle(position)
        }

        private fun loadCircle(position: Int) {
            binding.txtCircleName.text = viewModel.circles.get(position).name
            binding.txtCircleCategory.text = viewModel.circles.get(position).category.categoryName()
            binding.txtCirclePeopleCount.text =
                String.format(
                    MEMBER_COUNT_UNIT, viewModel.circles.get(position).member,
                )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CircleAdapterViewHolder {
        val binding =
            ItemCardCircleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        return CircleAdapterViewHolder(binding)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun onBindViewHolder(
        holder: CircleAdapterViewHolder,
        position: Int,
    ) {
        holder.bind(position)
    }

    fun update() {
        diffUtil.submitList(viewModel.circles.get())
    }

    companion object {
        private const val MEMBER_COUNT_UNIT = "%d명"
    }
}
