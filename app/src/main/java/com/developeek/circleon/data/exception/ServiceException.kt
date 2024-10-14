package com.developeek.circleon.data.exception

import java.io.IOException

object ServiceException {
    /**
     * NoResultException
     *
     * 통신 결과 컨텐츠가 없는 경우 발생하는 Exception
     * 경우에 따라 Critical 정도가 달라지기 때문에 Repository 에서 취할 action 을 구분하기 위해 제작
     */
    class NoResultException(override val message: String) : IOException(message)

    class RefreshTokenExpiredException(override val message: String) : IOException(message)
}
