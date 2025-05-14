package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.dto.home.Pin
import com.developeek.circleon.data.dto.home.RequestBodyEditComment
import com.developeek.circleon.data.dto.home.RequestBodyEditMemberRole
import com.developeek.circleon.data.dto.home.RequestBodyEditMemberStatus
import com.developeek.circleon.data.dto.home.RequestBodyEditPost
import com.developeek.circleon.data.dto.home.RequestBodyReport
import com.developeek.circleon.data.dto.home.RequestResponseBodyCircleJoin
import com.developeek.circleon.data.dto.home.RequestResponseBodyCircleLeave
import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.remote.retrofit.service.CircleService
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.enums.OfficialStatus
import com.developeek.circleon.domain.enums.PostType
import com.developeek.circleon.domain.enums.Role
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.CircleModel
import com.developeek.circleon.domain.model.CircleSummaryModels
import com.developeek.circleon.domain.model.CommentModels
import com.developeek.circleon.domain.model.MemberModels
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.view.viewmodelimpl.Page
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CircleRepositoryImpl(
    private val service: CircleService,
    private val dispatcher: CoroutineDispatcher,
) : CircleRepository {
    override suspend fun getCircles(
        page: Int,
        size: Int,
        category: Category,
    ): Result<Page<CircleModel>> {
        return try {
            val response =
                if (category.isSame(Category.ALL)) {
                    service.getAllCircles(page, size, SORT_CIRCLE_OLDEST)
                } else {
                    service.getCircleScrollContents(page, size, SORT_CIRCLE_OLDEST, category.codeName())
                }
            Result.success(
                Page(response.content.map { it.toCircleModel() }).apply {
                    if (response.isLastPage()) {
                        setAsLast()
                    }
                },
            )
        } catch (e: ServiceException.NoResultException) {
            Result.success(Page(emptyList()))
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getCircleDetail(circleId: Int): Result<CircleDetailModel> {
        return try {
            val response = service.getCircleDetail(circleId)
            Result.success(response.toCircleDetailModel())
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getCircleMembers(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<MemberModels> {
        return try {
            val response =
                service.getMembers(
                    circleId,
                    page,
                    size,
                    SORT_MEMBER_BY_NAME,
                    MembershipStatus.JOINED.codeName(),
                )
            Result.success(MemberModels(response.content.map { it.toMemberModel() }))
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getCircleLeaveRequestedMemberMessage(
        circleId: Int,
        memberId: Int,
    ): Result<String> {
        return try {
            withContext(dispatcher) {
                val response = service.getCircleLeaveRequestedMemberMessage(circleId, memberId)
                Result.success(response.message)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getCircleJoinRequestedMemberMessage(
        circleId: Int,
        memberId: Int,
    ): Result<String> {
        return try {
            withContext(dispatcher) {
                val response = service.getCircleJoinRequestedMemberMessage(circleId, memberId)
                Result.success(response.message)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getCirclePosts(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<Page<PostModel>> {
        return try {
            val response = service.getCirclePosts(circleId, page, size, TYPE_POST)
            Result.success(
                Page(response.content.map { it.toPostModel() }).apply {
                    if (response.isLastPage()) {
                        setAsLast()
                    }
                },
            )
        } catch (e: ServiceException.NoResultException) {
            Result.success(Page(emptyList()))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getCircleNotices(
        circleId: Int,
        page: Int,
        size: Int,
    ): Result<Page<PostModel>> {
        return try {
            val response = service.getCirclePosts(circleId, page, size, TYPE_NOTICE)
            Result.success(
                Page(response.content.map { it.toPostModel() }).apply {
                    if (response.isLastPage()) {
                        setAsLast()
                    }
                },
            )
        } catch (e: ServiceException.NoResultException) {
            Result.success(Page(emptyList()))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getCirclePostComments(
        circleId: Int,
        postId: Int,
        page: Int,
        size: Int,
    ): Result<CommentModels> {
        return try {
            withContext(dispatcher) {
                val response = service.getCirclePostComments(circleId, postId, page, size)
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
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getMyCircles(
        page: Int,
        size: Int,
    ): Result<CircleSummaryModels> {
        return try {
            withContext(dispatcher) {
                val response = service.getMyCircles(MembershipStatus.JOINED.codeName(), page, size)
                Result.success(CircleSummaryModels(response.content.map { it.toCircleSummaryModel() }))
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getMyJoinRequestedCircles(
        page: Int,
        size: Int,
    ): Result<CircleSummaryModels> {
        return try {
            withContext(dispatcher) {
                val response = service.getMyCircles(MembershipStatus.JOIN_REQUESTED.codeName(), page, size)
                Result.success(CircleSummaryModels(response.content.map { it.toCircleSummaryModel() }))
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getMyLeaveRequestedCircles(
        page: Int,
        size: Int,
    ): Result<CircleSummaryModels> {
        return try {
            withContext(dispatcher) {
                val response = service.getMyCircles(MembershipStatus.LEAVE_REQUESTED.codeName(), page, size)
                Result.success(CircleSummaryModels(response.content.map { it.toCircleSummaryModel() }))
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun postCircle(
        circleDetailModel: CircleDetailModel,
        profileImg: File?,
        introductionImg: File?,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                val textMediaType = TEXT_CONTENT_TYPE.toMediaType()

                val nameRequestBody = circleDetailModel.name.toRequestBody(textMediaType)
                val singleLineIntroductionRequestBody =
                    circleDetailModel.singleLineIntroduction.toRequestBody(
                        textMediaType,
                    )
                val categoryRequestBody = circleDetailModel.category.codeName().toRequestBody(textMediaType)
                val introductionRequestBody = circleDetailModel.introduction?.toRequestBody(textMediaType)
                val recruitmentStartDate =
                    circleDetailModel.recruitmentStartDate?.toString()?.toRequestBody(textMediaType)
                val recruitmentEndDate =
                    circleDetailModel.recruitmentEndDate?.toString()?.toRequestBody(textMediaType)
                val profileImgRequestBody =
                    profileImg?.let {
                        val imgRequestBody = it.asRequestBody(IMAGE_CONTENT_TYPE.toMediaType())
                        MultipartBody.Part.createFormData("profileImg", profileImg.name, imgRequestBody)
                    }
                val introductionImgRequestBody =
                    introductionImg?.let {
                        val imgRequestBody = it.asRequestBody(IMAGE_CONTENT_TYPE.toMediaType())
                        MultipartBody.Part.createFormData("introductionImg", introductionImg.name, imgRequestBody)
                    }

                service.postCircle(
                    nameRequestBody,
                    singleLineIntroductionRequestBody,
                    categoryRequestBody,
                    introductionRequestBody,
                    recruitmentStartDate,
                    recruitmentEndDate,
                    profileImgRequestBody,
                    introductionImgRequestBody,
                )
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun postMyCircle(
        circleId: Int,
        joinMessage: String,
    ): Result<Unit> {
        return try {
            service.postMyCircle(circleId, RequestResponseBodyCircleJoin(joinMessage))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun postCircleLeaveRequest(
        memberId: Int,
        leaveMessage: String,
    ): Result<Unit> {
        return try {
            service.postCircleLeaveRequest(memberId, RequestResponseBodyCircleLeave(leaveMessage))
            Result.success(Unit)
        } catch (e: Exception) {
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
                val postTypeRequestBody = postType.code().toRequestBody(TEXT_CONTENT_TYPE.toMediaType())
                val contentRequestBody = content.toRequestBody(TEXT_CONTENT_TYPE.toMediaType())
                val postImageRequestBody =
                    if (image == null) {
                        null
                    } else {
                        val imageRequestBody = image.asRequestBody(IMAGE_CONTENT_TYPE.toMediaType())
                        MultipartBody.Part.createFormData("image", image.name, imageRequestBody)
                    }
                service.postCirclePost(circleId, postTypeRequestBody, contentRequestBody, postImageRequestBody)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun postCirclePostComment(
        circleId: Int,
        postId: Int,
        comment: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.postCirclePostComment(circleId, postId, RequestBodyEditComment(comment))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun postReportCircle(
        circleId: Int,
        content: String,
    ): Result<Unit> {
        return try {
            service.postReportCircle(circleId, RequestBodyReport(content))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun postReportCirclePost(
        circleId: Int,
        postId: Int,
        content: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.postReportCirclePost(circleId, postId, RequestBodyReport(content))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun postReportCirclePostComment(
        circleId: Int,
        commentId: Int,
        content: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.postReportCirclePostComment(circleId, commentId, RequestBodyReport(content))
                Result.success(Unit)
            }
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun putCircle(circleDetailModel: CircleDetailModel): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putCircle(circleDetailModel.id, circleDetailModel.toRequestBodyForEdit())
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun putCircleOfficialStatus(circleId: Int): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putCircleOfficialStatus(circleId, OfficialStatus.OFFICIAL_REQUESTED.codeName())
                Result.success(Unit)
            }
        } catch (e: Exception) {
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
                        imageRequestBody = circleThumbnail.asRequestBody(IMAGE_CONTENT_TYPE.toMediaType())
                        MultipartBody.Part.createFormData("profileImg", circleThumbnail.name, imageRequestBody)
                    }
                val introductionImage =
                    if (circleIntroductionImage == null) {
                        null
                    } else {
                        imageRequestBody =
                            circleIntroductionImage.asRequestBody(IMAGE_CONTENT_TYPE.toMediaType())
                        MultipartBody.Part.createFormData(
                            "introImg",
                            circleIntroductionImage.name,
                            imageRequestBody,
                        )
                    }
                service.putCircleImage(circleId, thumbnail, introductionImage)
                Result.success(Unit)
            }
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun putCirclePostComment(
        circleId: Int,
        postId: Int,
        commentId: Int,
        content: String,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putCirclePostComment(circleId, postId, commentId, RequestBodyEditComment(content))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun putCircleMemberRole(
        circleId: Int,
        memberId: Int,
        role: Role,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putCircleMemberRole(circleId, memberId, RequestBodyEditMemberRole(role.codeName()))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun putCircleMemberStatus(
        circleId: Int,
        memberId: Int,
        status: MembershipStatus,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.putCircleMemberStatus(circleId, memberId, RequestBodyEditMemberStatus(status.codeName()))
                Result.success(Unit)
            }
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteCirclePostComment(
        circleId: Int,
        postId: Int,
        commentId: Int,
    ): Result<Unit> {
        return try {
            with(dispatcher) {
                service.deleteCirclePostComment(circleId, postId, commentId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteCircleMember(
        circleId: Int,
        memberId: Int,
    ): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.deleteCircleMember(circleId, memberId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteCircleJoinRequest(memberId: Int): Result<Unit> {
        return try {
            withContext(dispatcher) {
                service.deleteCircleJoinRequest(memberId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    companion object {
        private const val TEXT_CONTENT_TYPE = "text/plain; charset=utf-8"
        private const val IMAGE_CONTENT_TYPE = "image/jpeg; charset=utf-8"
        private const val SORT_CIRCLE_OLDEST = "createdAt,asc"
        private const val SORT_CIRCLE_LATEST = "createdAt,desc"
        private const val SORT_MEMBER_BY_NAME = "username,asc"
        private const val TYPE_POST = "POST"
        private const val TYPE_NOTICE = "NOTICE"
    }
}
