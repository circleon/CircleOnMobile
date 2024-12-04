package com.developeek.circleon.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.developeek.circleon.databinding.ItemCirclePostBinding
import com.developeek.circleon.view.listener.OverflowClickListener

class CircleNoticeAdapter(
    private val activity: Activity,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /**
     * CircleNoticeAdapterItemViewHolder
     *
     * 아이템 리스너의 경우 뷰홀더를 만들 때 설정을 하는 것이 올바르지만, 리스너에서 아이템 각각의 데이터를 필요로하는 경우
     * onCreateViewHolder 가 아닌 onBind 에서 리스너가 계속 재설정되는 비효율적 방식을 개선하기 위해
     *
     * 1. 커스텀 리스너 객체를 뷰홀더에서 저장
     * 2. 커스텀 리스너는 아이템 id 값만 갱신하는 별도 기능을 통해 onBind 에서 아이템 내용이 바뀌었을 때 식별값만 갱신
     */
    inner class CircleNoticeAdapterItemViewHolder(
        private val binding: ItemCirclePostBinding,
        private val overflowClickListener: OverflowClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            // TODO: post id 로 수정 필요
            overflowClickListener.onBind(0)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            ItemCirclePostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )

        val overflowClickListener = OverflowClickListener(activity)
        binding.btnNoticeOverflow.setOnClickListener(overflowClickListener)
        return CircleNoticeAdapterItemViewHolder(binding, overflowClickListener)
    }

    override fun getItemCount() = 10

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        (holder as CircleNoticeAdapterItemViewHolder).onBind(position)
    }
}
