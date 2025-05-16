package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.utils.Const
import java.io.Serializable

data class MyPostModels(private val models: List<MyPostModel>) {
    fun get() = models

    fun get(index: Int) = models[index]

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    fun addAllAndGet(myPostModels: List<MyPostModel>) = MyPostModels(models + myPostModels)
}

data class MyPostModel(
    val circleId: Int,
    val circleName: String,
    val post: PostModel,
) : Serializable {
    fun isSame(target: MyPostModel) = post.id == target.post.id

    fun areContentsSame(target: MyPostModel) = this == target

    companion object {
        fun emptyInstance() =
            MyPostModel(
                0,
                Const.EMPTY_TEXT,
                PostModel.emptyInstance(),
            )
    }
}
