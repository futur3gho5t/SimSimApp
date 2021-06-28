package az.azreco.simsimapp.scenario

import android.app.Notification
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import az.azreco.simsimapp.azreco.tts.TextToSpeech
import az.azreco.simsimapp.constant.PackageNames.SMS_PACK_NAME_SAMSUNG
import az.azreco.simsimapp.constant.PackageNames.SMS_PACK_NAME_XIOMI
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object NotificationScenario {

    private val TAG = "NOTIFICATION_SCENARIO"


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    suspend fun handle(event: AccessibilityEvent) {
        when (event.packageName) {
            SMS_PACK_NAME_XIOMI, SMS_PACK_NAME_SAMSUNG -> {
                filterSmsNotification(event = event)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    suspend fun filterSmsNotification(event: AccessibilityEvent) {
        val data = event.parcelableData
        if (data is Notification) {
            Log.d(TAG, "Recieved notification")
            val extras = data.extras as Bundle
            if (extras.getCharSequence("android.text") is String) {
                val text = extras.getCharSequence("android.text") as String
                val title = extras.getCharSequence("android.title") as String
                Log.d(
                    TAG,
                    "extra_text: $text, extra_title: $title, tickerText: ${data.tickerText}, eventText: ${event.text}"
                )
                coroutineScope {
                    TextToSpeech().apply {
                        speak("$title esemes göndərib. esemesin mətni $text", "325651")
                        destroy()
                    }
                }
            } else if (extras.getCharSequence("android.text") is SpannableString) {
                Log.d(TAG, "SpannableString")
            }
        }
    }

}