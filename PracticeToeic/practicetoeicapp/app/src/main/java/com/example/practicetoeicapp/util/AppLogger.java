package com.example.practicetoeicapp.util;

import android.content.Context;
import android.util.Log;

import com.example.practicetoeicapp.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class AppLogger {
    private static final String DEFAULT_TAG = "AppLogger";
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "app-debug.log";
    private static final Object LOCK = new Object();
    private static volatile File logFile;

    private AppLogger() {
    }

    public static void init(Context context) {
        if (context == null) return;
        try {
            File dir = new File(context.getApplicationContext().getFilesDir(), LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            logFile = new File(dir, LOG_FILE);
        } catch (Exception e) {
            Log.e(DEFAULT_TAG, "init logger failed", e);
        }
    }

    public static String getLogFilePath(Context context) {
        if (logFile == null && context != null) {
            init(context);
        }
        return logFile != null ? logFile.getAbsolutePath() : "";
    }

    public static void d(String tag, String message) {
        log(Log.DEBUG, tag, message, null);
    }

    public static void e(String tag, String message) {
        log(Log.ERROR, tag, message, null);
    }

    public static void e(String tag, String message, Throwable throwable) {
        log(Log.ERROR, tag, message, throwable);
    }

    private static void log(int priority, String tag, String message, Throwable throwable) {
        String safeTag = tag == null ? DEFAULT_TAG : tag;
        String safeMessage = message == null ? "" : message;
        if (priority == Log.ERROR) {
            if (throwable != null) {
                Log.e(safeTag, safeMessage, throwable);
            } else {
                Log.e(safeTag, safeMessage);
            }
        } else if (BuildConfig.DEBUG) {
            Log.d(safeTag, safeMessage, throwable);
        }

        if (logFile == null) return;
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
        String level = priority == Log.ERROR ? "E" : "D";
        StringBuilder sb = new StringBuilder();
        sb.append(ts).append(" ").append(level).append("/").append(safeTag).append(": ")
                .append(safeMessage);
        if (throwable != null) {
            sb.append("\n").append(stackTraceToString(throwable));
        }
        sb.append("\n");

        synchronized (LOCK) {
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(sb.toString());
            } catch (Exception e) {
                Log.e(DEFAULT_TAG, "write log file failed", e);
            }
        }
    }

    private static String stackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
