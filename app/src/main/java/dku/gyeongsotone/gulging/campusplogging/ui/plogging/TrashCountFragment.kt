package dku.gyeongsotone.gulging.campusplogging.ui.plogging

import android.app.ActionBar
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.setMargins
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dku.gyeongsotone.gulging.campusplogging.BR
import dku.gyeongsotone.gulging.campusplogging.R
import dku.gyeongsotone.gulging.campusplogging.databinding.FragmentPloggingFinishBinding
import dku.gyeongsotone.gulging.campusplogging.databinding.FragmentTrashCountBinding
import dku.gyeongsotone.gulging.campusplogging.ui.custom.TrashCountView
import dku.gyeongsotone.gulging.campusplogging.utils.Constant
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.FILE_PROVIDER
import dku.gyeongsotone.gulging.campusplogging.utils.dpToPx
import java.io.File


class TrashCountFragment : Fragment() {
    companion object {
        private val TAG = this::class.java.name
    }

    private lateinit var imageUri: Uri
    private lateinit var imageFile: File

    private lateinit var binding: FragmentTrashCountBinding
    private val viewModel: PloggingViewModel by activityViewModels()

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                viewModel.picture = MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    imageUri
                )
                navigateToPloggingFinishFragment()
            } else {
                showToast("사진을 불러오지 못했습니다. 다시 시도해주세요.")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        init(inflater, container)
        setOnClickListener()
        addTrashCountViews()
        (requireActivity() as PloggingActivity).setBackPressable(false)

        return binding.root
    }

    /** binding 설정 */
    private fun init(inflater: LayoutInflater, container: ViewGroup?) {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_trash_count,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setSpannableText()
    }

    /** spannable text 설정 */
    private fun setSpannableText() {
        val titleTextViewSpannable = SpannableStringBuilder("모은 쓰레기를 입력하세요")
        titleTextViewSpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.main_yellow)),
            0,
            6,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        binding.tvTitle.text = titleTextViewSpannable
    }


    /** 클릭 리스너 설정 */
    private fun setOnClickListener() {
        binding.btnNext.setOnClickListener { onNextBtnClick() }
    }

    /** 사진 촬영 버튼 클릭 시 사진 촬영 후 plogging finish 프래그먼트로 이동 */
    private fun onNextBtnClick() {
        imageFile = File.createTempFile("picture_", ".png")
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "fileprovider",
            imageFile
        )

        takePicture.launch(imageUri)
    }

    private fun navigateToPloggingFinishFragment() {
        findNavController().navigate(
            TrashCountFragmentDirections.actionTrashInputFragmentToPloggingFinishFragment()
        )
    }

    /** grid layout에 trash count views 추가*/
    private fun addTrashCountViews() {
        for (trashKind in Constant.TRASH_TYPES_KOR) {
            val newView = TrashCountView(requireContext())
            val countsInViewModel = when (trashKind) {
                "플라스틱" -> viewModel.plastics
                "비닐" -> viewModel.vinyls
                "유리" -> viewModel.glasses
                "캔" -> viewModel.cans
                "종이" -> viewModel.papers
                "일반쓰레기" -> viewModel.generals
                else -> null
            }

            // set text and click listener
            newView.binding.tvTrashKind.text = trashKind
            newView.binding.ivMinus.setOnClickListener {
                val count = countsInViewModel?.get()
                if (count != null) {
                    if (count > 0) countsInViewModel.set(count - 1)
                }
            }
            newView.binding.ivPlus.setOnClickListener {
                val count = countsInViewModel?.get()
                if (count != null) {
                    if (count < 99) countsInViewModel.set(count + 1)
                }
            }

            // set data binding variable
            newView.binding.setVariable(BR.trashCount, countsInViewModel)

            // add view on grid layout
            binding.gridLayout.addView(newView)

            // set grid layout params
            (newView.layoutParams as GridLayout.LayoutParams).apply {
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}