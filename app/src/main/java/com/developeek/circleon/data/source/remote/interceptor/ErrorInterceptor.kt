package com.developeek.circleon.data.source.remote.interceptor

import com.developeek.circleon.data.exception.ExceptionMessage
import com.developeek.circleon.data.source.remote.retrofit.StatusCode
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import javax.inject.Inject

class ErrorInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)

            val newResponse = parseResponse(response)

            return newResponse
        }

        private fun parseResponse(response: Response): Response {
            val responseCode = response.code()
            val responseBody = response.body()
            val contentType = response.header(PARAM_NAME_CONTENT_TYPE)

            if (responseBody != null) {
                val responseString = responseBody.string()
                val jsonObject = JSONTokener(responseString).nextValue() as JSONObject

                val detailCode = parseDetailCode(jsonObject)
                val statusCode = findStatusCode(responseCode, detailCode)

                throwExceptionWhenErrorStatus(statusCode)

                val newResponseBody = ResponseBody.create(MediaType.get(contentType!!), responseString)
                return response.newBuilder().body(newResponseBody).build()
            } else {
                throw IOException(ExceptionMessage.NO_RESPONSE_BODY)
            }
        }

        private fun parseDetailCode(data: JSONObject): String {
            try {
                return data.getString(PARAM_NAME_DETAIL_CODE)
            } catch (e: JSONException) {
                throw IOException(ExceptionMessage.UNDEFINED_DETAIL_CODE)
            }
        }

        private fun findStatusCode(
            responseCode: Int,
            detailCode: String,
        ): StatusCode {
            try {
                return StatusCode.entries.single { it.isSame(responseCode, detailCode) }
            } catch (e: NoSuchElementException) {
                throw IOException(ExceptionMessage.UNDEFINED_STATUS_CODE)
            }
        }

        private fun throwExceptionWhenErrorStatus(statusCode: StatusCode) {
            when (statusCode) {
                StatusCode.SUCCESS,
                StatusCode.FAIL_ACCESS_TOKEN_VALIDATION, StatusCode.FAIL_REFRESH_TOKEN_VALIDATION,
                -> {}
                else -> {
                    throw IOException(statusCode.message())
                }
            }
        }

        companion object {
            // TODO: param name 확정되면 수정 필요
            private const val PARAM_NAME_DETAIL_CODE = "detailCode"
            private const val PARAM_NAME_CONTENT_TYPE = "content-type"
        }
    }
