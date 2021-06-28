package az.azreco.simsimapp.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.lifecycle.MutableLiveData
import az.azreco.simsimapp.R
import az.azreco.simsimapp.azreco.asr.KeywordSpotting
import az.azreco.simsimapp.azreco.tts.TextToSpeech
import az.azreco.simsimapp.constant.TTS_CONSTANTS.KWS_DONT_UNDERSTAND
import az.azreco.simsimapp.exoplayer.MyExoPlayer
import az.azreco.simsimapp.scenario.NotificationScenario
import az.azreco.simsimapp.util.RecursiveCaller
import kotlinx.coroutines.*


class SimSimService : AccessibilityService(), RecursiveCaller {

    private val TAG = "SIMSIM_SERVICE"
    private var serviceScope = CoroutineScope(Dispatchers.IO)
    private var serviceReceiver: ServiceReceiver? = null

    companion object {
        val isWhatsappSubscribed = MutableLiveData<Boolean>()
        val isSmsSubscribed = MutableLiveData<Boolean>()
        val readWhatsappSender = MutableLiveData<Boolean>()
        val readSmsSender = MutableLiveData<Boolean>()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter().apply {
            addAction("subscribe WhatsApp")
            addAction("unsubscribe WhatsApp")
            addAction("unsubscribe SMS")
            addAction("unsubscribe SMS")
        }
        isWhatsappSubscribed.postValue(true)
        serviceReceiver = ServiceReceiver()
        registerReceiver(serviceReceiver, filter)
        Log.d(TAG, "onServiceConnected")
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(TAG, "onAccessibilityEvent")
        when (event.eventType) {
            TYPE_NOTIFICATION_STATE_CHANGED -> serviceScope.launch {
                handleNotification(event = event)
            }
            TYPE_WINDOW_CONTENT_CHANGED -> serviceScope.launch {
                handleWhatsappWindowContent()
            }

        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        return super.onKeyEvent(event)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private suspend fun handleNotification(event: AccessibilityEvent) {
        NotificationScenario.handle(event = event)
    }

    private suspend fun handleWhatsappWindowContent() {
        try {
            if (rootInActiveWindow == null) return
            else {
                val packageName = rootInActiveWindow.packageName
                if (packageName == "com.whatsapp") {
                    val rootInActiveWindow = AccessibilityNodeInfoCompat.wrap(rootInActiveWindow)
                    //Whatsapp send button id
                    val sendMessageNodeInfoList =
                        rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
                    if (sendMessageNodeInfoList == null || sendMessageNodeInfoList.isEmpty()) {
                        return
                    }
                    val sendMessageButton = sendMessageNodeInfoList[0]
                    if (!sendMessageButton.isVisibleToUser) {
                        return
                    }

                    // Now fire a click on the send button

                    sendMessageButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                    // Now go back to your app by clicking on the Android back button twice:
                    // First one to leave the conversation screen
                    // Second one to leave whatsapp
                    delay(1000) // hack for certain devices in which the immediate back click is too fast to handle

                    performGlobalAction(GLOBAL_ACTION_BACK)
                    delay(1000) // same hack as above

                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onInterrupt() {
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        unregisterReceiver(serviceReceiver)
    }

    internal inner class ServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "subscribe WhatsApp" -> isWhatsappSubscribed.postValue(true)
                "unsubscribe WhatsApp" -> isWhatsappSubscribed.postValue(false)

                "subscribe SMS" -> isSmsSubscribed.postValue(true)
                "unsubscribe SMS" -> isSmsSubscribed.postValue(false)

                "read sender WhatsApp" -> readWhatsappSender.postValue(true)
                "unread sender WhatsApp" -> readWhatsappSender.postValue(false)

                "read sender SMS" -> readSmsSender.postValue(true)
                "unread sender SMS" -> readSmsSender.postValue(false)
            }
        }

    }

    override suspend fun callRecursively(
        keyWords: String,
        filterFunc: suspend (String) -> Unit
    ) {
        Log.d(TAG, "Recursion!!")
        do {
            val command = KeywordSpotting().listen(keyWords)
            filterFunc(command)
            if (command == "error") {
                TextToSpeech().apply {
                    speak(KWS_DONT_UNDERSTAND)
                    destroy()
                }
                MyExoPlayer().play(R.raw.signal)
            }
        } while (command == "error")
    }
}