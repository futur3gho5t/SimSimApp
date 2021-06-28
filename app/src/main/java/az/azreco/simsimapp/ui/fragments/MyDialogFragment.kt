package az.azreco.simsimapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import az.azreco.simsimapp.R
import az.azreco.simsimapp.scenario.HomeScenario
import az.azreco.simsimapp.util.SpeechLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottom_dialog.*
import javax.inject.Inject


open class MyDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var homeScenario: HomeScenario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initResponses()
        SpeechLiveData.isSynthesizes.observe(viewLifecycleOwner, {
            showWaveLoader(it)
        })
        SpeechLiveData.ttsQuestion.observe(viewLifecycleOwner, {
            tvQuestionDialogFragment.text = it
        })
        SpeechLiveData.kwsResponse.observe(viewLifecycleOwner, {
            tvResponseDialogFragment.text = it
        })

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initResponses() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        params.setMargins(5)
        SpeechLiveData.responseGroup.observe(viewLifecycleOwner, { list ->
            val responseList: List<String> = if (list.size > 3) {
                list.take(3)
            } else {
                list
            }
            responseList.forEach {
                val textView = TextView(requireContext()).apply {
                    layoutParams = params
                    gravity = Gravity.CENTER
                    background =
                        resources.getDrawable(R.drawable.rounded_textview_response, null)
                    setPadding(10)
                    text = it
                }
                linearResponseContainerDialogFragment.addView(textView)
            }
        })
    }

    private fun showWaveLoader(isSynthesizes: Boolean) {
        when (isSynthesizes) {
            true -> {
                waveLoaderDialogFragment.visibility = View.VISIBLE
                tvResponseDialogFragment.visibility = View.INVISIBLE
            }
            false -> {
                waveLoaderDialogFragment.visibility = View.INVISIBLE
                tvResponseDialogFragment.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_dialog, container, false)
    }

}