package az.azreco.simsimapp.scenario

import android.util.Log
import az.azreco.simsimapp.R
import az.azreco.simsimapp.azreco.asr.KeywordSpotting
import az.azreco.simsimapp.azreco.tts.TextToSpeech
import az.azreco.simsimapp.constant.KWS_CONSTANTS
import az.azreco.simsimapp.constant.TTS_CONSTANTS
import az.azreco.simsimapp.model.PhoneContact
import az.azreco.simsimapp.exoplayer.MyExoPlayer
import az.azreco.simsimapp.exoplayer.TestPlayer
import az.azreco.simsimapp.exoplayer.WildExoPlayer
import az.azreco.simsimapp.util.ContactApi
import az.azreco.simsimapp.util.ContactUtil
import az.azreco.simsimapp.util.SpeechLiveData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

open class ContactInfoScenario @Inject constructor(val exoPlayer: WildExoPlayer) {


    private val TAG = "ContactInfoScenario"

//    val exoPlayer = MyExoPlayer()

//    @Inject
//    lateinit var exoPlayer: WildExoPlayer

    private val mContactList: List<PhoneContact> = ContactApi.getContactList()

    val textToSpeech = TextToSpeech()
    var keywordSpotting = KeywordSpotting()
    private val contactApi = ContactApi
    var selectedName = ""
//    lateinit var receiver: PhoneContact


    suspend fun getContactData(): PhoneContact {
        Log.d(TAG, "::start::")
        var totalContactsNames = mContactList.joinToString(separator = "\n") { it.name }
        totalContactsNames = ContactUtil.removeNonAzLetters(totalContactsNames.split("\n"))
        textToSpeech.speak(TTS_CONSTANTS.ASK_WHO_IS_RECEIVER)
        exoPlayer.play(R.raw.signal)
        recursively(
            keyWords = "$totalContactsNames${KWS_CONSTANTS.STOP_KEYWORD}",
            filterFunc = {
                when {
                    totalContactsNames.contains(it) -> {
                        SpeechLiveData.kwsResponse.postValue(it)
                        selectedName = it
                    }
                    it == KWS_CONSTANTS.STOP_KEYWORD -> {
                        exoPlayer.play(R.raw.sms_canceled)
                        selectedName = KWS_CONSTANTS.STOP_KEYWORD
                    }
                }
            }
        )
        if (selectedName != KWS_CONSTANTS.STOP_KEYWORD) {
            val foundedContacts = contactApi.containsContactName(selectedName, mContactList)
            Log.d(TAG, foundedContacts.toString())
            return getReceiverData(foundedContacts = foundedContacts)
//            textToSpeech.destroy()
        } else {
            return PhoneContact("", "")
        }
    }

    suspend fun getContactData(name: String): PhoneContact {
        selectedName = name
        val foundedContacts = contactApi.containsContactName(selectedName, mContactList)
        return getReceiverData(foundedContacts = foundedContacts)
    }


    private suspend fun getReceiverData(foundedContacts: List<PhoneContact>): PhoneContact {
        Log.d(TAG, "::getReceiverData::")
        return when {
            foundedContacts.isEmpty() -> {
                ifContactNotFound()
            }
            foundedContacts.size == 1 -> {
                ifContactIsOne(phoneContact = foundedContacts[0])
            }
            else -> {
                ifContactTooMany(foundedContacts = foundedContacts)
            }
        }
    }

    private suspend fun ifContactNotFound(): PhoneContact {
        Log.d(TAG, "::ifContactNotFound::")
        exoPlayer.play(R.raw.sms_contact_not_found)
        return PhoneContact("", "")
    }

    private suspend fun ifContactIsOne(phoneContact: PhoneContact): PhoneContact {
        val receiverPhoneNo =
            contactApi.getFilteredPhoneNumber(phoneContact.phoneNumber)
        textToSpeech.speak(
            text = "Kontaktın adı ${phoneContact.name}. nömrəsi $receiverPhoneNo"
        )
        return phoneContact
    }


    private suspend fun ifContactTooMany(foundedContacts: List<PhoneContact>): PhoneContact {
        Log.d(TAG, "::ifContactTooMany::")
        val strContacts = fromListToString(foundedContacts)
        val contactChoice =
            "kontaktın ardıcılıq rəqəmini söyləyib məsəl ücün birinci. və yaxud kontaktın son 4 rəqəmini söyləyib seçə bilərsiz."
        textToSpeech.speak(
            "$selectedName adında ${foundedContacts.size} dənə kontakt tapıldı. $strContacts. $contactChoice"
        )
        exoPlayer.play(R.raw.signal)
        var indexOfContact = 0
        recursively(
            keyWords = "${ContactUtil.azNumerical()}${KWS_CONSTANTS.STOP_KEYWORD}",
            filterFunc = {
                when {
                    ContactUtil.azNumerical().contains(it) -> {
                        indexOfContact = ContactUtil.getContactByNumerical(it, foundedContacts)
                        SpeechLiveData.kwsResponse.postValue(it)
                    }
                    it == KWS_CONSTANTS.STOP_KEYWORD -> {
                        exoPlayer.play(R.raw.sms_canceled)
                        indexOfContact = -1
                    }
                }
            })
        if (indexOfContact != -1) {
            val chosenContact = foundedContacts[indexOfContact]
            val phoneNo =
                contactApi.getFilteredPhoneNumber(chosenContact.phoneNumber)
            textToSpeech.speak(
                text = "Kontaktın adı ${chosenContact.name}. nömrəsi $phoneNo"
            )
            return chosenContact
        } else {
            return PhoneContact("", "")
        }
    }

    private suspend fun recursively(keyWords: String, filterFunc: suspend (String) -> Unit) {
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

    private fun lolka(myFunc: (String) -> Unit) = myFunc

    // from List<PhoneContact> to contact.name\ncontact.name\ncontact.name\n...
    private fun fromListToString(contactList: List<PhoneContact>): String {
        return contactList.joinToString(separator = ", ") {
            "${it.name} , nömrəsi ${contactApi.getFilteredPhoneNumber(it.phoneNumber)}"
        }
    }

    fun destroy() {
        textToSpeech.destroy()
    }
}