package dku.gyeongsotone.gulging.campusplogging.ui.plogging

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dku.gyeongsotone.gulging.campusplogging.APP
import dku.gyeongsotone.gulging.campusplogging.R
import dku.gyeongsotone.gulging.campusplogging.data.local.dao.PloggingDao
import dku.gyeongsotone.gulging.campusplogging.data.local.model.Plogging
import dku.gyeongsotone.gulging.campusplogging.data.repository.PloggingRepository
import dku.gyeongsotone.gulging.campusplogging.databinding.FragmentPloggingFinishBinding
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_LEVEL
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.SP_TOTAL_DISTANCE
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.UNIV_DISTANCE
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.getSpDouble
import dku.gyeongsotone.gulging.campusplogging.utils.PreferenceUtil.getSpInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.floor


class PloggingFinishFragment : Fragment() {
    companion object {
        private val TAG = this::class.java.name
    }

    private lateinit var binding: FragmentPloggingFinishBinding
    private val viewModel: PloggingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.setBadges()
        viewModel.saveOnDatabase()
        init(inflater, container)
        setClickListener()
        showTrashCount()
        (requireActivity() as PloggingActivity).setBackPressable(false)

        return binding.root
    }

    /** binding 설정 */
    private fun init(inflater: LayoutInflater, container: ViewGroup?) {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_plogging_finish,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
    }

    /** 클릭 리스너 설정 */
    private fun setClickListener() {
        binding.layoutPloggingSummary.btnShare.setOnClickListener { onShareBtnClick() }
        binding.layoutPloggingSummary.btnExit.setOnClickListener { onExitBtnClick() }
    }

    private fun onShareBtnClick() {
        val bitmap = getBitmapFromView(binding)
        val file = File.createTempFile("picture_", ".png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        val uri =
            FileProvider.getUriForFile(requireContext(), "fileprovider", file)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }

        val shareIntent = Intent.createChooser(sendIntent, "플로깅 기록을 공유하세요.")
        startActivity(shareIntent)
    }

    private fun getBitmapFromView(binding: FragmentPloggingFinishBinding): Bitmap {
        val bitmap: Bitmap?

        binding.layoutPloggingSummary.btnShare.isVisible = false
        binding.layoutPloggingSummary.btnExit.isVisible = false
        bitmap = binding.layoutPloggingSummary.layout.drawToBitmap()
        binding.layoutPloggingSummary.btnShare.isVisible = true
        binding.layoutPloggingSummary.btnExit.isVisible = true

        return bitmap
    }

    private fun onExitBtnClick() {
        Log.d(TAG, "onExitBtnClicked!!!")
        requireActivity().finish()
    }


    /** 쓰레기 합계 계산 후 set text */
    private fun showTrashCount() {
        var total = 0
        total += viewModel.plastics.get()
        total += viewModel.vinyls.get()
        total += viewModel.glasses.get()
        total += viewModel.cans.get()
        total += viewModel.papers.get()
        total += viewModel.generals.get()
        binding.layoutPloggingSummary.tvTrashCount.text = resources.getString(R.string.count, total)
    }
}