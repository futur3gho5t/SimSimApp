package az.azreco.simsimapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import az.azreco.simsimapp.R
import az.azreco.simsimapp.adapter.SubscribersRecyclerAdapter
import az.azreco.simsimapp.model.SubscribeModel
import kotlinx.android.synthetic.main.fragment_subscribe.*

class SubscribeFragment : Fragment(R.layout.fragment_subscribe) {

    private val subscribeAdapter by lazy {
        SubscribersRecyclerAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        subscribeAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putString("subscribe_app", it.appName)
            }
            findNavController().navigate(
                R.id.action_subscribeFragment_to_subscribeDetailFragment,
                bundle
            )
        }
    }


    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        subscribeAdapter.setData(initSubscribers())

        recyclerSubscribeFragment.apply {
            layoutManager = linearLayoutManager
            adapter = subscribeAdapter
        }
    }


    private fun initSubscribers(): List<SubscribeModel> {
        return listOf(
            SubscribeModel("SMS", "message application", R.drawable.ic_sms),
            SubscribeModel("WhatsApp", "message application", R.drawable.ic_whatsapp),
            SubscribeModel("Telegram", "message application", R.drawable.ic_telegram),
            SubscribeModel("Messenger", "message application", R.drawable.ic_messenger),
            SubscribeModel("Facebook", "message application", R.drawable.ic_facebook),
            SubscribeModel("Gmail", "message application", R.drawable.ic_gmail),
        )
    }

}