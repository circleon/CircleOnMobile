package com.developeek.circleon.view.viewmodelimpl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.repository.Page
import com.developeek.circleon.data.source.Error
import com.developeek.circleon.data.source.Success
import com.developeek.circleon.domain.model.BaseModel
import com.developeek.circleon.domain.model.CommentModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.validator.Invalid
import com.developeek.circleon.domain.utils.validator.Validator
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.viewmodel.home.CircleDetailPostDetailViewModel
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

@HiltViewModel(assistedFactory = CircleDetailPostDetailViewModelImpl.CircleDetailPostDetailViewModelFactory::class)
class CircleDetailPostDetailViewModelImpl
    @AssistedInject
    constructor(
        @Assisted("circleId") private val circleId: Int,
        @Assisted("post") private val post: PostModel,
        private val repository: CircleRepository,
    ) : CircleDetailPostDetailViewModel, ViewModel() {
        @AssistedFactory
        interface CircleDetailPostDetailViewModelFactory {
            fun create(
                @Assisted("circleId") circleId: Int,
                @Assisted("post") post: PostModel,
            ): CircleDetailPostDetailViewModelImpl
        }

        private val _event = MutableSharedFlow<Event>()
        override val event: SharedFlow<Event> = _event
        private val _screenFlow =
            MutableStateFlow<CircleDetailPostDetailScreen>(CircleDetailPostDetailScreen.LoadingView)
        override val screenFlow: StateFlow<CircleDetailPostDetailScreen> = _screenFlow

        override val isLastPage: Boolean
            get() = _isLastPage
        private var _isLastPage = false
        private var currentPage = DEFAULT_PAGE

        private lateinit var contents: Models<BaseModel>

        private var fetchCommentsJob: Job? = null
        private var scrollOverCommentJob: Job? = null
        private var userRequestJob: Job? = null
        private val dispatcher = Dispatchers.IO

        init {
            fetchComments(currentPage, SIZE_BY_PAGE)
        }

        override fun showLoadingAndRefresh() {
            viewModelScope.launch {
                _screenFlow.emit(CircleDetailPostDetailScreen.LoadingView)
            }
            refresh()
        }

        private fun refresh() {
            fetchComments(DEFAULT_PAGE, (currentPage + 1) * SIZE_BY_PAGE)
        }

        private fun fetchComments(
            page: Int,
            size: Int,
        ) {
            fetchCommentsJob?.let {
                if (!it.isCompleted) return
            }

            fetchCommentsJob =
                viewModelScope.launch {
                    when (val result = getCirclePostComments(circleId, post.id, page, size)) {
                        is Success -> whenFetchCommentsSuccess(result)
                        is Error -> whenFetchCommentsFail(result)
                    }
                }
        }

        private suspend fun getCirclePostComments(
            circleId: Int,
            postId: Int,
            page: Int,
            size: Int,
        ) = withContext(dispatcher) {
            repository.getCirclePostComments(circleId, postId, page, size)
        }

        private suspend fun whenFetchCommentsSuccess(result: Success<Page<CommentModel>>) {
            result.data.let {
                _isLastPage = it.isLastPage
                contents = Models(listOf(post) + it.content)
                _screenFlow.emit(CircleDetailPostDetailScreen.SuccessView(contents, false))
            }
        }

        private suspend fun whenFetchCommentsFail(result: Error<Page<CommentModel>>) {
            _event.emit(Event.ShowToast(result.message()))
            _screenFlow.emit(CircleDetailPostDetailScreen.ErrorView)

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun scrollOver() {
            scrollOverCommentJob?.let {
                if (!it.isCompleted) return
            }

            scrollOverCommentJob =
                viewModelScope.launch {
                    val result = getCirclePostComments(circleId, post.id, currentPage + 1, SIZE_BY_PAGE)

                    if (!isActive) return@launch
                    when (result) {
                        is Success -> whenScrollOverSuccess(result)
                        is Error -> whenScrollOverFail(result)
                    }
                }
        }

        private suspend fun whenScrollOverSuccess(result: Success<Page<CommentModel>>) {
            result.data.let {
                _isLastPage = it.isLastPage
                contents = contents.addAllAndGet(it.content)
                _screenFlow.emit(CircleDetailPostDetailScreen.SuccessView(contents, false))
            }
            currentPage++
        }

        private suspend fun whenScrollOverFail(result: Error<Page<CommentModel>>) {
            _event.emit(Event.ShowToast(result.message()))

            if (result.isAuthenticationError()) {
                _event.emit(Event.SendToLoginScreen)
            }
        }

        override fun uploadComment(content: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    if (!checkCommentFormat(content)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = postCirclePostComment(circleId, post.id, content)) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_UPLOAD_COMMENT)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postCirclePostComment(
            circleId: Int,
            postId: Int,
            content: String,
        ) = withContext(dispatcher) {
            repository.postCirclePostComment(circleId, postId, content)
        }

        override fun editComment(
            commentId: Int,
            content: String,
        ) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    if (!checkCommentFormat(content)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = putCirclePostComment(circleId, post.id, commentId, content)) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_EDIT_COMMENT)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun putCirclePostComment(
            circleId: Int,
            postId: Int,
            commentId: Int,
            content: String,
        ) = withContext(dispatcher) {
            repository.putCirclePostComment(circleId, postId, commentId, content)
        }

        override fun deleteComment(commentId: Int) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)

                    when (val result = deleteCirclePostComment(circleId, post.id, commentId)) {
                        is Success -> showToastAndRefresh(MESSAGE_SUCCESS_DELETE_COMMENT)
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun deleteCirclePostComment(
            circleId: Int,
            postId: Int,
            commentId: Int,
        ) = withContext(dispatcher) {
            repository.deleteCirclePostComment(circleId, postId, commentId)
        }

        override fun delete() {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    _event.emit(Event.ShowProcessing)

                    when (val result = deleteCirclePost(circleId, post.id)) {
                        is Success -> {
                            _event.emit(Event.ShowToast(MESSAGE_SUCCESS_DELETE_POST))
                            _screenFlow.emit(CircleDetailPostDetailScreen.SuccessView(contents, true))
                        }
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun deleteCirclePost(
            circleId: Int,
            postId: Int,
        ) = withContext(dispatcher) {
            repository.deleteCirclePost(circleId, postId)
        }

        override fun reportPost(message: String) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    if (!checkMessageFormat(message)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = postReportCirclePost(circleId, post.id, message)) {
                        is Success -> _event.emit(Event.ShowToast(MESSAGE_SUCCESS_REQUEST_REPORT))
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postReportCirclePost(
            circleId: Int,
            postId: Int,
            content: String,
        ) = withContext(dispatcher) {
            repository.postReportCirclePost(circleId, postId, content)
        }

        override fun reportComment(
            commentId: Int,
            message: String,
        ) {
            userRequestJob?.let {
                if (!it.isCompleted) return
            }

            userRequestJob =
                viewModelScope.launch {
                    if (!checkMessageFormat(message)) return@launch
                    _event.emit(Event.ShowProcessing)

                    when (val result = postReportCirclePostComment(circleId, commentId, message)) {
                        is Success -> _event.emit(Event.ShowToast(MESSAGE_SUCCESS_REQUEST_REPORT))
                        is Error -> whenUserRequestFail(result)
                    }
                }
        }

        private suspend fun postReportCirclePostComment(
            circleId: Int,
            commentId: Int,
            message: String,
        ) = withContext(dispatcher) {
            repository.postReportCirclePostComment(circleId, commentId, message)
        }

        private suspend fun showToastAndRefresh(message: String) {
            _event.emit(Event.ShowToast(message))
            refresh()
        }

        private suspend fun whenUserRequestFail(result: Error<Unit>) {
            if (result.isAuthenticationError()) {
                _event.emit(Event.ShowToast(result.message()))
                _event.emit(Event.SendToLoginScreen)
                return
            }

            _event.emit(Event.ShowDialog(result.message()))
        }

        private suspend fun checkCommentFormat(comment: String): Boolean {
            val result = Validator.checkComment(comment)

            return if (result is Invalid) {
                _event.emit(Event.ShowDialog(result.message()))
                false
            } else {
                true
            }
        }

        private suspend fun checkMessageFormat(message: String): Boolean {
            val result = Validator.checkMessage(message)

            return if (result is Invalid) {
                _event.emit(Event.ShowDialog(result.message()))
                false
            } else {
                true
            }
        }

        companion object {
            private const val MESSAGE_SUCCESS_UPLOAD_COMMENT = "댓글이 작성됐어요"
            private const val MESSAGE_SUCCESS_EDIT_COMMENT = "댓글이 수정됐어요"
            private const val MESSAGE_SUCCESS_DELETE_COMMENT = "댓글이 삭제됐어요"
            private const val MESSAGE_SUCCESS_DELETE_POST = "게시글이 삭제됐어요"
            private const val MESSAGE_SUCCESS_REQUEST_REPORT = "신고 요청이 완료됐어요"
            private const val SIZE_BY_PAGE = 20
            private const val DEFAULT_PAGE = 0
        }
    }

sealed class CircleDetailPostDetailScreen {
    data class SuccessView(val contents: Models<BaseModel>, val hasDeleted: Boolean) : CircleDetailPostDetailScreen()

    data object LoadingView : CircleDetailPostDetailScreen()

    data object ErrorView : CircleDetailPostDetailScreen()
}
