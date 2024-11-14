package com.developeek.circleon.domain.model

import com.developeek.circleon.domain.enums.University
import java.io.Serializable

data class UserModel(
    val id: Int,
    val name: String,
    val univ: University,
) : Serializable
