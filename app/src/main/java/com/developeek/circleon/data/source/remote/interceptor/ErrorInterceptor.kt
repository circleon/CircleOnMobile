package com.developeek.circleon.data.source.remote.interceptor

import android.util.Log
import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.exception.ServiceExceptionMessage
import com.developeek.circleon.data.source.remote.retrofit.StatusCode
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import javax.inject.Inject

class ErrorInterceptor
    @Inject
    constructor(
        private val tokenManager: TokenManager,
        private val tokenRequester: TokenRequester,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)

            val newResponse = parseResponse(response, request)

            return newResponse
        }

        private fun parseResponse(
            response: Response,
            request: Request,
        ): Response {
            val responseCode = response.code()
            val responseBody = response.body()
            val contentType = response.header(PARAM_NAME_CONTENT_TYPE)

            if (responseBody != null) {
                val responseString = responseBody.string()
                val jsonObject = JSONTokener(responseString).nextValue() as JSONObject

                val detailCode = parseDetailCode(jsonObject)
                val statusCode = findStatusCode(responseCode, detailCode)

                errorLog(statusCode)
                throwException(statusCode, request)

                val newResponseBody = ResponseBody.create(MediaType.get(contentType!!), responseString)
                return response.newBuilder().body(newResponseBody).build()
            } else {
                throw IOException(ServiceExceptionMessage.NO_RESPONSE_BODY)
            }
        }

        private fun parseDetailCode(data: JSONObject): String {
            try {
                return data.getString(PARAM_NAME_DETAIL_CODE)
            } catch (e: JSONException) {
                throw IOException(ServiceExceptionMessage.UNDEFINED_DETAIL_CODE)
            }
        }

        private fun findStatusCode(
            responseCode: Int,
            detailCode: String,
        ): StatusCode {
            try {
                return StatusCode.entries.single { it.isSame(responseCode, detailCode) }
            } catch (e: NoSuchElementException) {
                throw IOException(ServiceExceptionMessage.UNDEFINED_STATUS_CODE)
            }
        }

        private fun errorLog(statusCode: StatusCode) {
            when (statusCode) {
                StatusCode.SUCCESS -> {}
                else -> {
                    Log.e(ServiceExceptionMessage.TAG_ERROR_STATUS, statusCode.message())
                }
            }
        }

        private fun throwException(
            statusCode: StatusCode,
            request: Request,
        ) {
            when (statusCode) {
                StatusCode.SUCCESS -> {}
                StatusCode.NO_CONTENTS -> {
                    throw ServiceException.NoResultException(statusCode.message())
                }
                StatusCode.FAIL_ACCESS_TOKEN_VALIDATION -> {
                    tokenManager.getRefreshToken()
                        ?: throw ServiceException.RefreshTokenExpiredException(statusCode.message())
                    tokenRequester.add(request)
                }
                StatusCode.FAIL_REFRESH_TOKEN_VALIDATION -> {
                    throw ServiceException.RefreshTokenExpiredException(statusCode.message())
                }
                // 서버 혹은 통신 문제인 경우
                StatusCode.WRONG_REQUEST_FORMAT,
                StatusCode.WRONG_INPUT_DATA_FORMAT,
                StatusCode.SERVER_ERROR,
                -> {
                    throw IOException(String.format(ServiceExceptionMessage.MESSAGE_FAIL_REQUEST, statusCode.code()))
                }
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
