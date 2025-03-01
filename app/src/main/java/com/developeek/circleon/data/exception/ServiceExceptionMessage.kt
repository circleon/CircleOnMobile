package com.developeek.circleon.data.exception

object ServiceExceptionMessage {
    const val TAG_ERROR_STATUS = "[Error Status]"
    const val MESSAGE_NO_RESPONSE_BODY = "응답 데이터가 존재하지 않습니다"
    const val MESSAGE_WRONG_RESPONSE_FORMAT = "응답 형식이 올바르지 않습니다"
    const val MESSAGE_FAIL_REQUEST = "데이터 요청에 실패하였습니다 [ErrorCode : %s]"
    const val MESSAGE_FAIL_INTERNET_CONNECTION = "인터넷 연결에 실패하였습니다"
    const val MESSAGE_FAIL_SERVER_CONNECTION = "서버 연결에 실패하였습니다"
    const val MESSAGE_SOCKET_TIMEOUT_EXCEPTION = "요청 시간이 초과되었습니다. 다시 시도해주세요"
    const val MESSAGE_REFRESH_EXPIRED = "인증이 만료되었습니다. 다시 로그인해주세요"
}
