package az.azreco.simsimapp.scene

import az.azreco.simsimapp.util.RecursiveCaller
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import javax.inject.Inject

class MainScene() : RecursiveCaller {

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var rawResourceDataSource: RawResourceDataSource



    override suspend fun callRecursively(keyWords: String, filterFunc: suspend (String) -> Unit) {


    }

}