package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category

data class CategoryModels(private val models: List<CategoryModel>) {
    fun get() = models

    fun selectedOrFirst() = models.find { it.isSelected } ?: models.first()

    fun minus(category: CategoryModel) = CategoryModels(models - category)

    companion object {
        fun selectAndGet(target: Category) =
            CategoryModels(
                Category.entries.map {
                    return@map CategoryModel(it).apply {
                        if (it.isSame(target)) select()
                    }
                },
            )

        fun selectAndRemoveAndGet(
            selectionTarget: Category,
            removeTarget: Category,
        ) = selectAndGet(selectionTarget).minus(CategoryModel(removeTarget))

        fun empty() = CategoryModels(emptyList())
    }
}

data class CategoryModel(val category: Category) : Selectable {
    override val isSelected: Boolean
        get() = selected
    private var selected = false

    override fun select() {
        this.selected = true
    }

    override fun unSelect() {
        this.selected = false
    }

    fun isSame(target: Category) = this.category.isSame(target)

    fun isSame(target: CategoryModel) = this.category.isSame(target.category)

    fun areContentsSame(target: CategoryModel) = this == target && this.isSelected == target.isSelected

    fun name() = this.category.categoryName()
}
