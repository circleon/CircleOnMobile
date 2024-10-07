package com.developeek.circleon.data.source.remote.retrofit

enum class StatusCode(
    private val responseCode: Int,
    private val detailCode: String,
    private val message: String,
) {
    SUCCESS(200, "0", "성공"),
    NO_CONTENTS(204, "0", "컨텐츠 없음"),
    WRONG_REQUEST_FORMAT(400, "001", "요청 형식 에러"),
    WRONG_USER_DATA_FORMAT(400, "022", "유저 정보 형식 에러"),
    WRONG_CIRCLE_DATA_FORMAT(400, "031", "동아리 정보 형식 에러"),
    WRONG_CIRCLE_PLAN_DATA_FORMAT(400, "032", "동아리 일정 정보 형식 에러"),
    WRONG_POST_DATA_FORMAT(400, "051", "게시글 관련 정보 형식 에러"),
    FAIL_ACCESS_TOKEN_VALIDATION(401, "002", "access 토큰 검증 실패"),
    FAIL_REFRESH_TOKEN_VALIDATION(401, "003", "refresh 토큰 검증 실패"),
    FAIL_LOGIN_NO_USER_EMAIL(401, "023", "로그인 실패 - 이메일 불일치"),
    FAIL_LOGIN_NO_USER_PASSWORD(401, "024", "로그인 실패 - 비밀번호 불일치"),
    NO_AUTHORIZATION(403, "004", "권한 없음"),
    FAIL_EMAIL_VALIDATION(409, "021", "이메일 중복 검증 실패"),
    FAIL_CIRCLE_RESIGN_UNREGISTERED(409, "041", "동아리 탈퇴 실패 - 미가입"),
    SERVER_ERROR(500, "5", "서버 에러"), ;

    fun isSame(
        responseCode: Int,
        detailCode: String,
    ) = this.responseCode == responseCode && this.detailCode == detailCode

    fun message() = this.message
}
