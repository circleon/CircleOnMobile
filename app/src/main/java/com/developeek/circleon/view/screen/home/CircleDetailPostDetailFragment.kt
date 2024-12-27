package com.developeek.circleon.view.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.developeek.circleon.R
import com.developeek.circleon.databinding.FragmentCircleDetailPostDetailBinding
import com.developeek.circleon.domain.model.PostModel
import com.developeek.circleon.domain.utils.Const
import com.developeek.circleon.domain.utils.glide.GlideProvider
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class CircleDetailPostDetailFragment : Fragment() {
    private lateinit var binding: FragmentCircleDetailPostDetailBinding
    private lateinit var post: PostModel

    @Inject
    lateinit var glideProvider: GlideProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            post = it.getSerializable(Const.TAG_CIRCLE_POST) as PostModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCircleDetailPostDetailBinding.inflate(layoutInflater)

        post.imgUrl?.let {
            binding.imgPost.isVisible = true
        }
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
    }

    private fun initView() {
        loadAuthor()
        loadPost()
    }

    private fun loadAuthor() {
        binding.txtAuthorName.text = post.author.name
        binding.txtCreated.text =
            post.createdAt.format(
                DateTimeFormatter.ofPattern(CREATED_DATE_FORMAT),
            )
        post.author.profileUrl?.let {
            glideProvider.callImage(it, requireActivity(), binding.imgAuthorProfile)
        } ?: binding.imgAuthorProfile.setImageResource(R.drawable.ic_author_placeholder)
    }

    private fun loadPost() {
        binding.txtPostContent.text = post.content
        post.imgUrl?.let {
            glideProvider.callImage(it, requireActivity(), binding.imgPost)
        }
    }

    private fun initListener() {
        setBtnBackListener()
    }

    private fun setBtnBackListener() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    companion object {
        private const val CREATED_DATE_FORMAT = "M월 d일 hh:mm"
    }
}
