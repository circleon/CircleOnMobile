package com.developeek.circleon.domain.model

interface Identifiable {
    val id: Int

    fun isSame(target: Identifiable): Boolean

    fun areContentsSame(target: Identifiable): Boolean
}
