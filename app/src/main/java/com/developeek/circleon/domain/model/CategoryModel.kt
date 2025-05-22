package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category

data class CategoryModels(private val categoryModels: List<CategoryModel>) : Models<CategoryModel>(categoryModels) {
    fun selectedOrFirst() = categoryModels.find { it.isSelected } ?: categoryModels.first()

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
        ) = CategoryModels(selectAndGet(selectionTarget).minusAndGet(CategoryModel(removeTarget)).get())
    }
}

data class CategoryModel(val category: Category) : BaseModel(category.id), Selectable {
    override val isSelected: Boolean
        get() = _isSelected
    private var _isSelected = false

    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is CategoryModel) return false

        return this == target && _isSelected == target.isSelected
    }

    override fun select() {
        _isSelected = true
    }

    override fun unSelect() {
        _isSelected = false
    }

    fun name() = this.category.categoryName
}
