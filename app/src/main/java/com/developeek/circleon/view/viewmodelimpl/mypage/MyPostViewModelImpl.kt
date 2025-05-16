package com.developeek.circleon.view.viewmodelimpl.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.MyPostModel
import com.developeek.circleon.domain.model.MyPostModels
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.mypage.MyPostViewModel
import com.developeek.circleon.view.viewmodelimpl.Page
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = MyPostViewModelImpl.MyPostViewModelFactory::class)
class MyPostViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("isMyPosts") private val isMyPosts: Boolean,
        private val repository: CircleRepository,
    ) : MyPostViewModel, ViewModel() {
        @AssistedFactory
        interface MyPostViewModelFactory {
            fun create(
                @Assisted("isMyPosts") isMyPosts: Boolean,
            ): MyPostViewModelImpl
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow = MutableStateFlow<MyPostScreen>(MyPostScreen.LoadingView)
        override val screenFlow: StateFlow<MyPostScreen> = _screenFlow

        override val isLastPage: Boolean
            get() = _isLastPage
        private var _isLastPage = false
        private var currentPage = DEFAULT_PAGE

        private lateinit var posts: MyPostModels

        private var fetchMyPostsJob: Job? = null
        private var scrollOverMyPostsJob: Job? = null
        private val dispatcher = Dispatchers.IO

        init {
            fetchMyPosts(currentPage, SIZE_BY_PAGE)
        }

        private fun fetchMyPosts(
            page: Int,
            size: Int,
        ) {
            fetchMyPostsJob?.let {
                if (!it.isCompleted) return
            }

            fetchMyPostsJob =
                viewModelScope.launch {
                    _screenFlow.emit(MyPostScreen.LoadingView)
                    scrollOverMyPostsJob?.cancel()

                    when (val result = if (isMyPosts) getMyPosts(page, size) else getMyCommentPosts(page, size)) {
                        is Success -> whenFetchMyPostsSuccess(result)
                        is Error -> whenFetchMyPostsFail(result)
                    }
                }
        }

        private suspend fun getMyPosts(
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getMyPosts(page, size)
        }

        private suspend fun getMyCommentPosts(
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getMyCommentPosts(page, size)
        }

        private suspend fun whenFetchMyPostsSuccess(result: Success<Page<MyPostModel>>) {
            result.data.let {
                posts = MyPostModels(it.content)
                _isLastPage = it.isLastPage
                Log.d("isLast", _isLastPage.toString())
            }
            _screenFlow.emit(MyPostScreen.SuccessView(posts))
        }

        private suspend fun whenFetchMyPostsFail(result: Error<Page<MyPostModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(MyPostScreen.ErrorView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun refresh() {
            fetchMyPosts(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
        }

        override fun scrollOver() {
            scrollOverMyPostsJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverMyPostsJob =
                viewModelScope.launch {
                    val result =
                        if (isMyPosts) {
                            getMyPosts(currentPage + 1, SIZE_BY_PAGE)
                        } else {
                            getMyCommentPosts(
                                currentPage + 1,
                                SIZE_BY_PAGE,
                            )
                        }

                    if (!isActive) return@launch
                    when (result) {
                        is Success -> whenScrollOverSuccess(result)
                        is Error -> whenScrollOverFail(result)
                    }
                }
        }

        private suspend fun whenScrollOverSuccess(result: Success<Page<MyPostModel>>) {
            result.data.let {
                posts = posts.addAllAndGet(it.content)
                _isLastPage = isLastPage
            }
            currentPage++

            _screenFlow.emit(MyPostScreen.SuccessView(posts))
        }

        private suspend fun whenScrollOverFail(result: Error<Page<MyPostModel>>) {
            _event.emit(Event.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        companion object {
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class MyPostScreen {
    data class SuccessView(val posts: MyPostModels) : MyPostScreen()

    data object LoadingView : MyPostScreen()

    data object ErrorView : MyPostScreen()
}
