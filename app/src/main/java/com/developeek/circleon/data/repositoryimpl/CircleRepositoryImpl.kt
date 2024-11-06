package com.developeek.circleon.data.repositoryimpl

import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.repository.CircleRepository
import com.developeek.circleon.data.source.Result
import com.developeek.circleon.data.source.remote.retrofit.service.CircleService
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleModels
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
        category: Category?,
    ): Result<CircleModels> {
        return try {
            withContext(dispatcher) {
                val response =
                    if (category == null) {
                        service.getAllCircles(page, size, "createdAt,desc")
                    } else {
                        service.getCircles(page, size, "createdAt,desc", category.codeName())
                    }
                Result.success(CircleModels(response.content.map { it.toCircleModel() }))
            }
        } catch (e: ServiceException.NoResultException) {
            Result.success(CircleModels.emptyInstance())
        } catch (e: IOException) {
            Result.error(e)
        }
    }
}
