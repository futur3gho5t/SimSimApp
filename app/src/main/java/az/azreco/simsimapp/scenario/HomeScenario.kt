package az.azreco.simsimapp.scenario

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import az.azreco.simsimapp.R
import az.azreco.simsimapp.azreco.asr.KeywordSpotting
import az.azreco.simsimapp.azreco.tts.TextToSpeech
import az.azreco.simsimapp.constant.ServiceActions.SERVICE_ACTION_PHONE_CALL
import az.azreco.simsimapp.constant.ServiceActions.SERVICE_ACTION_SEND_SMS
import az.azreco.simsimapp.constant.ServiceActions.SERVICE_ACTION_SEND_SMS_1
import az.azreco.simsimapp.constant.ServiceActions.SERVICE_OPEN_APPLICATION
import az.azreco.simsimapp.constant.TTS_CONSTANTS
import az.azreco.simsimapp.model.PhoneContact
import az.azreco.simsimapp.model.SmsModel
import az.azreco.simsimapp.exoplayer.MyExoPlayer
import az.azreco.simsimapp.exoplayer.WildExoPlayer
import az.azreco.simsimapp.util.ContactApi
import az.azreco.simsimapp.util.ContactUtil
import az.azreco.simsimapp.util.RecursiveCaller
import az.azreco.simsimapp.util.SpeechLiveData
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject


class HomeScenario @Inject constructor(@ActivityContext private val context: Context) :
    RecursiveCaller {


    private val TAG = "MAIN_SCENARIO"
    private var contactList: List<PhoneContact> = ContactApi.getContactList()

    @Inject
    lateinit var exoPlayer: WildExoPlayer

    @Inject
    lateinit var sendSmsScenario: SendSmsScenario

    @Inject
    lateinit var callScenario: CallScenario

    @Inject
    lateinit var whatsappScenario: WhatsappScenario

//    private val sendSmsScenario = SendSmsScenario()
//    private val callScenario = CallScenario()
//    private val whatsappScenario = WhatsappScenario()

    suspend fun greetingUser() {
        SpeechLiveData.isWorking = true
        exoPlayer.play(R.raw.greeting)
        val kwPack =
            "${SERVICE_ACTION_SEND_SMS}\n$SERVICE_ACTION_SEND_SMS_1\n$SERVICE_ACTION_PHONE_CALL"
        SpeechLiveData.setKwsResponseGroup(responses = kwPack)
        callRecursively(
            keyWords = "${SERVICE_ACTION_SEND_SMS}\n$SERVICE_ACTION_SEND_SMS_1\n" +
                    "$SERVICE_OPEN_APPLICATION\n$SERVICE_ACTION_PHONE_CALL\n" +
                    "${getSmsKeywords()}\n${getCallKeywords()}\n${getWhatsappKeywords()}",
            filterFunc = {
                filterCommand(command = it)
            }
        )
        SpeechLiveData.isWorking = false
        SpeechLiveData.setKwsResponseGroup(responses = kwPack)
    }

    private suspend fun filterCommand(command: String) {
        val kwsSmsSplitted = getSmsKeywords().split("\n")
        val kwsCallSplitted = getCallKeywords().split("\n")
        val kwsWhatsappSplitted = getWhatsappKeywords().split("\n")
        when {
            command == SERVICE_ACTION_SEND_SMS || SERVICE_ACTION_SEND_SMS_1 == command ->
                sendSmsScenario.start()
            command == SERVICE_ACTION_PHONE_CALL -> {
                val receiver = callScenario.start()
                receiver?.let { makeCall(receiver = it) }
            }
            // SMS keywords
            kwsSmsSplitted.contains(command) -> {
                val name =
                    ContactUtil.removeSuffix(command, "a esemes göndər", "ya esemes göndər")
                sendSmsScenario.start(name = name)

            }
            // Call keywords
            kwsCallSplitted.contains(command) -> {
                val name = ContactUtil.removeSuffix(command, "a zəng elə", "ya zəng elə")
                val receiver = callScenario.start(name = name)
                receiver?.let { makeCall(receiver = it) }
            }
            // Whatsapp keywords
            kwsWhatsappSplitted.contains(command) -> {
                val name = ContactUtil.removeSuffix(command, "a vatsap göndər", "ya vatsap göndər")
                val smsModel = whatsappScenario.start(name = name)
                smsModel?.let { sendWhatsappMessage(smsModel = it) }
            }
        }
    }


    private fun makeCall(receiver: PhoneContact) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:${receiver.phoneNumber}")
        }
        context.startActivity(intent)

    }

    private fun sendWhatsappMessage(smsModel: SmsModel) {
        val phoneNo = smsModel.phoneContact.phoneNumber.replace("+", "")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_TEXT, smsModel.msg)
            putExtra("jid", "$phoneNo@s.whatsapp.net")
            setPackage("com.whatsapp")
        }
        context.startActivity(intent)
    }

    private fun getSmsKeywords(): String {
        val contactNames = contactList.map { it.name }
        return ContactUtil.removeNonAzWithSuffix(
            contactNames,
            "a esemes göndər\n",
            "ya esemes göndər\n"
        )
    }

    private fun getWhatsappKeywords(): String {
        val contactNames = contactList.map { it.name }
        return ContactUtil.removeNonAzWithSuffix(
            contactNames,
            "a vatsap göndər\n",
            "ya vatsap göndər\n"
        )
    }

    private fun getCallKeywords(): String {
        val contactNames = contactList.map { it.name }
        return ContactUtil.removeNonAzWithSuffix(
            contactNames,
            "a zəng elə\n",
            "ya zəng elə\n"
        )
    }

    override suspend fun callRecursively(keyWords: String, filterFunc: suspend (String) -> Unit) {
        Log.d(TAG, "Recursion!!")
        do {
            val command = KeywordSpotting().listen(keyWords)
            filterFunc(command)
            if (command == "error") {
                TextToSpeech().apply {
                    speak(TTS_CONSTANTS.KWS_DONT_UNDERSTAND)
                    destroy()
                }
                MyExoPlayer().play(R.raw.signal)
            }
        } while (command == "error")
    }

}