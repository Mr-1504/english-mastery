package com.example.practicetoeicapp.luyennghe;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.example.practicetoeicapp.R;
import com.example.practicetoeicapp.ui.UiFeedback;

public class MediaPlayerUtils {
    public static final String URL_MEDIA_SAMPLE  = "https://github.com/duchmdev/huynhminhduc_assets_android_firebase_project_study_english/raw/main/LuyenNghe/Hey%20Mama%20-%20David%20Guetta%20feat_%20Nicki%20Mina.mp3";

    public static final String LOG_TAG= "MediaPlayerTutorial";


    // String videoURL = "https://raw.githubusercontent.com/o7planning/webexamples/master/_testdatas_/yodel.mp3";
    public static void playURLMedia(Activity activity, MediaPlayer mediaPlayer, String videoURL)  {
        try {
            Log.i(LOG_TAG, "Media URL: "+ videoURL);

            Uri uri= Uri.parse( videoURL );
            mediaPlayer.setDataSource(activity, uri);
            mediaPlayer.prepareAsync();

        } catch(Exception e) {
            Log.e(LOG_TAG, "Error Play URL Media: "+ e.getMessage());
            UiFeedback.showError(activity, activity.getString(R.string.form_audio_error));
            e.printStackTrace();
        }
    }
}
