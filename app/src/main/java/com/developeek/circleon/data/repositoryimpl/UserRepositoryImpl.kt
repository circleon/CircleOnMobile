package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.dto.auth.LogoutRequestBody
import com.developeek.circleon.data.dto.home.RequestResponseBodyCircleJoin
import com.developeek.circleon.data.dto.home.RequestResponseBodyCircleLeave
import com.developeek.circleon.data.repository.Page
import com.developeek.circleon.data.repository.UserRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.manager.TokenManager
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.data.source.remote.retrofit.service.UserService
import com.developeek.circleon.domain.enums.MembershipStatus
import com.developeek.circleon.domain.model.CircleSummaryModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.model.MyPostModel
import com.developeek.circleon.domain.model.UserModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepositoryImpl(
    private val service: UserService,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
) : UserRepository {
    override fun getUser(): Result<UserModel> {
        return try {
            val user = userManager.getUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.success(UserModel.empty())
        }
    }

    override suspend fun getMyJoinRequestedCircles(
        page: Int,
        size: Int,
    ): Result<Models<CircleSummaryModel>> {
        return try {
            val response = service.getMyCircles(MembershipStatus.JOIN_REQUESTED.codeName(), page, size)
            Result.success(Models(response.content.map { it.toCircleSummaryModel() }))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getMyLeaveRequestedCircles(
        page: Int,
        size: Int,
    ): Result<Models<CircleSummaryModel>> {
        return try {
            val response = service.getMyCircles(MembershipStatus.LEAVE_REQUESTED.codeName(), page, size)
            Result.success(Models(response.content.map { it.toCircleSummaryModel() }))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getMyPosts(
        page: Int,
        size: Int,
    ): Result<Page<MyPostModel>> {
        return try {
            val response = service.getMyPosts(page, size, SORT_LATEST)
            Result.success(
                Page(response.content.map { it.toMyPostModel() }).apply {
                    if (response.isLastPage()) {
                        setAsLast()
                    }
                },
            )
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getMyCommentPosts(
        page: Int,
        size: Int,
    ): Result<Page<MyPostModel>> {
        return try {
            val response = service.getMyCommentPosts(page, size, SORT_LATEST)
            Result.success(
                Page(response.content.map { it.toMyPostModel() }).apply {
                    if (response.isLastPage()) {
                        setAsLast()
                    }
                },
            )
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getMyCircles(
        page: Int,
        size: Int,
    ): Result<Models<CircleSummaryModel>> {
        return try {
            val response = service.getMyCircles(MembershipStatus.JOINED.codeName(), page, size)
            Result.success(Models(response.content.map { it.toCircleSummaryModel() }))
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

    override suspend fun putUser(): Result<Unit> {
        return try {
            val response = service.getUser()
            userManager.setUser(response)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun putUserProfileImage(profileImage: File?): Result<Unit> {
        return try {
            val profileImg =
                if (profileImage == null) {
                    null
                } else {
                    val imageRequestBody = profileImage.asRequestBody(IMAGE_CONTENT_TYPE.toMediaType())
                    MultipartBody.Part.createFormData("image", profileImage.name, imageRequestBody)
                }
            service.putUserProfileImage(profileImg)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteCircleJoinRequest(memberId: Int): Result<Unit> {
        return try {
            service.deleteCircleJoinRequest(memberId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteUserProfileImage(): Result<Unit> {
        return try {
            service.deleteUserProfileImage()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.getRefreshToken()?.let {
                service.logout(LogoutRequestBody(it))
            }
            tokenManager.deleteAccessToken()
            tokenManager.deleteRefreshToken()
            userManager.deleteUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun resign(): Result<Unit> {
        return try {
            service.resign()
            tokenManager.deleteAccessToken()
            tokenManager.deleteRefreshToken()
            userManager.deleteUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    companion object {
        private const val IMAGE_CONTENT_TYPE = "image/jpeg; charset=utf-8"
        private const val SORT_LATEST = "createdAt,desc"
    }
}
