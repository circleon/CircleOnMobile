package com.developeek.circleon.view.screen.auth

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentSignUpTermsBinding
import com.developeek.circleon.view.viewmodel.auth.SignUpViewModel
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpScreenEvent
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpStep
import com.developeek.circleon.view.viewmodelimpl.auth.SignUpViewModelImpl
import kotlinx.coroutines.launch

class SignUpTermsFragment : Fragment() {
    private lateinit var binding: FragmentSignUpTermsBinding
    private val viewModel: SignUpViewModel by activityViewModels<SignUpViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignUpTermsBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initListener(requireContext())
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signUpScreenEvent.collect {
                    handleSignUpScreenEvent(it, requireContext())
                }
            }
        }
    }

    private fun initListener(context: Context) {
        setBtnAgreeTermsListener()
        setBtnTermsDetailListener(context)
    }

    private fun setBtnAgreeTermsListener() {
        binding.btnAgreeAllTerms.setOnClickListener {
            viewModel.toggleAllTermsAgreement()
        }
        binding.btnAgreeServiceTerms.setOnClickListener {
            viewModel.toggleServiceTermsAgreement()
        }
        binding.btnAgreePrivacyPolicy.setOnClickListener {
            viewModel.togglePrivacyPolicyAgreement()
        }
        binding.btnAgreeCommunityRules.setOnClickListener {
            viewModel.toggleCommunityRulesAgreement()
        }
    }

    private fun setBtnTermsDetailListener(context: Context) {
        binding.btnServiceTermsDetail.setOnClickListener {
            sendUserToLinkPage(context.getString(R.string.site_service_terms))
        }
        binding.btnPrivacyPolicyDetail.setOnClickListener {
            sendUserToLinkPage(context.getString(R.string.site_privacy_policy))
        }
        binding.btnCommunityRulesDetail.setOnClickListener {
            sendUserToLinkPage(context.getString(R.string.site_community_rules))
        }
    }

    private fun sendUserToLinkPage(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))

        startActivity(intent)
    }

    private fun handleSignUpScreenEvent(
        event: SignUpScreenEvent,
        context: Context,
    ) {
        when (event) {
            is SignUpScreenEvent.UpdateSignUpProcess -> updateSignUpProcess(event, context)
        }
    }

    private fun updateSignUpProcess(
        event: SignUpScreenEvent.UpdateSignUpProcess,
        context: Context,
    ) {
        if (event.step != SignUpStep.TERMS) return

        loadTermsAgreement(context)
    }

    private fun loadTermsAgreement(context: Context) {
        binding.checkAgreeAllTerms.imageTintList =
            getSingleColorStateListByTermState(viewModel.signUpManager.hasAgreedAllTerms, context)
        binding.checkAgreeServiceTerms.imageTintList =
            getSingleColorStateListByTermState(viewModel.signUpManager.hasAgreedServiceTerms, context)
        binding.checkAgreePrivacyPolicy.imageTintList =
            getSingleColorStateListByTermState(viewModel.signUpManager.hasAgreedPrivacyPolicies, context)
        binding.checkAgreeCommunityRules.imageTintList =
            getSingleColorStateListByTermState(viewModel.signUpManager.hasAgreedCommunityRules, context)
    }

    private fun getSingleColorStateListByTermState(
        hasAgreed: Boolean,
        context: Context,
    ): ColorStateList {
        val color =
            when (hasAgreed) {
                true -> context.getColor(R.color.purple_5)
                false -> context.getColor(R.color.grey_5)
            }

        return ColorStateList.valueOf(color)
    }
}
