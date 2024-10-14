package com.developeek.circleon.data.source.remote.interceptor

import okhttp3.Request
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRequester
    @Inject
    constructor() {
        private val requests = LinkedList<Request>()
        private lateinit var first: Request

        fun add(request: Request) {
            requests.add(request)
            if (requests.isEmpty()) first = request
        }

        fun get() = if (requests.isNotEmpty()) requests.pop() else null

        fun first() = first
    }
