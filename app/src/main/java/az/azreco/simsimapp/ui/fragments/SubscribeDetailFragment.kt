package az.azreco.simsimapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import az.azreco.simsimapp.R
import az.azreco.simsimapp.service.SimSimService
import kotlinx.android.synthetic.main.fragment_subscribe_detail.*

class SubscribeDetailFragment : Fragment(R.layout.fragment_subscribe_detail) {

    private val args: SubscribeDetailFragmentArgs by navArgs()

    private var subscriberName = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscriberName = args.subscribeApp
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(requireContext(), subscriberName, Toast.LENGTH_SHORT).show()

        subscribeObservers(appName = subscriberName)

        detailTvReadAllNotif.text = "Read all $subscriberName message notifications"
        detailTvReadWithSender.text = "Read $subscriberName message with Sender name"

        detialSwitcherReadAllNotif.setOnCheckedChangeListener { checked ->
            if (checked) {
                requireContext().sendBroadcast(Intent("subscribe $subscriberName"))
            } else {
                requireContext().sendBroadcast(Intent("unsubscribe $subscriberName"))
            }
        }

        detailSwitcherReadWithSender.setOnCheckedChangeListener { checked ->
            if (checked) {
//                requireContext().sendBroadcast(Intent("read sender $subscriberName"))
                SimSimService.readSmsSender.postValue(true)
            } else {
//                requireContext().sendBroadcast(Intent("unread sender $subscriberName"))
                SimSimService.readSmsSender.postValue(false)
            }
        }

    }


    private fun subscribeObservers(appName: String) {
        if (appName == "WhatsApp") {
            SimSimService.isWhatsappSubscribed.observe(viewLifecycleOwner, {
                detialSwitcherReadAllNotif.setChecked(it)
            })
            SimSimService.readWhatsappSender.observe(viewLifecycleOwner,{
                detailSwitcherReadWithSender.setChecked(it)
            })


        } else if (appName == "SMS") {
            SimSimService.isSmsSubscribed.observe(viewLifecycleOwner, {
                detialSwitcherReadAllNotif.setChecked(it)
            })
            SimSimService.readSmsSender.observe(viewLifecycleOwner, {
                detailSwitcherReadWithSender.setChecked(it)
            })
        }
    }


}