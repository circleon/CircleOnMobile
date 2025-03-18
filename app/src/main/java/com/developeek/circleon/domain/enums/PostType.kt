package com.developeek.circleon.domain.enums

enum class PostType(
    private val codeName: String,
    private val typeName: String,
) {
    POST("POST", "게시글"),
    NOTICE("NOTICE", "공지사항"),
    ;

    fun code() = this.codeName

    fun isPost() = this == POST

    fun isNotice() = this == NOTICE

    companion object {
        private val default = POST

        fun findOrDefault(code: String?) = entries.find { it.codeName == code } ?: default
    }
}
