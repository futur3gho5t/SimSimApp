package az.azreco.simsimapp.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import az.azreco.simsimapp.R
import az.azreco.simsimapp.exoplayer.WildExoPlayer
import az.azreco.simsimapp.model.PhoneContact
import az.azreco.simsimapp.ui.viewmodels.MainViewModel
import az.azreco.simsimapp.util.SpeechLiveData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val TAG = "HOME_FRAGMENT"


    @Inject
    lateinit var exoPlayer: WildExoPlayer


    private val viewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnStop.setOnClickListener {
            exoPlayer.stop()
        }

        SpeechLiveData.kwsResponse.observe(viewLifecycleOwner, {
            tvSpeechOutputHome.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.fade_out
                )
            )
            tvSpeechOutputHome.text = it
        })
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(receiver, IntentFilter().apply {
            addAction("isSubscribed")
            addAction("PhoneCaller")
        })
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(receiver)
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "PhoneCaller" -> {
                    val result = intent.getSerializableExtra("RECEIVER_DATA") as PhoneContact
                    Log.e(TAG, result.toString())
                    val callIntent = Intent(Intent.ACTION_CALL)
//                    val phone = result.phoneNumber.replace("+", "")
                    val phone = result.phoneNumber
                    callIntent.data = Uri.parse("tel:$phone") //change the number
                    startActivity(callIntent)
                }
            }
        }

    }
}