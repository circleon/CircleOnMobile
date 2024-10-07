package com.developeek.circleon.data.source.remote.interceptor

import com.developeek.circleon.data.source.remote.retrofit.StatusCode
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class ErrorInterceptor : Interceptor {
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
            // TODO: throw ResponseException("No Response Body")
        }
    }

    private fun parseDetailCode(data: JSONObject): String {
        try {
            return data.getString(PARAM_NAME_DETAIL_CODE)
        } catch (e: JSONException) {
            // TODO: throw ResponseException("No Detail Code")
        }
    }

    private fun findStatusCode(
        responseCode: Int,
        detailCode: String,
    ): StatusCode {
        try {
            return StatusCode.entries.single { it.isSame(responseCode, detailCode) }
        } catch (e: NoSuchElementException) {
            // TODO: throw ResponseException("Undefined Status Code")
        }
    }

    private fun throwExceptionWhenErrorStatus(statusCode: StatusCode) {
        // TODO: throw Service Exception By Status
    }

    companion object {
        // TODO: param name 확정되면 수정 필요
        private const val PARAM_NAME_DETAIL_CODE = "detailCode"
        private const val PARAM_NAME_CONTENT_TYPE = "content-type"
    }
}
