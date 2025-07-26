package com.developeek.circleon.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.developeek.circleon.view.screen.auth.ChangePasswordEmailAuthenticationFragment
import com.developeek.circleon.view.screen.auth.ChangePasswordEmailFragment
import com.developeek.circleon.view.screen.auth.ChangePasswordPasswordFragment

class ChangePasswordFragmentAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifeCycle) {
    private val fragments =
        listOf(
            ChangePasswordEmailFragment(),
            ChangePasswordEmailAuthenticationFragment(),
            ChangePasswordPasswordFragment(),
        )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
