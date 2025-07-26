package com.developeek.circleon.domain.model

import java.io.Serializable

abstract class BaseModel(val id: Int) : Serializable {
    fun isSame(target: BaseModel) = this.id == target.id

    abstract fun areContentsSame(target: BaseModel): Boolean
}
