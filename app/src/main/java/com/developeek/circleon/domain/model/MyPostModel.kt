package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.io.Serializable

data class MyPostModel(
    val circleId: Int,
    val circleName: String,
    val post: PostModel,
) : BaseModel(post.postId), Serializable {
    override fun areContentsSame(target: BaseModel): Boolean {
        if (target !is MyPostModel) return false

        return this == target
    }

    companion object {
        fun emptyInstance() =
            MyPostModel(
                0,
                Const.EMPTY_TEXT,
                PostModel.emptyInstance(),
            )
    }
}
