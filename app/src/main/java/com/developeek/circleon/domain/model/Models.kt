package com.developeek.circleon.domain.model

open class Models<T : BaseModel>(val models: List<T> = emptyList()) {
    fun get() = models

    fun get(index: Int) = models[index]

    fun last() = models.last()

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    fun addAllAndGet(target: List<T>) = Models(models + target)
}
