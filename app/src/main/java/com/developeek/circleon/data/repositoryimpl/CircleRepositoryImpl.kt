package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.dto.home.Pin
import com.developeek.circleon.data.dto.home.RequestBodyComment
import com.developeek.circleon.data.dto.home.RequestBodyEditPost
import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.remote.retrofit.service.CircleService
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.PostModels
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

class CircleRepositoryImpl(
    private val service: CircleService,
    private val dispatcher: CoroutineDispatcher,
) : CircleRepository {
    override suspend fun getCircles(
        page: Int,
        size: Int,
        category: Category,
    ): Result<CircleModels> {
        return try {
            withContext(dispatcher) {
                val response =
                    if (category == Category.ALL) {
                        service.getAllCircles(page, size, SORT_LATEST)
                    } else {
                        service.getCircleScrollContents(page, size, SORT_LATEST, category.codeName())
                    }
                Result.success(
                    CircleModels(response.content.map { it.toCircleModel() }).apply {
                        if (response.isLastPage()) {
                            setAsLast()
                        }
                    },
                )
            }
        } catch (e: ServiceException.NoResultException) {
            Result.success(CircleModels.emptyInstance())
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getCircleSummaries(): Result<CircleSummaryModels> {
        return try {
            withContext(dispatcher) {
                val response = service.getCircleSummaries()
                Result.success(CircleSummaryModels(response.content.map { it.toCircleSummaryModel() }))
            }
        } catch (e: ServiceException.NoResultException) {
            Result.success(CircleSummaryModels.emptyInstance())
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getCircleDetail(circleId: Int): Result<CircleDetailModel> {
        return try {
            withContext(dispatcher) {
                val response = service.getCircleDetail(circleId)
                Result.success(response.toCircleDetailModel())
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getCirclePosts(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<PostModels> {
        return try {
            withContext(dispatcher) {
                val response = service.getCirclePosts(circleId, page, size, TYPE_POST)
                Result.success(
                    PostModels(response.content.map { it.toPostModel() }).apply {
                        if (response.isLastPage()) {
                            setAsLast()
                        }
                    },
                )
            }
        } catch (e: ServiceException.NoResultException) {
            Result.success(PostModels.emptyInstance())
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getCircleNotices(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<PostModels> {
        return try {
            withContext(dispatcher) {
                val response = service.getCirclePosts(circleId, page, size, TYPE_NOTICE)
                Result.success(
                    PostModels(response.content.map { it.toPostModel() }).apply {
                        if (response.isLastPage()) {
                            setAsLast()
                        }
                    },
                )
            }
        } catch (e: ServiceException.NoResultException) {
            Result.success(PostModels.emptyInstance())
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getPostComments(
        circleId: Int,
        postId: Int,
        page: Int,
        size: Int,
    ): Result<CommentModels> {
        return try {
            withContext(dispatcher) {
                val response = service.getPostComments(circleId, postId, page, size)
                Result.success(
                    CommentModels(response.content.map { it.toCommentModel() }).apply {
                        if (response.isLastPage()) {
                            setAsLast()
                        }
                    },
                )
            }
        } catch (e: ServiceException.NoResultException) {
            Result.success(CommentModels.emptyInstance())
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun postCirclePost(
        circleId: Int,
        postType: PostType,
        content: String,
        image: File?,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                val type = RequestBody.create(MediaType.parse("text/plain"), postType.code())
                val content = RequestBody.create(MediaType.parse("text/plain"), content)
                val body =
                    if (image == null) {
                        null
                    } else {
                        val imageRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), image)
                        MultipartBody.Part.createFormData("image", image.name, imageRequestBody)
                    }
                service.postCirclePost(circleId, type, content, body)
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun postCircleComment(
        circleId: Int,
        postId: Int,
        comment: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.postCircleComment(circleId, postId, RequestBodyComment(comment))
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun putPostPin(
        circleId: Int,
        postId: Int,
        isPinned: Boolean,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putPostPin(circleId, postId, Pin(isPinned))
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun putCirclePost(
        circleId: Int,
        postId: Int,
        postType: PostType,
        content: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putCirclePost(circleId, postId, RequestBodyEditPost(postType.code(), content))
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun deleteCirclePost(
        circleId: Int,
        postId: Int,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.deleteCirclePost(circleId, postId)
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun deletePostComment(
        circleId: Int,
        postId: Int,
        commentId: Int,
    ): Result<Unit> {
        return try {
            with(dispatcher) {
                service.deletePostComment(circleId, postId, commentId)
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    companion object {
        private const val SORT_LATEST = "createdAt,desc"
        private const val TYPE_POST = "POST"
        private const val TYPE_NOTICE = "NOTICE"
    }
}
