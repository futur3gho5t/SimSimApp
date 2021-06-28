package az.azreco.simsimapp.exoplayer

import android.util.Log
import az.azreco.simsimapp.SimSimApplication
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyExoPlayer {



    private lateinit var exoPlayer: SimpleExoPlayer

    suspend fun play(audioName: Int) {
        exoPlayer = SimpleExoPlayer.Builder(SimSimApplication.getContext()).build()
        val mediaSource = buildRawMediaSource(audioName = audioName)
        addListener()
        withContext(Dispatchers.Main) {
//            val lol = async(Dispatchers.Main) {
        exoPlayer.apply {
            playWhenReady = true
            setMediaSource(mediaSource)
            prepare()
        }
            }
//            lol.await()
//        }
    }

    private fun addListener() {
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    exoPlayer.apply {
                        stop()
                        release()
                        Log.d("EXOPLAYER", "Released")
                        Log.d("EXOPLAYER", "$exoPlayer")
                    }
                } else if (state == Player.STATE_READY) {
                    Log.d("EXOPLAYER", "Ready")
                }
            }
        })
    }


//    fun pause(){
//        exoPlayer.pause()
//    }
//
//
//    fun release(){
//        exoPlayer.apply {
//            stop()
//            release()
//        }
//    }

    private fun buildRawMediaSource(audioName: Int): MediaSource {
        val rawDataSource = RawResourceDataSource(SimSimApplication.getContext())
        // open the /raw resource file
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(audioName)))

        // Create media Item
        val mediaItem = MediaItem.fromUri(rawDataSource.uri!!)

        // create a media source with the raw DataSource
        val mediaSource = ProgressiveMediaSource.Factory { rawDataSource }
            .createMediaSource(mediaItem)

        return mediaSource
    }
}
