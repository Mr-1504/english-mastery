package com.example.practicetoeicapp.data;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.practicetoeicapp.BuildConfig;
import com.example.practicetoeicapp.util.AppLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseStorageRepository {
    public interface UploadCallback {
        void onSuccess(String publicUrl);

        void onError(Exception e);
    }

    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.parse("application/json; charset=utf-8");
    private static final String FUNCTION_PATH = "/functions/v1/media-upload-url";
    private static final String TAG = "SupabaseStorage";

    private static SupabaseStorageRepository instance;
    private final OkHttpClient client;
    private final Handler mainHandler;

    private SupabaseStorageRepository() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized SupabaseStorageRepository getInstance() {
        if (instance == null) {
            instance = new SupabaseStorageRepository();
        }
        return instance;
    }

    public void uploadImage(Context context, Uri uri, String pathPrefix, UploadCallback callback) {
        upload(context, uri, pathPrefix, "image/", callback);
    }

    public void uploadAudio(Context context, Uri uri, String pathPrefix, UploadCallback callback) {
        upload(context, uri, pathPrefix, "audio/", callback);
    }

    private void upload(Context context, Uri uri, String pathPrefix, String expectedMimePrefix,
                        UploadCallback callback) {
        logDebug("upload start: prefix=" + pathPrefix + ", expected=" + expectedMimePrefix + ", uri=" + uri);
        if (BuildConfig.SUPABASE_URL.trim().isEmpty()
                || BuildConfig.SUPABASE_ANON_KEY.trim().isEmpty()) {
            postError(callback, new IllegalStateException("Supabase is not configured"));
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            postError(callback, new IllegalStateException("Please sign in again"));
            return;
        }
        logDebug("current user: " + user.getUid());

        new Thread(() -> {
            try {
                String mimeType = MediaTypeUtil.getMimeType(context, uri);
                logDebug("resolved mimeType=" + mimeType);
                if (!mimeType.startsWith(expectedMimePrefix)) {
                    throw new IllegalArgumentException("Unsupported file type: " + mimeType);
                }
                byte[] bytes = readBytes(context, uri);
                logDebug("read bytes=" + bytes.length);
                if (bytes.length == 0) {
                    throw new IllegalArgumentException("Selected file is empty");
                }
                boolean isAudio = expectedMimePrefix.startsWith("audio/");
                String path = pathPrefix + UUID.randomUUID()
                        + MediaTypeUtil.extensionForMimeType(mimeType, isAudio);
                logDebug("upload path=" + path);

                user.getIdToken(false).addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Exception ex = task.getException();
                        logError("getIdToken failed", ex);
                        postError(callback, new IllegalStateException("Could not verify admin session", ex));
                        return;
                    }
                    logDebug("firebase id token acquired");
                    GetTokenResult result = task.getResult();
                    requestUploadUrl(bytes, mimeType, path, result.getToken(), callback);
                });
            } catch (Exception e) {
                postError(callback, e);
            }
        }).start();
    }

    private void requestUploadUrl(byte[] bytes, String mimeType, String path, String firebaseToken,
                                  UploadCallback callback) {
        try {
            String endpoint = baseUrl() + FUNCTION_PATH;
            logDebug("requestUploadUrl: endpoint=" + endpoint + ", path=" + path + ", contentType=" + mimeType);
            JSONObject body = new JSONObject();
            body.put("path", path);
            body.put("contentType", mimeType);

            Request request = new Request.Builder()
                    .url(endpoint)
                    .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY)
                    .addHeader("x-firebase-id-token", firebaseToken)
                    .post(RequestBody.create(JSON_MEDIA_TYPE, body.toString()))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    logError("requestUploadUrl network failure", e);
                    postError(callback, e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try (Response current = response) {
                        String responseBody = current.body() == null ? "" : current.body().string();
                        logDebug("requestUploadUrl response: code=" + current.code() + ", body=" + truncate(responseBody));
                        if (!current.isSuccessful()) {
                            throw new IllegalStateException("Upload URL request failed: "
                                    + current.code() + " " + responseBody);
                        }
                        JSONObject json = new JSONObject(responseBody);
                        String signedUrl = json.optString("signedUrl");
                        String publicUrl = json.optString("publicUrl");
                        if (signedUrl.isEmpty()) {
                            throw new IllegalStateException("Missing signed upload URL");
                        }
                        if (publicUrl.isEmpty()) {
                            publicUrl = publicUrl(path);
                        }
                        uploadBytes(bytes, mimeType, absoluteSignedUrl(signedUrl), publicUrl, callback);
                    } catch (Exception e) {
                        postError(callback, e);
                    }
                }
            });
        } catch (Exception e) {
            postError(callback, e);
        }
    }

    private void uploadBytes(byte[] bytes, String mimeType, String signedUrl, String publicUrl,
                             UploadCallback callback) {
        logDebug("uploadBytes: size=" + bytes.length + ", mimeType=" + mimeType);
        Request request = new Request.Builder()
                .url(signedUrl)
                .put(RequestBody.create(MediaType.parse(mimeType), bytes))
                .addHeader("x-upsert", "true")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                logError("uploadBytes network failure", e);
                postError(callback, e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response current = response) {
                    String responseBody = current.body() == null ? "" : current.body().string();
                    logDebug("uploadBytes response: code=" + current.code() + ", body=" + truncate(responseBody));
                    if (!current.isSuccessful()) {
                        throw new IllegalStateException("Media upload failed: "
                                + current.code() + " " + responseBody);
                    }
                    logDebug("upload success, publicUrl=" + publicUrl);
                    postSuccess(callback, publicUrl);
                } catch (Exception e) {
                    postError(callback, e);
                }
            }
        });
    }

    private byte[] readBytes(Context context, Uri uri) throws Exception {
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new IllegalArgumentException("Could not open selected file");
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private String baseUrl() {
        String url = BuildConfig.SUPABASE_URL.trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String publicUrl(String path) {
        return baseUrl() + "/storage/v1/object/public/"
                + BuildConfig.SUPABASE_MEDIA_BUCKET + "/" + path;
    }

    private String absoluteSignedUrl(String signedUrl) {
        if (signedUrl.startsWith("http://") || signedUrl.startsWith("https://")) {
            return signedUrl;
        }
        if (signedUrl.startsWith("/storage/v1")) {
            return baseUrl() + signedUrl;
        }
        if (signedUrl.startsWith("/")) {
            return baseUrl() + "/storage/v1" + signedUrl;
        }
        return baseUrl() + "/storage/v1/" + signedUrl;
    }

    private void postSuccess(UploadCallback callback, String publicUrl) {
        mainHandler.post(() -> callback.onSuccess(publicUrl));
    }

    private void postError(UploadCallback callback, Exception e) {
        logError("postError: " + (e == null ? "unknown" : e.getMessage()), e);
        mainHandler.post(() -> callback.onError(e));
    }

    private void logDebug(String message) {
        AppLogger.d(TAG, message);
    }

    private void logError(String message, Exception e) {
        AppLogger.e(TAG, message, e);
    }

    private String truncate(String value) {
        if (value == null) return "";
        return value.length() > 500 ? value.substring(0, 500) + "..." : value;
    }
}
