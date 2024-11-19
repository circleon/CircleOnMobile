package com.developeek.circleon.data.source

import com.developeek.circleon.data.exception.ServiceException
import com.developeek.circleon.data.exception.ServiceExceptionMessage
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class Result<T> {
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)

        fun <T> error(error: Exception): Result<T> = Error(error)
    }
}

class Success<T>(val data: T) : Result<T>()

class Error<T>(private val error: Exception) : Result<T>() {
    fun message() =
        when (error) {
            is UnknownHostException -> {
                ServiceExceptionMessage.MESSAGE_FAIL_INTERNET_CONNECTION
            }
            is SocketTimeoutException -> {
                ServiceExceptionMessage.MESSAGE_SOCKET_TIMEOUT_EXCEPTION
            }
            is ServiceException.RefreshTokenExpiredException -> {
                ServiceExceptionMessage.MESSAGE_REFRESH_EXPIRED
            }
            else -> {
                error.message ?: error.toString()
            }
        }

    fun isRefreshExpired() = error is ServiceException.RefreshTokenExpiredException
}
