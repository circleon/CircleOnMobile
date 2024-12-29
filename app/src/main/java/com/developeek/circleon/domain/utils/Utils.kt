package com.developeek.circleon.domain.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import com.developeek.circleon.BuildConfig

object Utils {
    // TODO: local properties 로 이동
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

    fun changeMenuItemTextColor(
        item: MenuItem,
        color: Int,
    ) {
        val title = SpannableString(item.title)
        item.title =
            title.apply {
                setSpan(
                    ForegroundColorSpan(color),
                    0,
                    length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE,
                )
            }
    }
}
