package com.example.practicetoeicapp.ui.practice;

import android.app.Activity;

public class PracticeItem {
    private final int iconRes;
    private final String title;
    private final String subtitle;
    private final Class<? extends Activity> target;

    public PracticeItem(int iconRes, String title, String subtitle, Class<? extends Activity> target) {
        this.iconRes = iconRes;
        this.title = title;
        this.subtitle = subtitle;
        this.target = target;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Class<? extends Activity> getTarget() {
        return target;
    }
}
