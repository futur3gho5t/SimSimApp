package az.azreco.simsimapp.other

import android.content.Context
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ExxoPlayer(private val context: Context) {

    private lateinit var exoPlayer: SimpleExoPlayer


    fun initPlayer() {
        exoPlayer = SimpleExoPlayer.Builder(context).build()
    }

    suspend fun play(audioName: Int) {
        val mediaSource = buildRawMediaSource(audioName = audioName)
        coroutineScope {
            launch(Dispatchers.Main) {
                exoPlayer.apply {
                    playWhenReady = true
                    setMediaSource(mediaSource)
                    prepare()
                }
            }
        }
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    exoPlayer.apply {
                        stop()
                        release()
                        initPlayer()
                        Log.d("EXOPLAYER", "Released")
                        Log.d("EXOPLAYER", "$exoPlayer")
                    }
                } else if (state == Player.STATE_READY) {
                    Log.d("EXOPLAYER", "Ready")
                }
            }
        })
    }


    fun pause() {
        exoPlayer.pause()
    }

    fun play() {
        exoPlayer.play()
    }

    fun release() {
        exoPlayer.release()
    }


    private fun buildRawMediaSource(audioName: Int): MediaSource {
        val rawDataSource = RawResourceDataSource(SimSimApplication.getContext())
        // open the /raw resource file
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(audioName)))
        // Create media Item
        val mediaItem = MediaItem.fromUri(rawDataSource.uri!!)
        // create a media source with the raw DataSource
        return ProgressiveMediaSource.Factory { rawDataSource }
            .createMediaSource(mediaItem)
    }
}