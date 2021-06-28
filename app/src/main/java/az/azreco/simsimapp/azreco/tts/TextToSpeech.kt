package az.azreco.simsimapp.azreco.tts

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import az.azreco.simsimapp.constant.AzrecoConstants.AZ_LANG
import az.azreco.simsimapp.constant.AzrecoConstants.TTS_HOST
import az.azreco.simsimapp.constant.AzrecoConstants.TTS_ID
import az.azreco.simsimapp.constant.AzrecoConstants.TTS_PORT
import com.azreco.tts.client.TTSClient
import com.azreco.tts.client.TTSClientConfiguration

class TextToSpeech {

    private lateinit var ttsClient: TTSClient
    private var audioTrack: AudioTrack? = null
    private var isFirstRun = true
    private var voiceId = ""

    suspend fun speak(text: String, voiceId: String = "") {
        if (voiceId.isNotEmpty()) {
            this.voiceId = voiceId
        }
        if (isFirstRun) {
            Log.d("TextToSpeech", "FIRST RUN")
            initTTSClient()
            isFirstRun = false
        }
        ttsClient.synthesize(text)
        val bufferSize = initAudioTrack()
        audioTrack?.play()
        buferrizing(bufferSize)
        reset()
    }

    private fun initTTSClient() {
        val ttsConf = TTSClientConfiguration().apply {
            host = TTS_HOST
            port = TTS_PORT
            language = AZ_LANG
            ttsId = if (voiceId.isEmpty()) {
                TTS_ID
            } else {
                voiceId
            }
            isSSLEnabled = true
        }
        ttsClient = TTSClient(ttsConf)
    }


    private fun reset() {
        ttsClient.apply {
            waitForCompletion()
            reset()
        }
        audioTrack?.let {
            it.flush()
            it.stop()
            it.release()
        }
        audioTrack = null
    }

    fun destroy() {
        ttsClient.apply {
            waitForCompletion()
            destroy()
        }
        isFirstRun = true
        Log.d("TextToSpeech","TextToSpeech is Destroyed")
    }

    private fun initAudioTrack(): Int {
        var bufferSize = AudioTrack.getMinBufferSize(
            22050,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        bufferSize =
            if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
                22050 * 1 * 2 * 5
            } else {
                bufferSize * 5
            }
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            22050,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        return bufferSize
    }

    private fun buferrizing(bufferSize: Int) {
        val buffer = ByteArray(bufferSize)
        while (ttsClient.isOk) {
            val result = ttsClient.read()
            if (result == null || result.isEmpty()) {
                if (result == null) {
                    //network error
                }
                break
            }
            if (result.size > bufferSize) {
                val numBlocks: Int = result.size / bufferSize
                val remainBytes: Int = result.size % bufferSize
                for (j in 0 until numBlocks) {
                    System.arraycopy(result, j * bufferSize, buffer, 0, bufferSize)
                    audioTrack?.write(buffer, 0, bufferSize)
                }
                if (remainBytes > 0) {
                    System.arraycopy(result, numBlocks * bufferSize, buffer, 0, remainBytes)
                    audioTrack?.write(buffer, 0, remainBytes)
                }
            } else {
                System.arraycopy(result, 0, buffer, 0, result.size)
                audioTrack?.write(buffer, 0, result.size)
            }
        }
    }

}