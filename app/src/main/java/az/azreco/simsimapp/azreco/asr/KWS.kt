package az.azreco.simsimapp.azreco.asr

import android.util.Log
import az.azreco.simsimapp.constant.AzrecoConstants
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

class KWS : Callbacks {

    private val FRAME_MS = 100
    private val FRAME_RATE = 16000
    private val HEAD_MARGIN = 600
    private val TAILMARGIN = 2000

    private val TAG = "KeywordSpotting"

    private var frameSizeInBytes = 0
    private var frameSizeInSamples = 0

    private var kwsClient: ASRClient? = null
    private var androidMic: AndroidMic? = null
    private var energyVad: EnergyVad? = null
    private var closeMic = false

    private var keyWords = ""
    private var kw = ""
    private var failedTimes = 0

    private var job = Job()
    private var kwsScope = CoroutineScope(Dispatchers.IO + job)


    suspend fun listen(keyWords: String): String {
        this.keyWords = keyWords
        kw = ""
        val recordJob = kwsScope.launch {
            initEnergyVad()
        }
        recordJob.join()
        destroy()
        return kw
    }


    private fun getKwsResult(status: Int) {
        if (status == 0) {
            Log.d(TAG, "KWS working")
            kwsClient?.let {
                while (it.hasResult(true)) {
                    Log.d(TAG, "KWS has result - ${Calendar.getInstance().time}")
                    val result = it.result
                    try {
                        val jsonObject = JSONObject(result)
                        val list = jsonObject.getJSONArray("words")
                        for (i in 0 until list.length()) {
                            val rec = list.getJSONObject(i)
                            val confindence = rec["confidence"] as Int
                            if (confindence > 60) {
                                kw = rec.getString("word")
                                closeMic = true
                            }
                            Log.e(TAG, confindence.toString())
                            val word = rec.getString("word")
                            Log.d(TAG, word)
                        }

                    } catch (ex: JSONException) {
                        ex.printStackTrace()
                    }
                }
                it.waitForCompletion()
                it.destroy()
                Log.d(TAG, "KWS stopped")
            }
        }
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
        Log.d(TAG, "callbackRead - " + Calendar.getInstance().time)
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
        Log.d(TAG, "callbackProcess - " + Calendar.getInstance().time)
        if (closeMic) return -1
        val data = ByteArray(len * 2)
        val bf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        bf.asShortBuffer().put(buf, offset, len)
        val leBuffer = bf.array()
        return if (kwsClient?.isOk == true) {
            kwsClient?.write(leBuffer, 0, leBuffer.size)
            0
        } else {
            -1
        }
    }

    override fun callbackGetCheck(): Int {
        return if (androidMic?.isOpen == true) {
            1
        } else {
            -1
        }
    }

    override fun callbackStart() {
        if (!closeMic) {
            Log.d(TAG, "callbackStart - " + Calendar.getInstance().time)
            kwsClient = ASRClient(getKwsConfig(keyWords))
            kwsClient?.let {
                val status = it.connect()
                if (status == 0) {
                    androidMic?.close()
                    return
                }
                kwsScope.launch {
                    getKwsResult(status = status)
                }
            }
        }
    }

    override fun callbackStop() {
        if (closeMic) return
        Log.d(TAG, "callbackStop - " + Calendar.getInstance().time)
        kwsClient?.endStream()
        runBlocking {
            job.join()
        }
        if (kw.isEmpty() && failedTimes >= 1) {
            Log.d(TAG, "FAILED TIME $failedTimes")
            closeMic = true
            kw = "error"
        } else {
            Log.d(TAG, "FAILED TIME $failedTimes")
            failedTimes += 1
        }
        if (closeMic) {
            androidMic?.close()
        }

    }

    override fun callbackVisualize(value: Float) {
        TODO("Not yet implemented")
    }

    private fun getKwsConfig(kWords: String): ASRClientConfiguration {
        return ASRClientConfiguration().apply {
            host = AzrecoConstants.ASR_HOST
            port = AzrecoConstants.ASR_PORT
            audioSource = AudioSource.AS_REALTIME
            resultType = ResultType.RT_KWS_PARTIAL
            language = AzrecoConstants.AZ_LANG
            customDictionary = kWords
            isSSLEnabled = true
            isOpusEnabled = true
        }
    }

    private fun destroy() {
        kwsScope.cancel()
        androidMic = null
        closeMic = false
        failedTimes = 0
    }

}