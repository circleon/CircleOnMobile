package com.developeek.circleon.domain.enums

enum class Category(
    private val code: String,
    private val categoryName: String,
) {
    ALL("ALL", "전체"),
    IT_COMPUTER("IT_COMPUTER", "IT/컴퓨터"),
    SPORTS("SPORTS", "스포츠"),
    CULTURE("CULTURE", "문화"),
    VOLUNTEER("VOLUNTEER", "봉사"),
    ACADEMIC_LIBERAL_ARTS("ACADEMIC_LIBERAL_ARTS", "학술/교양"),
    ENTREPRENEURSHIP("ENTREPRENEURSHIP", "창업"),
    FRIENDSHIP("FRIENDSHIP", "친목"),
    RELIGION("RELIGION", "종교"),
    LANGUAGE("LANGUAGE", "어학"),
    ETC("ETC", "기타"), ;

    fun isSame(category: Category) = this.code == category.code

    fun codeName() = code

    fun categoryName() = categoryName

    companion object {
        fun findOrNull(code: String) = Category.entries.find { it.code == code }
    }
}
