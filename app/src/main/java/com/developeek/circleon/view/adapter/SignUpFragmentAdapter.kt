package com.developeek.circleon.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.developeek.circleon.view.screen.auth.SignUpEmailAuthenticationFragment
import com.developeek.circleon.view.screen.auth.SignUpEmailFragment
import com.developeek.circleon.view.screen.auth.SignUpNameFragment
import com.developeek.circleon.view.screen.auth.SignUpPasswordFragment

class SignUpFragmentAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifeCycle) {
    private val fragments =
        listOf(
            SignUpNameFragment(),
            SignUpEmailFragment(),
            SignUpEmailAuthenticationFragment(),
            SignUpPasswordFragment(),
        )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
