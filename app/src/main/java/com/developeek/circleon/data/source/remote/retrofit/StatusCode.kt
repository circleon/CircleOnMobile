package com.developeek.circleon.data.source.remote.retrofit

enum class StatusCode(
    private val responseCode: Int,
    private val detailCode: String,
    private val message: String,
) {
    SUCCESS(200, "0", "성공"),
    NO_CONTENTS(204, "0", "컨텐츠 없음"),
    WRONG_REQUEST_FORMAT(400, "001", "요청 형식 에러"),
    WRONG_INPUT_DATA_FORMAT(400, "005", "입력 정보 형식 에러"),
    UNEXPIRED_AUTHENTICATION_CODE(400, "025", "인증 코드가 아직 만료되지 않았습니다"),
    NO_ISSUED_AUTHENTICATION_CODE(400, "027", "인증 코드 발급을 먼저 요청해주세요"),
    FAIL_AUTHENTICATE_NO_MATCH_AUTHENTICATION_CODE(400, "028", "유효하지 않은 인증 코드입니다"),
    EXPIRED_AUTHENTICATION_CODE(400, "029", "인증 코드가 만료되었습니다"),
    FAIL_SIGN_UP_NO_MATCH_UNIVERSITY(400, "030", "허용된 대학 이메일 도메인이 아닙니다"),
    FAIL_ACCESS_TOKEN_VALIDATION(401, "002", "access 토큰 검증 실패"),
    FAIL_REFRESH_TOKEN_VALIDATION(401, "003", "refresh 토큰 검증 실패"),
    FAIL_LOGIN_NO_MATCH_USER_EMAIL(401, "023", "아이디를 확인해주세요"),
    FAIL_LOGIN_NO_MATCH_USER_PASSWORD(401, "024", "비밀번호를 확인해주세요"),
    NO_AUTHORIZATION(403, "004", "권한 없음"),
    FAIL_EMAIL_VALIDATION(409, "021", "중복 이메일이 존재합니다"),
    FAIL_CIRCLE_RESIGN_UNREGISTERED(409, "041", "동아리 탈퇴 실패 - 미가입"),
    OVER_REQUEST_LIMIT(429, "026", "너무 많은 인증 시도가 발생했습니다"),
    SERVER_ERROR(500, "5", "서버 에러"), ;

    fun isSame(
        responseCode: Int,
        detailCode: String,
    ) = this.responseCode == responseCode && this.detailCode == detailCode

    fun code() = this.responseCode

    fun message() = this.message
}
