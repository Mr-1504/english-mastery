package com.example.practicetoeicapp.theme;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.practicetoeicapp.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class ThemePrefs {

    public static final int MODE_SYSTEM = 0;
    public static final int MODE_LIGHT = 1;
    public static final int MODE_DARK = 2;

    private static final String PREFS_NAME = "app_preferences";
    private static final String KEY_THEME_MODE = "theme_mode";

    private ThemePrefs() {
    }

    public static void applySavedTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(toNightMode(getMode(context)));
    }

    public static int getMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_THEME_MODE, MODE_SYSTEM);
    }

    public static void setMode(Context context, int mode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_THEME_MODE, mode)
                .apply();
        AppCompatDelegate.setDefaultNightMode(toNightMode(mode));
    }

    public static String getModeLabel(Context context, int mode) {
        return context.getString(getModeLabelRes(mode));
    }

    public static void showThemeDialog(Context context) {
        CharSequence[] labels = new CharSequence[]{
                context.getString(R.string.theme_system),
                context.getString(R.string.theme_light),
                context.getString(R.string.theme_dark)
        };
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.theme_title)
                .setSingleChoiceItems(labels, getMode(context), (dialog, which) -> {
                    setMode(context, which);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.theme_cancel, null)
                .show();
    }

    private static int toNightMode(int mode) {
        switch (mode) {
            case MODE_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case MODE_DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case MODE_SYSTEM:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    private static int getModeLabelRes(int mode) {
        switch (mode) {
            case MODE_LIGHT:
                return R.string.theme_light;
            case MODE_DARK:
                return R.string.theme_dark;
            case MODE_SYSTEM:
            default:
                return R.string.theme_system;
        }
    }
}
