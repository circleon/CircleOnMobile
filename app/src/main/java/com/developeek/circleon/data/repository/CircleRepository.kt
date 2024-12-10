package com.developeek.circleon.data.repository

import com.developeek.circleon.data.source.Result
import com.developeek.circleon.domain.enums.Category
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.CircleModels
import com.developeek.circleon.domain.model.CircleSummaryModels

interface CircleRepository {
    suspend fun getCircles(
        page: Int,
        size: Int,
        category: Category,
    ): Result<CircleModels>

    suspend fun getCircleSummaries(): Result<CircleSummaryModels>

    suspend fun getCircleDetail(circleId: Int): Result<CircleDetailModel>
}
