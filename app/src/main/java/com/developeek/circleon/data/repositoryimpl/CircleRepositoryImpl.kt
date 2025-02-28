package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.dto.home.Pin
import com.developeek.circleon.data.dto.home.RequestBodyEditComment
import com.developeek.circleon.data.dto.home.RequestBodyEditPost
import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.remote.retrofit.service.CircleService
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.model.PostModels
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
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
                        service.getAllCircles(page, size, SORT_CIRCLE_OLDEST)
                    } else {
                        service.getCircleScrollContents(page, size, SORT_CIRCLE_OLDEST, category.codeName())
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
            Result.success(CircleModels.empty())
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
            Result.success(CircleSummaryModels.empty())
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getCircleDetail(circleId: Int): Result<CircleDetailModel> {
        return try {
            withContext(dispatcher) {
                val getCircleDetailJob =
                    async {
                        return@async service.getCircleDetail(circleId)
                    }
                val getMembersJob =
                    async {
                        return@async service.getMembers(
                            circleId,
                            0,
                            200,
                            SORT_MEMBER_BY_NAME,
                            MembershipStatus.JOINED.codeName(),
                        )
                    }

                Result.success(
                    getCircleDetailJob.await().toCircleDetailModel(
                        MemberModels(getMembersJob.await().content.map { it.toMemberModel() }),
                    ),
                )
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getCircleMembers(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<MemberModels> {
        return try {
            withContext(dispatcher) {
                val response =
                    service.getMembers(
                        circleId,
                        page,
                        size,
                        SORT_MEMBER_BY_NAME,
                        MembershipStatus.JOINED.codeName(),
                    )
                Result.success(MemberModels(response.content.map { it.toMemberModel() }))
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getCircleJoinRequestedMembers(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<MemberModels> {
        return try {
            withContext(dispatcher) {
                val response =
                    service.getMembers(
                        circleId,
                        page,
                        size,
                        SORT_MEMBER_BY_NAME,
                        MembershipStatus.JOIN_REQUESTED.codeName(),
                    )
                Result.success(MemberModels(response.content.map { it.toMemberModel() }))
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun getCircleLeaveRequestedMembers(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<MemberModels> {
        return try {
            withContext(dispatcher) {
                val response =
                    service.getMembers(
                        circleId,
                        page,
                        size,
                        SORT_MEMBER_BY_NAME,
                        MembershipStatus.LEAVE_REQUESTED.codeName(),
                    )
                Result.success(MemberModels(response.content.map { it.toMemberModel() }))
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
            Result.success(PostModels.empty())
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
            Result.success(PostModels.empty())
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
                service.postCircleComment(circleId, postId, RequestBodyEditComment(comment))
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

    override suspend fun putCircle(circleDetailModel: CircleDetailModel): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putCircle(circleDetailModel.id, circleDetailModel.toRequestBodyForEdit())
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun putCircleImage(
        circleId: Int,
        circleThumbnail: File?,
        circleIntroductionImage: File?,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                var imageRequestBody: RequestBody
                val thumbnail =
                    if (circleThumbnail == null) {
                        null
                    } else {
                        imageRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), circleThumbnail)
                        MultipartBody.Part.createFormData("profileImg", circleThumbnail.name, imageRequestBody)
                    }
                val introductionImage =
                    if (circleIntroductionImage == null) {
                        null
                    } else {
                        imageRequestBody =
                            RequestBody.create(MediaType.parse("image/jpeg"), circleIntroductionImage)
                        MultipartBody.Part.createFormData(
                            "introImg",
                            circleIntroductionImage.name,
                            imageRequestBody,
                        )
                    }
                service.putCircleImage(circleId, thumbnail, introductionImage)
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

    override suspend fun putCircleComment(
        circleId: Int,
        postId: Int,
        commentId: Int,
        content: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putCircleComment(circleId, postId, commentId, RequestBodyEditComment(content))
                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.error(e)
        }
    }

    override suspend fun deleteCircleImage(
        circleId: Int,
        deleteThumbnail: Boolean,
        deleteIntroductionImage: Boolean,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.deleteCircleImage(circleId, deleteThumbnail, deleteIntroductionImage)
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
        private const val SORT_CIRCLE_OLDEST = "createdAt,asc"
        private const val SORT_CIRCLE_LATEST = "createdAt,desc"
        private const val SORT_MEMBER_BY_NAME = "username,asc"
        private const val TYPE_POST = "POST"
        private const val TYPE_NOTICE = "NOTICE"
    }
}
