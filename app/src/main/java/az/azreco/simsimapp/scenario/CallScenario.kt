package az.azreco.simsimapp.scenario

import android.util.Log
import az.azreco.simsimapp.R
import az.azreco.simsimapp.constant.TTS_CONSTANTS
import az.azreco.simsimapp.constant.TtsConstants.ASK_ABOUT_CALLING
import az.azreco.simsimapp.exoplayer.WildExoPlayer
import az.azreco.simsimapp.model.PhoneContact
import az.azreco.simsimapp.util.RecursiveCaller
import javax.inject.Inject

class CallScenario @Inject constructor(exoPlayer: WildExoPlayer) : ContactInfoScenario(exoPlayer),
    RecursiveCaller {
    private val TAG = "CALL_SCENARIO"


//    suspend fun start(): PhoneContact? {
//        val resultContact: PhoneContact?
//        val contact = getContactData()
//        textToSpeech.speak(ASK_ABOUT_CALLING)
//        resultContact = if (contact.phoneNumber.isNotEmpty()) {
//            when (askAboutCall()) {
//                true -> contact
//                false -> null
//            }
//        } else {
//            null
//        }
//        textToSpeech.destroy()
//        return resultContact
//    }


    suspend fun start(name: String = ""): PhoneContact? {
        val resultContact: PhoneContact?
        val contact: PhoneContact = if (name.isEmpty()) {
            getContactData()
        } else {
            getContactData(name = name)
        }
        textToSpeech.speak(ASK_ABOUT_CALLING)
        exoPlayer.play(R.raw.signal)
        resultContact = if (contact.phoneNumber.isNotEmpty()) {
            when (askAboutCall()) {
                true -> contact
                false -> null
            }
        } else {
            null
        }
        destroy()
        return resultContact
    }

    private suspend fun askAboutCall(): Boolean {
        var response = false
        callRecursively(
            keyWords = "zəng et\nləğv et",
            filterFunc = {
                when (it) {
                    "zəng et" -> response = true
                    "ləğv et" -> response = false
                }
            }
        )
        return response
    }

    override suspend fun callRecursively(keyWords: String, filterFunc: suspend (String) -> Unit) {
        Log.d(TAG, "Recursion!!")
        do {
            val command = keywordSpotting.listen(keyWords)
            filterFunc(command)
            if (command == "error") {
                textToSpeech.speak(TTS_CONSTANTS.KWS_DONT_UNDERSTAND)
                exoPlayer.play(R.raw.signal)
            }
        } while (command == "error")
    }


}