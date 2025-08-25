package com.developeek.circleon.domain.model

import java.io.Serializable

open class Models<T : BaseModel>(private val models: List<T> = emptyList()) : Serializable {
    fun get() = models

    fun get(index: Int) = models[index]

    fun sortedWith(comparator: Comparator<in T>) = Models(models.sortedWith(comparator))

    fun find(target: T) = models.find { it.isSame(target) }

    fun first() = models.first()

    fun size() = models.size

    fun isEmpty() = models.isEmpty()

    fun addAllAndGet(target: List<T>) = Models(models + target)

    fun replaceAndGet(
        oldItem: T,
        newItem: T,
    ) = Models(models.map { if (it.isSame(oldItem)) newItem else it })

    fun deleteAndGet(item: T) = Models(models.filter { !it.isSame(item) })
}
