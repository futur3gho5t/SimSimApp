package az.azreco.simsimapp.azreco.asr

import android.util.Log
import az.azreco.simsimapp.constant.AzrecoConstants.ASR_HOST
import az.azreco.simsimapp.constant.AzrecoConstants.ASR_PORT
import az.azreco.simsimapp.constant.AzrecoConstants.AZ_LANG
import com.AndroidMic
import com.Callbacks
import com.EnergyVad
import com.azreco.asr.client.ASRClient
import com.azreco.asr.client.ASRClientConfiguration
import com.azreco.asr.client.AudioSource
import com.azreco.asr.client.ResultType
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SpeechRecognizer : Callbacks {

    private val FRAME_MS = 100
    private val FRAME_RATE = 16000
    private val HEAD_MARGIN = 600

    // Stops when silence continues 3 sec
    private var TAILMARGIN = 3000

    private val TAG = "SpeechRecognizer"

    private var frameSizeInBytes = 0
    private var frameSizeInSamples = 0

    private var androidMic: AndroidMic? = null
    private var energyVad: EnergyVad? = null
    private var closeMic = false

    private val asrClient: ASRClient by lazy {
        ASRClient(getAsrConfig())
    }

    // timeOut in secs
    suspend fun listen(timeOut: Int = 3): String {
        var result = ""
        TAILMARGIN = timeOut * 1000
        val status = asrClient.connect()
        coroutineScope {
            launch { initEnergyVad() }
            val asrAnswer = async { waitingAsrResult(status = status) }
            result = asrAnswer.await()
        }
        Log.d(TAG, "The Result is - $result")
        destroy()
        return result
    }

    private fun destroy() {
        frameSizeInBytes = 0
        frameSizeInSamples = 0
        androidMic = null
        energyVad = null
        closeMic = false
    }

    private fun waitingAsrResult(status: Int): String {
        val resultSb = StringBuilder()
        if (status == 0) {
            Log.d(TAG, "ASR working")
            while (asrClient.hasResult(true)) {
                val jsonResult = asrClient.result
                try {
                    val jsonObj = JSONObject(jsonResult)
                    val recogResult = jsonObj.getString("resultText")
                    resultSb.append(recogResult).append(" ")
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                }
            }
        }
        asrClient.apply {
            waitForCompletion()
            destroy()
        }
        Log.d(TAG, "ASR stopped")
        return if (resultSb.isNotEmpty()) resultSb.toString() else ""
    }


    @Throws(Exception::class)
    private fun initEnergyVad() {
        androidMic = AndroidMic()
        androidMic?.let {
            it.open()
            frameSizeInSamples = FRAME_RATE * FRAME_MS / 1000
            frameSizeInBytes = 2 * frameSizeInSamples
            energyVad = EnergyVad(HEAD_MARGIN, TAILMARGIN, frameSizeInBytes)
            energyVad?.setCallbacks(this)
            it.start()
            energyVad?.process()
            Log.d(TAG, "initEnergyVad end - " + Calendar.getInstance().time)
        }
    }

    override fun callbackRead(buf: ShortArray?, offset: Int, len: Int): Int {
        if (closeMic) return -1
        val currentLen = len * 2
        val bufferBytes = ByteArray(currentLen)
        var cnt = 0
        if (androidMic != null) {
            cnt = androidMic!!.read(bufferBytes, 0, currentLen)
        } else {
            return -1
        }
        if (cnt > 0) {
            val bBuffer = ByteBuffer.allocate(currentLen).apply {
                put(bufferBytes)
                order(ByteOrder.LITTLE_ENDIAN)
                rewind()
            }
            if (bBuffer.hasArray()) {
                bBuffer.asShortBuffer()[buf, offset, cnt / 2]
            }
        } else if (cnt < 0) {
            return -1
        }
        return cnt / 2
    }

    override fun callbackProcess(buf: ShortArray?, offset: Int, len: Int): Int {
        if (closeMic) return -1
        val data = ByteArray(len * 2)
        val bf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        bf.asShortBuffer().put(buf, offset, len)
        val leBuffer = bf.array()
        return if (asrClient.isOk) {
            asrClient.write(leBuffer, 0, leBuffer.size)
            0
        } else {
            -1
        }
    }

    override fun callbackGetCheck(): Int {
        return when (androidMic?.isOpen) {
            true -> 1
            else -> 0
        }
    }

    override fun callbackStart() {
        if (!closeMic) Log.d(
            TAG,
            "callbackStart - androidMic ON ::: " + Calendar.getInstance().time
        )
        else Log.d(TAG, "callbackStart - androidMic OFF" + Calendar.getInstance().time)
    }

    override fun callbackStop() {
        if (closeMic) return
        else {
            Log.d(TAG, "callbackStop - " + Calendar.getInstance().time)
            asrClient.endStream()
            closeMic = true
            androidMic?.close()
        }
    }

    override fun callbackVisualize(value: Float) {
    }


    private fun getAsrConfig(): ASRClientConfiguration {
        return ASRClientConfiguration().apply {
            host = ASR_HOST
            port = ASR_PORT
            audioSource = AudioSource.AS_REALTIME
            resultType = ResultType.RT_PARTIAL
            language = AZ_LANG
            isSSLEnabled = true
            isOpusEnabled = true
        }
    }
}