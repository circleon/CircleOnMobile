package com.developeek.circleon.view.screen.mypage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.BuildConfig
import com.developeek.circleon.R
import com.developeek.circleon.data.source.manager.UserManager
import com.developeek.circleon.databinding.FragmentMyPageBinding
import com.developeek.circleon.domain.model.UserModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils.toJPEG
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.mypage.MyPageViewModel
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPageScreenEvent
import com.developeek.circleon.view.viewmodelimpl.mypage.MyPageViewModelImpl
import com.developeek.circleon.view.widget.PositiveAlertDialog
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MyPageFragment : Fragment() {
    private lateinit var binding: FragmentMyPageBinding
    private val viewModel: MyPageViewModel by viewModels<MyPageViewModelImpl>()
    private val userProfilePickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                showSetUserProfileDialog(it.toJPEG(requireContext()), requireContext())
            }
        }

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().findViewById<BottomNavigationView>(R.id.btmNav).selectedItemId = R.id.nav_graph_home
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyPageBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireContext())
        initListener(requireContext())
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, requireActivity(), requireContext())
                    }
                }
                launch {
                    viewModel.myPageScreenEvent.collect {
                        handleMyPageScreenEvent(it, requireContext())
                    }
                }
            }
        }
    }

    private fun initView(context: Context) {
        initVersionName(context)
        userManager.getUser()?.let {
            loadUserInfo(it, context)
        }
    }

    private fun initVersionName(context: Context) {
        binding.txtVersionName.text =
            String.format(context.getString(R.string.version_name), BuildConfig.VERSION_NAME)
    }

    private fun loadUserInfo(
        user: UserModel,
        context: Context,
    ) {
        binding.txtUserName.text = user.name
        user.profileImage?.let { url ->
            glideProvider.fetchImage(url, context, binding.btnAddUserProfile)
            binding.btnAddOrRemoveUserProfile.setImageResource(R.drawable.ic_cancel_2)
        } ?: run {
            binding.btnAddUserProfile.setImageResource(R.drawable.img_user_profile_large_default)
            binding.btnAddOrRemoveUserProfile.setImageResource(R.drawable.ic_add_2)
        }
    }

    private fun initListener(context: Context) {
        setBtnMyPostsListener()
        setBtnProfileImageListener(context)
        setBtnSendFeedbackListener()
        setBtnLogoutListener(context)
    }

    private fun setBtnMyPostsListener() {
        binding.btnMyPosts.setOnClickListener {
            sendUserToMyPostScreen(true)
        }
        binding.btnMyCommentPosts.setOnClickListener {
            sendUserToMyPostScreen(false)
        }
    }

    private fun sendUserToMyPostScreen(isMyPosts: Boolean) {
        val bundle = Bundle()

        bundle.putBoolean(Const.TAGE_MY_POSTS, isMyPosts)
        findNavController().navigate(R.id.action_myPageFragment_to_myPostFragment, bundle)
    }

    private fun setBtnProfileImageListener(context: Context) {
        userManager.getUser()?.let {
            it.profileImage?.let { _ ->
                binding.btnAddOrRemoveUserProfile.setOnClickListener {
                    showRemoveUserProfileDialog(context)
                }
            } ?: binding.btnAddOrRemoveUserProfile.setOnClickListener {
                pickImage()
            }
            binding.btnAddUserProfile.setOnClickListener {
                pickImage()
            }
        }
    }

    private fun setBtnSendFeedbackListener() {
        binding.btnSendFeedback.setOnClickListener {
            // sendFeedback
        }
    }

    private fun setBtnLogoutListener(context: Context) {
        binding.btnLogout.setOnClickListener {
            showLogoutDialog(context)
        }
    }

    private fun showLogoutDialog(context: Context) {
        PositiveAlertDialog(
            context,
            message = context.getString(R.string.message_user_logout),
            positiveListener = {
                viewModel.logout()
            },
        ).show()
    }

    private fun pickImage() {
        userProfilePickMedia.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.SingleMimeType(
                    PHOTO_MIME_TYPE,
                ),
            ),
        )
    }

    private fun showSetUserProfileDialog(
        image: File?,
        context: Context,
    ) {
        PositiveAlertDialog(
            context,
            message = context.getString(R.string.message_set_user_profile_image),
            positiveListener = {
                viewModel.setUserProfileImage(image)
            },
        ).show()
    }

    private fun showRemoveUserProfileDialog(context: Context) {
        PositiveAlertDialog(
            context,
            message = context.getString(R.string.message_remove_user_profile_image),
            positiveListener = {
                viewModel.removeUserProfileImage()
            },
        ).show()
    }

    private fun handleEvent(
        event: Event,
        parentActivity: Activity,
        context: Context,
    ) {
        binding.pgbLoading.isVisible = event is Event.ShowProcessing
        when (event) {
            is Event.SendToLoginScreen -> sendUserToLoginScreen(parentActivity)
            is Event.ShowToast -> showToast(event, context)
            is Event.ShowDialog -> showDialog(event, context)
            else -> {}
        }
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showToast(
        event: Event.ShowToast,
        context: Context,
    ) {
        if (SingleMessageToast.previousFinished()) {
            SingleMessageToast(context, event.message).show()
        }
    }

    private fun showDialog(
        event: Event.ShowDialog,
        context: Context,
    ) {
        SingleMessageAlertDialog(context, event.message).show()
    }

    private fun handleMyPageScreenEvent(
        event: MyPageScreenEvent,
        context: Context,
    ) {
        when (event) {
            is MyPageScreenEvent.SetUserProfileImage -> setUserProfileImage(context)
            is MyPageScreenEvent.RemoveUserProfileImage -> removeUserProfileImage()
        }
    }

    private fun setUserProfileImage(context: Context) {
        binding.btnAddOrRemoveUserProfile.setImageResource(R.drawable.ic_cancel_2)
        binding.btnAddOrRemoveUserProfile.setOnClickListener {
            showRemoveUserProfileDialog(context)
        }
        userManager.getUser()?.profileImage?.let {
            glideProvider.fetchImage(it, context, binding.btnAddUserProfile)
        }
    }

    private fun removeUserProfileImage() {
        binding.btnAddUserProfile.setImageResource(R.drawable.img_user_profile_large_default)
        binding.btnAddOrRemoveUserProfile.setImageResource(R.drawable.ic_add_2)
        binding.btnAddOrRemoveUserProfile.setOnClickListener {
            pickImage()
        }
    }

    companion object {
        private const val PHOTO_MIME_TYPE = "image/jpeg"
    }
}
