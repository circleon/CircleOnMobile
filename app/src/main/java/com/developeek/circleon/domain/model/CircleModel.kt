package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.utils.Const
import java.io.Serializable

data class CircleModels(private val models: List<CircleModel>) {
    private var isLastPage = false

    fun get() = models

    fun get(index: Int) = models[index]

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    fun add(circleModel: CircleModel) = CircleModels(models + circleModel)

    fun addAll(circleModels: CircleModels) = CircleModels(models + circleModels.get())

    fun setAsLast() {
        isLastPage = true
    }

    fun isLastPage() = isLastPage

    companion object {
        fun emptyInstance() = CircleModels(listOf())
    }
}

data class CircleModel(
    val id: Int,
    val name: String,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val category: Category,
    val comment: String,
    val memberCount: Int,
) : Serializable {
    fun isSame(circleModel: CircleModel) = this.id == circleModel.id

    fun areContentsSame(circleModel: CircleModel) = this == circleModel

    companion object {
        fun emptyInstance() =
            CircleModel(
                0,
                Const.EMPTY_TEXT,
                null,
                null,
                Category.ETC,
                Const.EMPTY_TEXT,
                0,
            )
    }
}
