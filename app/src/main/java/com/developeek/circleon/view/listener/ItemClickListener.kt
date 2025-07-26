package com.developeek.circleon.view.listener

import android.view.View

/**
 * ItemClickListener
 *
 * 아이템 클릭 리스너를 onCreateViewHolder 에서 설정하기 위해 listener 객체를 onCreate 안에서 새로 초기화해주는 것이 핵심
 * 초기화 단계에서 설정할 리스너 코드를 어댑터 외부에서 받을 수 있도록 별도의 ItemListenerInitializer 작성
 * ItemListenerInitializer 의 onCreate 를 외부에서 작성받아 초기화 단계에서 ViewHolder 에 적용
 */
interface ItemClickListener<T> : View.OnClickListener {
    var item: T
}

/**
 * ItemListenerInitializer
 *
 * 아이템 클릭 리스너 구현부를 외부 클래스에서 입력받기 위한 리스너 중개 클래스
 * initialize 에 리스너 구현부를 설정하고 ItemClickListener 의 onClick 에서 initialize 를 실행한다
 */
interface ItemListenerInitializer<T> {
    fun initialize(item: T)

    fun initialize(
        item: T,
        view: View?,
    )
}
