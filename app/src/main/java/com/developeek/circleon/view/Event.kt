package com.developeek.circleon.view

/**
 * Event
 *
 * UI 레이어 전체에서 공통으로 사용되는 이벤트
 * ScreenFlow 와 달리, 한 번 수행한 이벤트를 중복 수행하지 않기 위해 SharedFlow 를 사용할 것을 권장한다.
 */
sealed class Event {
    data object SendToLoginScreen : Event()

    data class ShowToast(val message: String) : Event()

    data class ShowDialog(val message: String) : Event()

    // Loading == 화면 주요 데이터 처리 작업, Processing == 단순 요청 작업
    data object ShowProcessing : Event()

    data object EndProcessing : Event()
}
