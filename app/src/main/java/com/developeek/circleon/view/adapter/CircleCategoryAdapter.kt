package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemTagCircleCategoryBinding
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.viewmodel.HomeViewModel

class CircleCategoryAdapter(
    private val viewModel: HomeViewModel,
    private val itemClickListener: ItemClickListener<Category>,
    private val context: Context,
) : RecyclerView.Adapter<CircleCategoryAdapter.CircleCategoryAdapterViewHolder>() {
    inner class CircleCategoryAdapterViewHolder(
        private val binding: ItemTagCircleCategoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val category = viewModel.category[position]

            loadCategory(category)
            setItemColor(category, context)
            setItemClickListener(position)
        }

        private fun loadCategory(category: Category) {
            binding.txtCircleCategory.text = category.categoryName()
        }

        private fun setItemColor(
            category: Category,
            context: Context,
        ) {
            viewModel.selectedCategory.value?.let {
                if (it.isSame(category)) {
                    binding.txtCircleCategory.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_active_tag)
                    binding.txtCircleCategory.setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    binding.txtCircleCategory.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_inactive_tag)
                    binding.txtCircleCategory.setTextColor(ContextCompat.getColor(context, R.color.grey_6))
                }
            }
        }

        private fun setItemClickListener(position: Int) {
            binding.clItemCircleCategory.setOnClickListener {
                itemClickListener.onItemClicked(viewModel.category[position])
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CircleCategoryAdapterViewHolder {
        val binding =
            ItemTagCircleCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        return CircleCategoryAdapterViewHolder(binding)
    }

    override fun getItemCount() = viewModel.category.size

    override fun onBindViewHolder(
        holder: CircleCategoryAdapterViewHolder,
        position: Int,
    ) {
        holder.bind(position)
    }
}
