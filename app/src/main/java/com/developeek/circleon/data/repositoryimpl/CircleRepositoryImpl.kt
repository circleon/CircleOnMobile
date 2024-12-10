package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.remote.retrofit.service.CircleService
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.model.CircleSummaryModels
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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
                        service.getCircles(page, size, SORT_LATEST, category.codeName())
                    }
                Result.success(
                    CircleModels(response.content.map { it.toCircleModel() }).also {
                        if (response.isLastPage()) {
                            it.setAsLast()
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

    companion object {
        private const val SORT_LATEST = "createdAt,desc"
    }
}
