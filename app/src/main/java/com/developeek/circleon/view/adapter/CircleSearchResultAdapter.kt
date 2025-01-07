package com.developeek.circleon.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.databinding.ItemCircleSearchResultBinding
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.view.listener.ItemClickListener
import com.developeek.circleon.view.listener.ItemListenerInitializer

class CircleSearchResultAdapter(private val itemListenerInitializer: ItemListenerInitializer<CircleSummaryModel>) :
    RecyclerView.Adapter<CircleSearchResultAdapter.CircleSearchResultAdapterViewHolder>() {
    private val diffUtil =
        AsyncListDiffer(
            this,
            object : DiffUtil.ItemCallback<CircleSummaryModel>() {
                override fun areItemsTheSame(
                    oldItem: CircleSummaryModel,
                    newItem: CircleSummaryModel,
                ): Boolean {
                    return oldItem.isSame(newItem)
                }

                override fun areContentsTheSame(
                    oldItem: CircleSummaryModel,
                    newItem: CircleSummaryModel,
                ): Boolean {
                    return oldItem.areContentsSame(newItem)
                }
            },
        )

    inner class CircleSearchResultAdapterViewHolder(
        private val binding: ItemCircleSearchResultBinding,
        private val itemClickListener: ItemClickListener<CircleSummaryModel>,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            loadCircleSummary(position)
            notifyListenerItemChanged(position)
        }

        private fun loadCircleSummary(position: Int) {
            binding.txtCircleName.text = diffUtil.currentList[position].name
            binding.txtCircleCategory.text = diffUtil.currentList[position].category.categoryName()
        }

        private fun notifyListenerItemChanged(position: Int) {
            itemClickListener.item = diffUtil.currentList[position]
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CircleSearchResultAdapterViewHolder {
        val binding =
            ItemCircleSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        val itemClickListener =
            object : ItemClickListener<CircleSummaryModel> {
                override lateinit var item: CircleSummaryModel

                override fun onClick(p0: View?) {
                    itemListenerInitializer.initialize(item)
                }
            }
        binding.llItemCircleSearchResult.setOnClickListener(itemClickListener)
        return CircleSearchResultAdapterViewHolder(binding, itemClickListener)
    }

    override fun getItemCount() = diffUtil.currentList.size

    override fun onBindViewHolder(
        holder: CircleSearchResultAdapterViewHolder,
        position: Int,
    ) {
        holder.bind(position)
    }

    fun update(
        models: CircleSummaryModels,
        commitCallback: Runnable,
    ) {
        diffUtil.submitList(models.get(), commitCallback)
    }
}
