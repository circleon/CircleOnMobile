package com.developeek.circleon.domain.model

interface Selectable {
    val isSelected: Boolean

    fun select()

    fun unSelect()
}
