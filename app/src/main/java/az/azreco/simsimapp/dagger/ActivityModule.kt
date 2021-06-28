package az.azreco.simsimapp.dagger

import android.content.Context
import az.azreco.simsimapp.other.ExxoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

//    @ActivityContext
//    @Provides
//    fun provideActivityContext(): Context = MainActivity()

//    @ActivityScoped
//    @Provides
//    fun provideHomeScenario(app: Context) = HomeScenario(mContext = app)
//

}