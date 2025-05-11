package com.developeek.circleon.view.viewmodelimpl

/**
 * Page
 *
 * Pagination 을 통해 가져온 아이템들을 ViewModel 에서 관리하기 위해 작성
 */
data class Page<T>(
    val content: List<T>,
) {
    val isLastPage: Boolean
        get() = _isLastPage
    private var _isLastPage = false

    fun setAsLast() {
        _isLastPage = true
    }
}
