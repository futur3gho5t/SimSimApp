package az.azreco.simsimapp.dagger

import android.content.Context
import az.azreco.simsimapp.exoplayer.MyExoPlayer
import az.azreco.simsimapp.exoplayer.WildExoPlayer
import az.azreco.simsimapp.other.ExxoPlayer
import az.azreco.simsimapp.scenario.CallScenario
import az.azreco.simsimapp.scenario.SendSmsScenario
import az.azreco.simsimapp.scenario.WhatsappScenario
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideWildExoPlayer(@ApplicationContext app: Context) = WildExoPlayer(context = app)


    @Singleton
    @Provides
    fun provideExoPlayer(@ApplicationContext app: Context) = SimpleExoPlayer.Builder(app).build()

    @Singleton
    @Provides
    fun provideRawDataSource(@ApplicationContext app: Context) = RawResourceDataSource(app)

    @Provides
    fun provideCallScenario(exoPlayer: WildExoPlayer) = CallScenario(exoPlayer = exoPlayer)


    @Provides
    fun provideSmsScenario(exoPlayer: WildExoPlayer) = SendSmsScenario(exoPlayer = exoPlayer)


    @Provides
    fun provideWhatsappScenario(exoPlayer: WildExoPlayer) = WhatsappScenario(exoPlayer = exoPlayer)
}