package az.azreco.simsimapp.scenario

import android.util.Log
import az.azreco.simsimapp.R
import az.azreco.simsimapp.azreco.asr.SpeechRecognizer
import az.azreco.simsimapp.constant.KWS_CONSTANTS.STOP_KEYWORD
import az.azreco.simsimapp.constant.TTS_CONSTANTS
import az.azreco.simsimapp.constant.TtsConstants
import az.azreco.simsimapp.exoplayer.WildExoPlayer
import az.azreco.simsimapp.model.PhoneContact
import az.azreco.simsimapp.model.SmsModel
import az.azreco.simsimapp.util.RecursiveCaller
import az.azreco.simsimapp.util.SpeechLiveData
import javax.inject.Inject

class WhatsappScenario @Inject constructor(exoPlayer: WildExoPlayer) : ContactInfoScenario(exoPlayer),
    RecursiveCaller {

    private val TAG = "WHATSAPP_SCENARIO"

    private var smsModel: SmsModel? = null

    suspend fun start(name: String = ""): SmsModel? {
        val contact = if (name.isEmpty()) {
            getContactData()
        } else {
            getContactData(name = name)
        }
        if (contact.phoneNumber.isNotEmpty()) {
            getMessageData(receiver = contact)
        }
        return smsModel
    }

    private suspend fun getMessageData(receiver: PhoneContact) {
        Log.d(TAG, "::getMessageData::")
        var counter = 0
        var resultText: String
        textToSpeech.speak(TtsConstants.SMS_SAY_TEXT)
        exoPlayer.play(R.raw.signal)
        resultText = SpeechRecognizer().listen(timeOut = 1)
        if (resultText.isEmpty()) {
            resultText = SpeechRecognizer().listen(timeOut = 1)
        }
        SpeechLiveData.kwsResponse.postValue(resultText)
        if (resultText.isNotEmpty()) {
            textToSpeech.speak(text = "esemesin mətni. $resultText. $${TtsConstants.SMS_ASK_ABOUT_SENDING}")
            exoPlayer.play(R.raw.signal)
            callRecursively(keyWords = "göndər\ndəyiş\nləğv et\nlazım deyil\n$STOP_KEYWORD",
                filterFunc = {
                    when (it) {
                        "göndər" -> smsModel = SmsModel(phoneContact = receiver, msg = resultText)
                        "ləğv et", "lazım deyil", STOP_KEYWORD -> smsModel = null
                        "dəyiş" -> getMessageData(receiver = receiver)
//                        "kontaktı dəyiş" -> start()
                    }
                }
            )
        }
    }

    private suspend fun askAboutWhatsappMessage(): Boolean {
        return false
    }


    override suspend fun callRecursively(keyWords: String, filterFunc: suspend (String) -> Unit) {
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