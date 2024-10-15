package com.developeek.circleon.data.exception

object ExceptionMessage {
    const val TAG_ERROR_STATUS = "[Error Status]"
    const val UNDEFINED_STATUS_CODE = "정의되지 않은 응답 코드입니다"
    const val UNDEFINED_DETAIL_CODE = "정의되지 않은 상세 코드입니다"
    const val NO_RESPONSE_BODY = "응답 데이터가 존재하지 않습니다"

    const val MESSAGE_FAIL_INTERNET_CONNECTION = "인터넷 연결에 실패하였습니다"
    const val MESSAGE_FAIL_REQUEST = "요청 실패 [ErrorCode : %d]"
}
