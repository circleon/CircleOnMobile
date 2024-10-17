package com.developeek.circleon.data.exception

object ExceptionMessage {
    const val TAG_ERROR_STATUS = "[Error Status]"
    const val UNDEFINED_STATUS_CODE = "정의되지 않은 응답 코드입니다"
    const val UNDEFINED_DETAIL_CODE = "정의되지 않은 상세 코드입니다"
    const val NO_RESPONSE_BODY = "응답 데이터가 존재하지 않습니다"
    const val MESSAGE_FAIL_INTERNET_CONNECTION = "인터넷 연결에 실패하였습니다"
    const val MESSAGE_FAIL_REQUEST = "요청 실패 [ErrorCode : %d]"

    const val MESSAGE_INPUT_VALIDATION_NO_DATA = "*%s (을)를 입력해주시기 바랍니다"
    const val MESSAGE_WRONG_FORMAT_EMAIL = "*이메일 주소 형식이 올바르지 않습니다"
    const val MESSAGE_WRONG_FORMAT_PASSWORD = "*비밀번호 형식이 올바르지 않습니다"
    const val MESSAGE_WRONG_PASSWORD_CHECK = "*비밀번호가 일치하지 않습니다"
}
