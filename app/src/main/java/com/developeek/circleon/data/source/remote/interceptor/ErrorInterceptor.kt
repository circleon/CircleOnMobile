package com.developeek.circleon.data.source.remote.interceptor

import android.util.Log
import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.exception.ServiceExceptionMessage
import com.developeek.circleon.data.source.remote.retrofit.StatusCode
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
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
            val responseCode = response.code
            val responseString = response.peekBody(Long.MAX_VALUE).string()
            val contentType = response.header(PARAM_NAME_CONTENT_TYPE)

            if (responseString.isEmpty()) {
                throw IOException(ServiceExceptionMessage.MESSAGE_NO_RESPONSE_BODY)
            }

            try {
                val jsonObject = JSONTokener(responseString).nextValue() as JSONObject
                val statusCode = findStatusCode(responseCode, jsonObject)

                errorLog(statusCode)
                throwException(statusCode)

                val newResponseBody = responseString.toResponseBody(contentType!!.toMediaType())
                return response.newBuilder().body(newResponseBody).build()
            } catch (e: ClassCastException) {
                throw IOException(ServiceExceptionMessage.MESSAGE_FAIL_REQUEST)
            }
        }

        private fun findStatusCode(
            responseCode: Int,
            data: JSONObject,
        ): StatusCode {
            if (responseCode == StatusCode.SUCCESS.responseCode()) {
                return StatusCode.SUCCESS
            }
            if (responseCode == StatusCode.NO_CONTENTS.responseCode()) {
                return StatusCode.NO_CONTENTS
            }

            val errorCode = parseErrorCode(data)
            val errorMessage = parseErrorMessage(data)

            try {
                return StatusCode.entries.single { it.isSame(responseCode, errorCode) }
            } catch (e: NoSuchElementException) {
                throw IOException(errorMessage)
            }
        }

        private fun parseErrorCode(data: JSONObject): String {
            try {
                return data.getString(PARAM_NAME_ERROR_CODE)
            } catch (e: JSONException) {
                throw IOException(ServiceExceptionMessage.MESSAGE_WRONG_RESPONSE_FORMAT)
            }
        }

        private fun parseErrorMessage(data: JSONObject): String {
            try {
                return data.getString(PARAM_NAME_ERROR_MESSAGE)
            } catch (e: JSONException) {
                throw IOException(ServiceExceptionMessage.MESSAGE_WRONG_RESPONSE_FORMAT)
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

        private fun throwException(statusCode: StatusCode) {
            when (statusCode) {
                StatusCode.SUCCESS -> {}
                StatusCode.NO_CONTENTS -> {
                    throw ServiceException.NoResultException(statusCode.message())
                }
                // 서버 혹은 통신 문제인 경우
                StatusCode.WRONG_REQUEST_FORMAT,
                StatusCode.WRONG_INPUT_DATA_FORMAT,
                StatusCode.SERVER_ERROR,
                -> {
                    throw IOException(
                        String.format(ServiceExceptionMessage.MESSAGE_FAIL_REQUEST, statusCode.errorCode()),
                    )
                }
                else -> {
                    throw IOException(statusCode.message())
                }
            }
        }

        companion object {
            private const val PARAM_NAME_ERROR_CODE = "errorCode"
            private const val PARAM_NAME_ERROR_MESSAGE = "errorMessage"
            private const val PARAM_NAME_CONTENT_TYPE = "content-type"
        }
    }
