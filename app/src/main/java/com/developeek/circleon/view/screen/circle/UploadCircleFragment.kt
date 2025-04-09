package com.developeek.circleon.view.screen.circle

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentUploadCircleBinding
import com.developeek.circleon.domain.model.CategoryModel
import com.developeek.circleon.domain.model.CategoryModels
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.state.UiState
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils.toJPEG
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.adapter.CategoryAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.screen.login.LoginActivity
import com.developeek.circleon.view.viewmodel.circle.UploadCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.circle.UploadCircleViewModelImpl
import com.developeek.circleon.view.widget.ErrorAlertDialog
import com.developeek.circleon.view.widget.ErrorToast
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class UploadCircleFragment : Fragment() {
    private lateinit var binding: FragmentUploadCircleBinding
    private var isEdit = false
    private var origin: CircleDetailModel? = null
    private val viewModel: UploadCircleViewModel by viewModels<UploadCircleViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<UploadCircleViewModelImpl.UploadCircleViewModelFactory> {
                    it.create(origin)
                }
        },
    )
    private val circleThumbnailPickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            uri?.let {
                glideProvider.loadImage(it, requireContext(), binding.btnAddCircleThumbnail)
                viewModel.setCircleProfileImage(it.toJPEG(requireContext()))
                binding.btnAddOrRemoveCircleThumbnail.setImageResource(R.drawable.ic_cancel_2)
                binding.btnAddOrRemoveCircleThumbnail.setOnClickListener {
                    onBtnRemoveCircleThumbnailClicked()
                }
            }
        }
    private val circleIntroductionImagePickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            uri?.let {
                glideProvider.loadImage(it, requireContext(), binding.btnAddCircleIntroductionImage)
                viewModel.setCircleIntroductionImage(it.toJPEG(requireContext()))
                binding.btnRemoveCircleIntroductionImage.isVisible = true
                binding.btnAddCircleIntroductionImage.background = null
            }
        }

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isEdit =
                it.getBoolean(Const.FLAG_EDIT_SCREEN).also { isEdit ->
                    if (isEdit) {
                        origin = it.getSerializable(Const.TAG_CIRCLE_DETAIL) as CircleDetailModel
                    }
                }
        }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUploadCircleBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView(requireActivity(), requireContext())
        initObserver(requireActivity(), requireContext())
        initListener(requireContext())
    }

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initCategoryRecyclerView(context)
        initToolbar()
        hideBtmNav(parentActivity)
        setBtnSaveEditOrUpload()
        loadCircleContent()
        loadCircleThumbnailWhenIsNotNull(context)
        loadCircleIntroductionImageWhenIsNotNull(context)
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun setBtnSaveEditOrUpload() {
        if (isEdit) {
            binding.btnSaveEdit.isVisible = true
        } else {
            binding.btnUpload.isVisible = true
        }
    }

    private fun initToolbar() {
        val title = if (isEdit) TITLE_CIRCLE_EDIT else TITLE_CIRCLE_NEW

        binding.txtTbTitle.text = title
    }

    private fun initCategoryRecyclerView(context: Context) {
        binding.rvCircleCategory.adapter =
            CategoryAdapter(
                context,
                object : ItemListenerInitializer<CategoryModel> {
                    override fun initialize(item: CategoryModel) {
                        viewModel.setCategory(item.category)
                    }

                    override fun initialize(
                        item: CategoryModel,
                        view: View?,
                    ) {
                    }
                },
            )
        binding.rvCircleCategory.layoutManager = FlexboxLayoutManager(context, FlexDirection.ROW)
        binding.rvCircleCategory.itemAnimator = null
    }

    private fun loadCircleContent() {
        binding.edtCircleName.setText(viewModel.circle.name)
        viewModel.circle.recruitmentStartDate?.let {
            binding.txtRecruitmentStartDate.text =
                it.format(
                    DateTimeFormatter.ofPattern(
                        RECRUITMENT_DATE_FORMAT,
                    ),
                )
        }
        viewModel.circle.recruitmentEndDate?.let {
            binding.txtRecruitmentEndDate.text =
                it.format(
                    DateTimeFormatter.ofPattern(
                        RECRUITMENT_DATE_FORMAT,
                    ),
                )
        }
        binding.edtCircleSingleLineIntroduction.setText(viewModel.circle.singleLineIntroduction)
        binding.txtCurrentCircleSingleIntroductionSize.text =
            viewModel.circle.singleLineIntroduction.length.toString()
        binding.edtCircleIntroduction.setText(viewModel.circle.introduction)
    }

    private fun loadCircleThumbnailWhenIsNotNull(context: Context) {
        viewModel.circle.thumbnailUrl?.let {
            glideProvider.fetchImage(
                it,
                context,
                binding.btnAddCircleThumbnail,
            )
            binding.btnAddOrRemoveCircleThumbnail.setImageResource(R.drawable.ic_cancel_2)
        }
    }

    private fun loadCircleIntroductionImageWhenIsNotNull(context: Context) {
        viewModel.circle.introImgUrl?.let {
            glideProvider.fetchImage(
                it,
                context,
                binding.btnAddCircleIntroductionImage,
            )
            binding.btnAddCircleIntroductionImage.background = null
            binding.btnRemoveCircleIntroductionImage.isVisible = true
        }
    }

    private fun initObserver(
        parentActivity: Activity,
        context: Context,
    ) {
        viewModel.state.observe(
            viewLifecycleOwner,
            stateObserver(parentActivity, context),
        )
        viewModel.categories.observe(
            viewLifecycleOwner,
            categoriesObserver(),
        )
    }

    private fun stateObserver(
        parentActivity: Activity,
        context: Context,
    ) = Observer<UiState> {
        binding.pgbLoading.isVisible = it is UiState.Loading
        when (it) {
            UiState.Success -> {
                requestRefreshToPreviousScreen()
                sendUserToPreviousScreen()
            }
            UiState.AuthenticationError -> {
                sendUserToLoginScreen(parentActivity)
                if (ErrorToast.previousFinished()) {
                    ErrorToast(context, viewModel.error).show()
                }
            }
            UiState.ServiceError -> {
                ErrorAlertDialog(context, viewModel.error).show()
            }
            else -> {}
        }
    }

    private fun categoriesObserver() =
        Observer<CategoryModels> { categories ->
            binding.rvCircleCategory.adapter?.let {
                (it as CategoryAdapter).update(categories) {}
            }
        }

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_CIRCLE_DATA_CHANGED, true)
    }

    private fun initListener(context: Context) {
        setBtnCancelListener()
        setBtnSaveEditListener()
        setBtnUploadListener()
        setBtnAddCircleThumbnailListener()
        setBtnAddCircleIntroductionImageListener()
        setBtnEditRecruitmentDate(requireContext())
        setBtnEdtCircleContentListener()
        setBtnAddOrRemoveCircleThumbnailListener(context)
        setBtnRemoveCircleIntroductionImageListener(context)
        setEdtSingleLineIntroductionListener()
    }

    private fun setBtnCancelListener() {
        binding.btnCancel.setOnClickListener {
            sendUserToPreviousScreen()
        }
    }

    private fun setBtnSaveEditListener() {
        binding.btnSaveEdit.setOnClickListener {
            viewModel.edit()
        }
    }

    private fun setBtnUploadListener() {
        binding.btnUpload.setOnClickListener {
            viewModel.upload()
        }
    }

    private fun setBtnAddCircleThumbnailListener() {
        binding.btnAddCircleThumbnail.setOnClickListener {
            onBtnAddCircleThumbnailClicked()
        }
    }

    private fun setBtnAddCircleIntroductionImageListener() {
        binding.btnAddCircleIntroductionImage.setOnClickListener {
            circleIntroductionImagePickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.SingleMimeType(
                        PHOTO_MIME_TYPE,
                    ),
                ),
            )
        }
    }

    private fun setBtnEditRecruitmentDate(context: Context) {
        setBtnEditRecruitmentStartDate(context)
        setBtnEditRecruitmentEndDate(context)
    }

    private fun setBtnEditRecruitmentStartDate(context: Context) {
        val onDataSetListener =
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                viewModel.setRecruitmentStartDate(LocalDateTime.of(y, m + 1, d, 0, 0))
                binding.txtRecruitmentStartDate.text =
                    viewModel.circle.recruitmentStartDate!!.format(
                        DateTimeFormatter.ofPattern(
                            RECRUITMENT_DATE_FORMAT,
                        ),
                    )
            }
        binding.btnEditRecruitmentStartDate.setOnClickListener {
            showDatePickerDialog(context, onDataSetListener, viewModel.circle.recruitmentStartDate)
        }
    }

    private fun setBtnEditRecruitmentEndDate(context: Context) {
        val onDataSetListener =
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                viewModel.setRecruitmentEndDate(LocalDateTime.of(y, m + 1, d, 0, 0))
                binding.txtRecruitmentEndDate.text =
                    viewModel.circle.recruitmentEndDate!!.format(
                        DateTimeFormatter.ofPattern(
                            RECRUITMENT_DATE_FORMAT,
                        ),
                    )
            }
        binding.btnEditRecruitmentEndDate.setOnClickListener {
            showDatePickerDialog(context, onDataSetListener, viewModel.circle.recruitmentEndDate)
        }
    }

    private fun showDatePickerDialog(
        context: Context,
        onDataSetListener: DatePickerDialog.OnDateSetListener,
        currentDate: LocalDateTime? = LocalDateTime.now(),
    ) {
        val date = currentDate ?: LocalDateTime.now()
        DatePickerDialog(
            context,
            android.R.style.Theme_Material_Dialog,
            onDataSetListener,
            date.year,
            date.monthValue - 1,
            date.dayOfMonth,
        ).show()
    }

    private fun setBtnEdtCircleContentListener() {
        setBtnEdtCircleNameListener()
        setBtnEdtSingleLineIntroductionListener()
        setBtnEdtCircleIntroductionListener()
    }

    private fun setBtnEdtCircleNameListener() {
        binding.edtCircleName.doAfterTextChanged {
            it?.let {
                viewModel.setCircleName(it.toString())
            }
        }
    }

    private fun setBtnEdtSingleLineIntroductionListener() {
        binding.edtCircleSingleLineIntroduction.doAfterTextChanged {
            it?.let {
                viewModel.setSingleLineIntroduction(it.toString())
            }
        }
    }

    private fun setBtnEdtCircleIntroductionListener() {
        binding.edtCircleIntroduction.doAfterTextChanged {
            it?.let {
                viewModel.setIntroduction(it.toString())
            }
        }
    }

    private fun setBtnAddOrRemoveCircleThumbnailListener(context: Context) {
        if (viewModel.circle.thumbnailUrl == null) {
            binding.btnAddOrRemoveCircleThumbnail.setOnClickListener {
                pickImage()
            }
        } else {
            binding.btnAddOrRemoveCircleThumbnail.setOnClickListener {
                onBtnRemoveCircleThumbnailClicked()
            }
        }
    }

    private fun onBtnAddCircleThumbnailClicked() {
        pickImage()
    }

    private fun onBtnRemoveCircleThumbnailClicked() {
        viewModel.removeCircleProfileImage()
        binding.btnAddCircleThumbnail.setImageResource(R.drawable.img_circle_profile_large_default)
        binding.btnAddOrRemoveCircleThumbnail.setImageResource(R.drawable.ic_add_2)
        binding.btnAddOrRemoveCircleThumbnail.setOnClickListener {
            onBtnAddCircleThumbnailClicked()
        }
    }

    private fun pickImage() {
        circleThumbnailPickMedia.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.SingleMimeType(
                    PHOTO_MIME_TYPE,
                ),
            ),
        )
    }

    private fun setBtnRemoveCircleIntroductionImageListener(context: Context) {
        binding.btnRemoveCircleIntroductionImage.setOnClickListener {
            viewModel.removeCircleIntroductionImage()
            binding.btnAddCircleIntroductionImage.setImageResource(R.drawable.ic_add)
            binding.btnRemoveCircleIntroductionImage.isVisible = false
            binding.btnAddCircleIntroductionImage.setBackgroundDrawable(
                ContextCompat.getDrawable(context, R.drawable.bg_dotted_rounded_rectangle),
            )
        }
    }

    private fun setEdtSingleLineIntroductionListener() {
        binding.edtCircleSingleLineIntroduction.doAfterTextChanged {
            if (it == null) {
                binding.txtCurrentCircleSingleIntroductionSize.text = "0"
            } else {
                binding.txtCurrentCircleSingleIntroductionSize.text = it.length.toString()
            }
        }
    }

    private fun sendUserToPreviousScreen() {
        findNavController().navigateUp()
    }

    private fun sendUserToLoginScreen(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    companion object {
        private const val RECRUITMENT_DATE_FORMAT = "yyyy.MM.dd"
        private const val TITLE_CIRCLE_NEW = "동아리 생성"
        private const val TITLE_CIRCLE_EDIT = "동아리 수정"
        private const val PHOTO_MIME_TYPE = "image/jpeg"
    }
}
