package com.example.practicetoeicapp.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

public final class MediaTypeUtil {
    public static final String PREFIX_LISTENING_IMAGES = "listening/images/";
    public static final String PREFIX_LISTENING_AUDIO = "listening/audio/";
    public static final String PREFIX_VOCABULARY_IMAGES = "vocabulary/images/";
    public static final String PREFIX_VOCABULARY_AUDIO = "vocabulary/audio/";
    public static final String PREFIX_AVATARS = "avatars/";

    private MediaTypeUtil() {
    }

    public static String getMimeType(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        String mimeType = resolver.getType(uri);
        if (mimeType != null && !mimeType.isEmpty()) {
            return mimeType;
        }
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null && !extension.isEmpty()) {
            String fromExtension = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension.toLowerCase());
            if (fromExtension != null) {
                return fromExtension;
            }
        }
        return "application/octet-stream";
    }

    public static String extensionForMimeType(String mimeType, boolean audio) {
        if (mimeType == null) {
            return audio ? ".mp3" : ".jpg";
        }
        switch (mimeType.toLowerCase()) {
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/webp":
                return ".webp";
            case "audio/mpeg":
                return ".mp3";
            case "audio/mp4":
            case "audio/aac":
                return ".m4a";
            case "audio/wav":
            case "audio/x-wav":
                return ".wav";
            case "audio/ogg":
                return ".ogg";
            default:
                return audio ? ".mp3" : ".jpg";
        }
    }
}
