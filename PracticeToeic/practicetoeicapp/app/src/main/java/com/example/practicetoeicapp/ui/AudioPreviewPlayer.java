package com.example.practicetoeicapp.ui;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

public final class AudioPreviewPlayer {

    public interface OnStateListener {
        void onStateChanged(boolean playing);

        void onError();
    }

    private final Context context;
    private MediaPlayer player;
    private OnStateListener listener;

    public AudioPreviewPlayer(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setOnStateListener(OnStateListener listener) {
        this.listener = listener;
    }

    public void toggle(Uri uri) {
        if (player != null) {
            stop();
        } else {
            play(uri);
        }
    }

    public void stop() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (listener != null) {
            listener.onStateChanged(false);
        }
    }

    public void release() {
        stop();
        listener = null;
    }

    private void play(Uri uri) {
        stop();
        if (uri == null) {
            if (listener != null) {
                listener.onError();
            }
            return;
        }
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            player.setDataSource(context, uri);
            player.setOnPreparedListener(mediaPlayer -> {
                mediaPlayer.start();
                if (listener != null) {
                    listener.onStateChanged(true);
                }
            });
            player.setOnCompletionListener(mediaPlayer -> stop());
            player.setOnErrorListener((mediaPlayer, what, extra) -> {
                stop();
                if (listener != null) {
                    listener.onError();
                }
                return true;
            });
            player.prepareAsync();
        } catch (Exception e) {
            stop();
            if (listener != null) {
                listener.onError();
            }
        }
    }
}
