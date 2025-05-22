package com.developeek.circleon.domain.enums

enum class Category(
    val id: Int,
    val codeName: String,
    val categoryName: String,
) {
    ALL(0, "ALL", "전체"),
    IT_COMPUTER(1, "IT_COMPUTER", "IT/컴퓨터"),
    SPORTS(2, "SPORTS", "스포츠"),
    CULTURE(3, "CULTURE", "문화"),
    VOLUNTEER(4, "VOLUNTEER", "봉사"),
    ACADEMIC_LIBERAL_ARTS(5, "ACADEMIC_LIBERAL_ARTS", "학술/교양"),
    ENTREPRENEURSHIP(6, "ENTREPRENEURSHIP", "창업"),
    FRIENDSHIP(7, "FRIENDSHIP", "친목"),
    RELIGION(8, "RELIGION", "종교"),
    LANGUAGE(9, "LANGUAGE", "어학"),
    ETC(10, "ETC", "기타"), ;

    fun isSame(target: Category) = this.id == target.id

    companion object {
        private val default = ETC

        fun findOrDefault(code: String) = entries.find { it.codeName == code } ?: default
    }
}
