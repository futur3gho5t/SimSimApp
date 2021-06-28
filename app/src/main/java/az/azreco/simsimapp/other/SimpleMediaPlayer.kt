package az.azreco.simsimapp.other

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import az.azreco.simsimapp.SimSimApplication

class SimpleMediaPlayer(private val onlyOneTime: Boolean) {


    private lateinit var mMediaPlayer: MediaPlayer

    fun play(audioName: Int) {
        val context = SimSimApplication.getContext()
        mMediaPlayer = MediaPlayer()
        val mediaPath =
            Uri.parse("android.resource://" + context?.packageName + "/" + audioName)
        try {
            context?.let { it ->
                mMediaPlayer.setDataSource(it, mediaPath)
                mMediaPlayer.prepare()
                mMediaPlayer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mMediaPlayer.setOnCompletionListener { player ->
            if (onlyOneTime) {
                player.also {
                    it.reset()
                    it.release()
                }
                player.release()
                Log.d("LOL", "released")
            } else {
                player.reset()
                Log.d("LOL", "reseted")
            }
        }
    }

    fun destroy() {
        mMediaPlayer.release()
    }


}