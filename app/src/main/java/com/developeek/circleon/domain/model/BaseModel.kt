package com.developeek.circleon.domain.model

open class BaseModel(override val id: Int) : Identifiable {
    override fun isSame(target: Identifiable) = this.id == target.id

    override fun areContentsSame(target: Identifiable) = this == target
}
