package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.OfficialStatus
import com.developeek.circleon.domain.utils.Const
import java.io.Serializable

data class CircleModel(
    val circleId: Int,
    val name: String,
    val category: Category,
    val officialStatus: OfficialStatus,
    val profileImgUrl: String?,
    val thumbnailUrl: String?,
    val comment: String,
    val memberCount: Int,
) : BaseModel(circleId), Serializable {
    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is CircleModel) return false

        return this == target
    }

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
