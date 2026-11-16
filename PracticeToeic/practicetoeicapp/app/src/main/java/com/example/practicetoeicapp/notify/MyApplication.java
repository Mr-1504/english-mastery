package com.example.practicetoeicapp.notify;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.practicetoeicapp.theme.ThemePrefs;
import com.example.practicetoeicapp.util.AppLogger;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyApplication extends Application {
    public static final String CHANNEL_ID="channel_service_example";
    @Override
    public void onCreate(){
        super.onCreate();
        AppLogger.init(this);
        AppLogger.d("MyApplication", "onCreate, logFile=" + AppLogger.getLogFilePath(this));
        ThemePrefs.applySavedTheme(this);
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        createChannelNotification();
    }
    private void createChannelNotification() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel Service Example",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
