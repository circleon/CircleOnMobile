package com.developeek.circleon.data.source.remote.retrofit

enum class StatusCode(
    private val responseCode: Int,
    private val errorCode: String,
    private val message: String,
) {
    SUCCESS(200, "0", "성공"),
    NO_CONTENTS(204, "0", "컨텐츠 없음"),
    WRONG_REQUEST_FORMAT(400, "001", "요청 형식이 잘못되었습니다"),
    WRONG_INPUT_DATA_FORMAT(400, "005", "입력 정보 형식이 잘못되었습니다"),
    NO_FILE_DATA(400, "008", "파일이 존재하지 않습니다"),
    WRONG_USER_DATA_FORMAT(400, "022", "사용자 정보 형식이 잘못되었습니다"),
    UNEXPIRED_AUTHENTICATION_CODE(400, "025", "인증 코드가 아직 만료되지 않았습니다"),
    NO_ISSUED_AUTHENTICATION_CODE(400, "027", "인증 코드 발급을 먼저 요청해주세요"),
    FAIL_AUTHENTICATE_NO_MATCH_AUTHENTICATION_CODE(400, "028", "유효하지 않은 인증 코드입니다"),
    EXPIRED_AUTHENTICATION_CODE(400, "029", "인증 코드가 만료되었습니다"),
    FAIL_SIGN_UP_NO_MATCH_UNIVERSITY(400, "030", "허용된 대학 이메일 도메인이 아닙니다"),
    FAIL_ACCESS_TOKEN_VALIDATION(401, "002", "access 토큰 검증 실패"),
    FAIL_REFRESH_TOKEN_VALIDATION(401, "003", "refresh 토큰 검증 실패"),
    NO_AUTHORIZATION(403, "004", "해당 권한이 없습니다"),
    FAIL_LOGIN_NO_MATCH_USER_EMAIL(404, "023", "아이디를 확인해주세요"),
    FAIL_LOGIN_NO_MATCH_USER_PASSWORD(404, "024", "비밀번호를 확인해주세요"),
    NO_USER_DATA(404, "032", "존재하지 않는 사용자 정보입니다"),
    NO_CIRCLE_DATA(404, "041", "존재하지 않는 동아리입니다"),
    NOT_SIGNED_CIRCLE(404, "042", "가입하지 않은 동아리입니다"),
    ALREADY_SIGNED_CIRCLE(404, "044", "이미 가입된 동아리입니다"),
    ALREADY_REQUESTED_CIRCLE_SIGN_UP(404, "045", "이미 가입 신청 상태입니다"),
    NO_REQUESTED_CIRCLE_SIGN_UP(404, "046", "가입 신청 정보가 존재하지 않습니다"),
    NO_POST_DATA(404, "061", "게시글 정보가 존재하지 않습니다"),
    NO_COMMENT_DATA(404, "063", "댓글 정보가 존재하지 않습니다"),
    NO_CALENDAR_DATA(404, "071", "일정 정보가 존재하지 않습니다"),
    FAIL_EMAIL_VALIDATION(409, "021", "중복 이메일이 존재합니다"),
    FAIL_CIRCLE_RESIGN_UNREGISTERED(409, "041", "동아리 탈퇴 실패 - 미가입"),
    OVER_REQUEST_LIMIT(429, "026", "너무 많은 인증 시도가 발생했습니다"),
    SERVER_ERROR(500, "5", "서버 에러"), ;

    fun isSame(
        responseCode: Int,
        detailCode: String,
    ) = this.responseCode == responseCode && this.errorCode == detailCode

    fun responseCode() = this.responseCode

    fun errorCode() = this.errorCode

    fun message() = this.message
}
