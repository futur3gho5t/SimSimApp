package az.azreco.simsimapp.other

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import az.azreco.simsimapp.R
import az.azreco.simsimapp.SimSimApplication
import java.io.IOException
import java.io.InputStream


class SimpleAudioTrack {

    private var audioTrack: AudioTrack? = null

    fun play() {
        initAudioTrack()
        var i = 0
        val bufferSize = 512
        val buffer = ByteArray(bufferSize)
        val inputStream: InputStream =
            SimSimApplication.getContext().resources.openRawResource(R.raw.greeting)
        try {
            while (inputStream.read().also { i = it } != -1) audioTrack?.write(buffer, 0, i)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
//        audioTrack?.let {
//            it.flush()
//            it.stop()
//            it.release()
//        }
//        audioTrack = null
    }


    private fun initAudioTrack() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            22050, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC, 22050, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM
        )
    }

}