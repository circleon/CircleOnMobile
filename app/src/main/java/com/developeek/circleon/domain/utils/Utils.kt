package com.developeek.circleon.domain.utils

import com.developeek.circleon.BuildConfig

object Utils {
    private const val CIRCLE_IMAGE_PATH = "circles/images/"
    private const val POST_IMAGE_PATH = "posts/images/"

    fun circleImageUrl(url: String?): String? {
        url ?: return null

        return BuildConfig.SERVICE_API_URL + CIRCLE_IMAGE_PATH + url
    }

    fun postImageUrl(url: String?): String? {
        url ?: return null

        return BuildConfig.SERVICE_API_URL + POST_IMAGE_PATH + url
    }
}
