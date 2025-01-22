package com.developeek.circleon.domain.utils.glide

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.developeek.circleon.data.source.manager.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlideProvider
    @Inject
    constructor(private val tokenManager: TokenManager) {
        fun fetchImage(
            url: String,
            parent: Context,
            view: ImageView,
        ) {
            var glideUrl: GlideUrl? = null

            tokenManager.getAccessToken()?.let {
                val imageExtension = getImageExtension(url)

                glideUrl =
                    GlideUrl(
                        url.substring(0, url.length - imageExtension.length - 1),
                        LazyHeaders.Builder()
                            .addHeader(AUTHORIZATION, "Bearer $it")
                            .addHeader(CONTENT_TYPE, "image/$imageExtension")
                            .build(),
                    )
            }

            glideUrl?.let {
                Glide.with(parent)
                    .load(it)
                    .into(view)
            }
        }

        private fun getImageExtension(url: String): String {
            var symbolIdx = 0

            for (i in (url.length - 1) downTo 0) {
                if (url[i] == EXTENSION_SYMBOL) {
                    symbolIdx = i
                    break
                }
            }

            return url.substring(symbolIdx + 1, url.length)
        }

        fun loadImage(
            uri: Uri,
            parent: Context,
            view: ImageView,
        ) {
            Glide.with(parent)
                .load(uri)
                .into(view)
        }

        companion object {
            private const val AUTHORIZATION = "Authorization"
            private const val CONTENT_TYPE = "Content-Type"
            private const val EXTENSION_SYMBOL = '.'
        }
    }
