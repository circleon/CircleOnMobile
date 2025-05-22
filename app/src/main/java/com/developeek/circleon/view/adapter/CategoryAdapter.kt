package com.developeek.circleon.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.R
import com.developeek.circleon.databinding.ItemTagCircleCategoryBinding
import com.developeek.circleon.domain.model.CategoryModel
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class CategoryAdapter(
    private val context: Context,
    private val itemListenerInitializer: ItemListenerInitializer<CategoryModel>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffUtil = CustomAsyncListDiffer<CategoryModel>(this)

    inner class CategoryAdapterViewHolder(
        private val binding: ItemTagCircleCategoryBinding,
        private val itemClickListener: ItemClickListener<CategoryModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val category = diffUtil.currentList[position]

            loadCategory(category)
            setItemColor(category, context)
            notifyListenerItemChanged(category)
        }

        private fun loadCategory(category: CategoryModel) {
            binding.txtCircleCategory.text = category.name()
        }

        private fun setItemColor(
            category: CategoryModel,
            context: Context,
        ) {
            if (category.isSelected) {
                binding.txtCircleCategory.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_button)
                binding.txtCircleCategory.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.txtCircleCategory.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_inactive_tag)
                binding.txtCircleCategory.setTextColor(ContextCompat.getColor(context, R.color.grey_6))
            }
        }

        private fun notifyListenerItemChanged(category: CategoryModel) {
            itemClickListener.item = category
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CategoryAdapterViewHolder {
        val binding =
            ItemTagCircleCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        val itemClickListener =
            object : ItemClickListener<CategoryModel> {
                override lateinit var item: CategoryModel

                override fun onClick(p0: View?) {
                    itemListenerInitializer.initialize(item)
                }
            }
        binding.clItemCircleCategory.setOnClickListener(itemClickListener)
        return CategoryAdapterViewHolder(binding, itemClickListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is CategoryAdapterViewHolder) {
            holder.bind(position)
        }
    }

    fun update(
        models: CategoryModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }
}
