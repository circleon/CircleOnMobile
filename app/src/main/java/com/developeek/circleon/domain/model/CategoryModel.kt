package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category

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

    fun name() = this.category.categoryName

    companion object {
        fun selectAndGet(target: Category) =
            Models(
                Category.entries.map {
                    return@map CategoryModel(it).apply {
                        if (it.isSame(target)) select()
                    }
                },
            )

        fun selectAndGetWithoutALL(target: Category) =
            Models(
                Category.withoutALL().map {
                    return@map CategoryModel(it).apply {
                        if (it.isSame(target)) select()
                    }
                },
            )
    }
}
