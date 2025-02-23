package com.developeek.circleon.domain.enums

enum class PostType(
    private val code: String,
    private val typeName: String,
) {
    POST("POST", "게시글"),
    NOTICE("NOTICE", "공지사항"),
    ;

    fun code() = this.code

    fun isPost() = this.code == POST.code

    fun isNotice() = this.code == NOTICE.code

    companion object {
        private val default = POST

        fun findOrDefault(code: String?) = entries.find { it.code == code } ?: default
    }
}
