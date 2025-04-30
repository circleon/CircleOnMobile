package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.OfficialStatus
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
        fun empty() = CircleModels(emptyList())
    }
}

data class CircleModel(
    val id: Int,
    val name: String,
    val category: Category,
    val officialStatus: OfficialStatus,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val comment: String,
    val memberCount: Int,
) : Serializable {
    fun isSame(circleModel: CircleModel) = this.id == circleModel.id

    fun areContentsSame(circleModel: CircleModel) = this == circleModel

    fun isOfficial() = officialStatus.isOfficial()

    companion object {
        fun emptyInstance() =
            CircleModel(
                0,
                Const.EMPTY_TEXT,
                Category.ETC,
                OfficialStatus.UNOFFICIAL,
                null,
                null,
                Const.EMPTY_TEXT,
                0,
            )
    }
}
