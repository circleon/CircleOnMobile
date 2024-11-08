package com.developeek.circleon.domain.utils.glide

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.developeek.circleon.R
import com.developeek.circleon.data.source.remote.interceptor.TokenManager
import javax.inject.Inject

class GlideProvider
    @Inject
    constructor(private val tokenManager: TokenManager) {
        fun callImage(
            url: String,
            parent: Context,
            view: ImageView,
        ) {
            var glideUrl: GlideUrl? = null

            tokenManager.getAccessToken()?.let {
                glideUrl =
                    GlideUrl(
                        url.substring(0, url.length - 5),
                        LazyHeaders.Builder()
                            .addHeader(AUTHORIZATION, "Bearer $it")
                            .addHeader(CONTENT_TYPE, "image/jpeg")
                            .build(),
                    )
            }

            if (glideUrl == null) {
                Glide.with(parent)
                    .load(url)
                    .placeholder(R.drawable.logo_main)
                    .into(view)
            } else {
                Glide.with(parent)
                    .load(glideUrl)
                    .error(R.drawable.logo_main)
                    .into(view)
            }
        }

        companion object {
            private const val AUTHORIZATION = "Authorization"
            private const val CONTENT_TYPE = "Content-Type"
        }
    }
