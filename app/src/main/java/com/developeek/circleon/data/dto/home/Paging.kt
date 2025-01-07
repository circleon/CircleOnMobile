package com.developeek.circleon.data.dto.home

/**
 * Paging<T>
 *
 * 데이터 호출 시 pagination 방식으로 호출할 때 사용되는 범용 제네릭 타입 DTO
 */
data class Paging<T>(
    val content: List<T>,
    val currentPageNumber: Int,
    val totalElementCount: Int,
    val totalPageCount: Int,
) {
    /**
     * isLastPage
     *
     * 페이지 호출 시 가장 최근 호출 페이지가 마지막 페이지인지 확인
     * when page 1 -> currentPage = 0, totalPage = 1
     */
    fun isLastPage() = currentPageNumber >= (totalPageCount - 1)
}
