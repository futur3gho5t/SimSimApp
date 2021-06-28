package az.azreco.simsimapp.util

import androidx.lifecycle.MutableLiveData

object SpeechLiveData {

    val kwsResponse = MutableLiveData<String>()

    val ttsQuestion = MutableLiveData<String>()

    val responseGroup = MutableLiveData<List<String>>()

    val isSynthesizes = MutableLiveData<Boolean>()

    var isWorking = false


    fun setKwsResponseGroup(responses: String) {
        val resultList = responses.split("\n")
            .filter { it.isNotEmpty() }
        responseGroup.postValue(resultList)
    }

    fun setSpeechResponse(str: String) {
        kwsResponse.postValue(str)
    }

    fun setSpeechQuestion(str: String) {
        ttsQuestion.postValue(str)
    }
}