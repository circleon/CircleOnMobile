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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentUploadCircleBinding
import com.developeek.circleon.domain.model.CategoryModel
import com.developeek.circleon.domain.model.CircleDetailModel
import com.developeek.circleon.domain.model.Models
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.Utils.toJPEG
import com.developeek.circleon.domain.utils.glide.GlideProvider
import com.developeek.circleon.view.Event
import com.developeek.circleon.view.adapter.CategoryAdapter
import com.developeek.circleon.view.listener.ItemListenerInitializer
import com.developeek.circleon.view.screen.auth.LoginActivity
import com.developeek.circleon.view.viewmodel.circle.UploadCircleViewModel
import com.developeek.circleon.view.viewmodelimpl.circle.UploadCircleScreen
import com.developeek.circleon.view.viewmodelimpl.circle.UploadCircleScreenEvent
import com.developeek.circleon.view.viewmodelimpl.circle.UploadCircleViewModelImpl
import com.developeek.circleon.view.widget.SingleMessageAlertDialog
import com.developeek.circleon.view.widget.SingleMessageToast
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class UploadCircleFragment : Fragment() {
    private lateinit var binding: FragmentUploadCircleBinding
    private var isEdit = false
    private var origin: CircleDetailModel = CircleDetailModel.empty()
    private val viewModel: UploadCircleViewModel by viewModels<UploadCircleViewModelImpl>(
        extrasProducer = {
            defaultViewModelCreationExtras
                .withCreationCallback<UploadCircleViewModelImpl.UploadCircleViewModelFactory> {
                    it.create(origin, isEdit)
                }
        },
    )
    private val circleThumbnailPickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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
        initListener(requireContext())
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.event.collect {
                        handleEvent(it, requireActivity(), requireContext())
                    }
                }
                launch {
                    viewModel.screenFlow.collect {
                        handleScreenFlow(it)
                    }
                }
                launch {
                    viewModel.uploadCircleScreenEvent.collect {
                        handleUploadCircleScreenEvent(it, requireContext())
                    }
                }
            }
        }
    }

    private fun initView(
        parentActivity: Activity,
        context: Context,
    ) {
        initToolbar(context)
        initCategoryRecyclerView(context)
        hideBtmNav(parentActivity)
        setBtnSaveEditOrUpload()
        loadCircleContent(origin, context)
        loadCircleThumbnailWhenIsNotNull(context, origin)
        loadCircleIntroductionImageWhenIsNotNull(context, origin)
    }

    private fun hideBtmNav(activity: Activity) {
        activity.findViewById<BottomNavigationView>(R.id.btmNav).isVisible = false
    }

    private fun setBtnSaveEditOrUpload() {
        if (isEdit) {
            binding.btnSaveEdit.isVisible = true
            return
        }

        binding.btnUpload.isVisible = true
    }

    private fun initToolbar(context: Context) {
        val title =
            if (isEdit) {
                context.getString(R.string.title_edit_circle)
            } else {
                context.getString(R.string.title_upload_circle)
            }

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

    private fun loadCircleContent(
        circle: CircleDetailModel,
        context: Context,
    ) {
        binding.edtCircleName.setText(circle.name)
        binding.switchRecruitment.isChecked = circle.recruiting
        circle.recruitmentStartDate?.let {
            binding.txtRecruitmentStartDate.text =
                it.format(
                    DateTimeFormatter.ofPattern(
                        RECRUITMENT_DATE_FORMAT,
                    ),
                )
        }
        circle.recruitmentEndDate?.let {
            binding.txtRecruitmentEndDate.text =
                it.format(
                    DateTimeFormatter.ofPattern(
                        RECRUITMENT_DATE_FORMAT,
                    ),
                )
        }
        binding.edtCircleSingleLineIntroduction.setText(circle.singleLineIntroduction)
        binding.txtCurrentCircleSingleIntroductionSize.text =
            circle.singleLineIntroduction.length.toString()
        binding.edtCircleIntroduction.setText(circle.introduction)
        selectCategory(CategoryModel.selectAndGetWithoutALL(circle.category))
        toggleRecruitmentLock(circle.recruiting, context)
    }

    private fun loadCircleThumbnailWhenIsNotNull(
        context: Context,
        circle: CircleDetailModel,
    ) {
        circle.thumbnailUrl?.let {
            glideProvider.fetchImage(
                it,
                context,
                binding.btnAddCircleThumbnail,
            )
            binding.btnAddOrRemoveCircleThumbnail.setImageResource(R.drawable.ic_cancel_2)
        }
    }

    private fun loadCircleIntroductionImageWhenIsNotNull(
        context: Context,
        circle: CircleDetailModel,
    ) {
        circle.introImgUrl?.let {
            glideProvider.fetchImage(
                it,
                context,
                binding.btnAddCircleIntroductionImage,
            )
            binding.btnAddCircleIntroductionImage.background = null
            binding.btnRemoveCircleIntroductionImage.isVisible = true
        }
    }

    private fun handleEvent(
        event: Event,
        parentActivity: Activity,
        context: Context,
    ) {
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

    private fun handleScreenFlow(screenFlow: UploadCircleScreen) {
        binding.pgbLoading.isVisible = screenFlow is UploadCircleScreen.LoadingView
        when (screenFlow) {
            is UploadCircleScreen.SuccessView -> {
                requestRefreshToPreviousScreen()
                sendUserToPreviousScreen()
            }
            else -> {}
        }
    }

    private fun requestRefreshToPreviousScreen() {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(Const.FLAG_CIRCLE_DATA_CHANGED, true)
    }

    private fun sendUserToPreviousScreen() {
        findNavController().popBackStack()
    }

    private fun handleUploadCircleScreenEvent(
        event: UploadCircleScreenEvent,
        context: Context,
    ) {
        when (event) {
            is UploadCircleScreenEvent.ToggleRecruitmentLock -> toggleRecruitmentLock(event.isRecruiting, context)
            is UploadCircleScreenEvent.SelectCategory -> selectCategory(event.categories)
        }
    }

    private fun toggleRecruitmentLock(
        isRecruiting: Boolean,
        context: Context,
    ) {
        if (isRecruiting) {
            unlockRecruitment(context)
            return
        }

        lockRecruitment(context)
    }

    private fun lockRecruitment(context: Context) {
        binding.btnEditRecruitmentStartDate.isClickable = false
        binding.btnEditRecruitmentEndDate.isClickable = false
        binding.txtRecruitmentStartDate.setTextColor(context.getColor(R.color.grey_5))
        binding.txtRecruitmentEndDate.setTextColor(context.getColor(R.color.grey_5))
    }

    private fun unlockRecruitment(context: Context) {
        binding.btnEditRecruitmentStartDate.isClickable = true
        binding.btnEditRecruitmentEndDate.isClickable = true
        binding.txtRecruitmentStartDate.setTextColor(context.getColor(R.color.grey_9))
        binding.txtRecruitmentEndDate.setTextColor(context.getColor(R.color.grey_9))
    }

    private fun selectCategory(categories: Models<CategoryModel>) {
        binding.rvCircleCategory.adapter?.let {
            (it as CategoryAdapter).update(categories) {}
        }
    }

    private fun initListener(context: Context) {
        setBtnCancelListener()
        setBtnSaveEditListener()
        setBtnUploadListener()
        setBtnAddCircleThumbnailListener()
        setBtnAddCircleIntroductionImageListener()
        setSwitchRecruitment()
        setBtnEditRecruitmentDate(requireContext())
        setBtnEdtCircleContentListener()
        setBtnAddOrRemoveCircleThumbnailListener()
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

    private fun setSwitchRecruitment() {
        binding.switchRecruitment.setOnCheckedChangeListener { _, checked ->
            viewModel.toggleRecruitmentLock(checked)
        }
    }

    private fun setBtnEditRecruitmentDate(context: Context) {
        setBtnEditRecruitmentStartDate(context)
        setBtnEditRecruitmentEndDate(context)
    }

    private fun setBtnEditRecruitmentStartDate(context: Context) {
        val onDataSetListener =
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                val date = LocalDateTime.of(y, m + 1, d, 0, 0)

                binding.txtRecruitmentStartDate.text = date.format(DateTimeFormatter.ofPattern(RECRUITMENT_DATE_FORMAT))
                viewModel.setRecruitmentStartDate(date)
            }
        binding.btnEditRecruitmentStartDate.setOnClickListener {
            showDatePickerDialog(context, onDataSetListener, viewModel.circle.recruitmentStartDate)
        }
    }

    private fun setBtnEditRecruitmentEndDate(context: Context) {
        val onDataSetListener =
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                val date = LocalDateTime.of(y, m + 1, d, 0, 0)

                binding.txtRecruitmentEndDate.text = date.format(DateTimeFormatter.ofPattern(RECRUITMENT_DATE_FORMAT))
                viewModel.setRecruitmentEndDate(date)
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

    private fun setBtnAddOrRemoveCircleThumbnailListener() {
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

    override fun onDestroyView() {
        super.onDestroyView()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) // softInputMode 복원
    }

    companion object {
        private const val RECRUITMENT_DATE_FORMAT = "yyyy.MM.dd"
        private const val PHOTO_MIME_TYPE = "image/jpeg"
    }
}
