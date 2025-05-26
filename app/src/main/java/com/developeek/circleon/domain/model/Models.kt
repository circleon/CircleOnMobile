package com.developeek.circleon.domain.model

import java.io.Serializable

open class Models<T : BaseModel>(private val models: List<T> = emptyList()) : Serializable {
    fun get() = models

    fun get(index: Int) = models[index]

    fun first() = models.first()

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    fun addAllAndGet(target: List<T>) = Models(models + target)
}
