package az.azreco.simsimapp.scenario

import android.util.Log
import az.azreco.simsimapp.R
import az.azreco.simsimapp.azreco.asr.SpeechRecognizer
import az.azreco.simsimapp.constant.KWS_CONSTANTS
import az.azreco.simsimapp.constant.TTS_CONSTANTS
import az.azreco.simsimapp.constant.TtsConstants
import az.azreco.simsimapp.exoplayer.WildExoPlayer
import az.azreco.simsimapp.model.PhoneContact
import az.azreco.simsimapp.util.ContactApi
import az.azreco.simsimapp.util.RecursiveCaller
import az.azreco.simsimapp.util.SmsApi
import az.azreco.simsimapp.util.SpeechLiveData
import kotlinx.coroutines.delay
import javax.inject.Inject

class SendSmsScenario @Inject constructor(exoPlayer: WildExoPlayer) : ContactInfoScenario(exoPlayer),
    RecursiveCaller {

    private val TAG = "SEND_SMS_SCENARIO"

//    suspend fun start() {
//        val contact = getContactData()
//        if (contact.phoneNumber.isNotEmpty()) {
//            getMessageData(receiver = contact)
//        }
//        destroy()
//    }

    suspend fun start(name: String = "") {
        val contact = if (name.isEmpty()) {
            getContactData()
        } else {
            getContactData(name = name)
        }
//        val contact = getContactData(name = name)
        if (contact.phoneNumber.isNotEmpty()) {
            getMessageData(receiver = contact)
        }
        destroy()
    }

    private suspend fun getMessageData(receiver: PhoneContact) {
        Log.d(TAG, "::getMessageData::")
        var counter = 0
        var resultText: String
        exoPlayer.play(R.raw.sms_say_text)
//        textToSpeech.speak(TtsConstants.SMS_SAY_TEXT)
//        exoPlayer.play(R.raw.signal)
        delay(1000)
        resultText = SpeechRecognizer().listen(timeOut = 1)
        if (resultText.isEmpty()) {
            resultText = SpeechRecognizer().listen(timeOut = 1)
        }
        SpeechLiveData.kwsResponse.postValue(resultText)
        if (resultText.isNotEmpty()) {
            textToSpeech.speak(text = "esemesin mətni. $resultText. $${TtsConstants.SMS_ASK_ABOUT_SENDING}")
            exoPlayer.play(R.raw.signal)
            callRecursively(keyWords = "göndər\ndəyiş\nləğv et\nkontaktı dəyiş\nlazım deyil\n${KWS_CONSTANTS.STOP_KEYWORD}",
                filterFunc = {
                    when (it) {
                        "göndər" -> sendMessage(receiver = receiver, text = resultText)
                        "ləğv et", "lazım deyil", KWS_CONSTANTS.STOP_KEYWORD -> exoPlayer.play(R.raw.sms_canceled)
                        "dəyiş" -> getMessageData(receiver = receiver)
                        "kontaktı dəyiş" -> start()
                    }
                }
            )
        }
    }

    private suspend fun sendMessage(text: String, receiver: PhoneContact) {
        Log.d(TAG, "::sendMessage::")
        SmsApi.sendSMSByName(
            name = receiver.name,
            msg = text,
            contactList = ContactApi.getContactList()
        )
        exoPlayer.play(R.raw.send_sms_successful)
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