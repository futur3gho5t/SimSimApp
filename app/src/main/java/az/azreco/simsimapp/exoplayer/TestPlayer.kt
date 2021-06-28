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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class TestPlayer(private val scope: CoroutineScope) {


    private var listener = WeakReference<ExoListener>(null)
    private lateinit var exoPlayer: SimpleExoPlayer


    //    var trigger by Delegates.observable(0) { property, oldValue, newValue ->
//        lol()
//    }
    suspend fun lol() {

    }

    suspend fun play(audioName: Int,lambda: suspend () -> Unit) {
        exoPlayer = SimpleExoPlayer.Builder(SimSimApplication.getContext()).build()
        val mediaSource = buildRawMediaSource(audioName = audioName)
        withContext(Dispatchers.Main) {
            exoPlayer.apply {
                playWhenReady = true
                setMediaSource(mediaSource)
                prepare()
            }
        }
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    exoPlayer.apply {
                        stop()
                        release()
                        scope.launch {
                            withContext(Dispatchers.IO){
                                Log.d("EXOPLAYER","DELAY")
                                delay(1000)
                                lambda()
                            }
                        }
                        Log.d("EXOPLAYER", "Released")
                    }
                } else if (state == Player.STATE_READY) {
                    Log.d("EXOPLAYER", "Ready")
                }
            }
        })
    }

    fun doSomething(): Flow<Int> = flow {
        for (i in 1..3) {
            emit(i)
            Log.d("LOLKALOLKA", i.toString())
        }
        Log.d("LOLKALOLKA", "${Thread.currentThread()}")
    }.flowOn(Dispatchers.Main)


//    fun addListener(listener: ExoListener) {
//        this.listener = WeakReference(listener)
//    }


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
