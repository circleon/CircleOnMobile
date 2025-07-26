package com.developeek.circleon.view.viewmodel.home

import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodelimpl.home.SearchCircleScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SearchCircleViewModel {
    val event: SharedFlow<Event>
    val screenFlow: StateFlow<SearchCircleScreen>

    fun refresh()

    fun setKeywordAndFind(keyword: String)

    fun clearKeyword()
}
