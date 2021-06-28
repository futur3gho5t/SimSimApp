package az.azreco.simsimapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import dagger.hilt.android.HiltAndroidApp;

import static az.azreco.simsimapp.constant.NotificationConstants.NOTIFICATION_CHANNEL_ID;
import static az.azreco.simsimapp.constant.NotificationConstants.NOTIFICATION_TITLE;

@HiltAndroidApp
public class SimSimApplication extends Application {

    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        instance = this;
    }


    public static Context getContext() {
        return instance.getApplicationContext();
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_TITLE,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
