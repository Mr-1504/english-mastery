package com.example.practicetoeicapp.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.practicetoeicapp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;

public final class UiFeedback {

    private static final long ANSWER_DURATION_MS = 1200L;
    private static final long TRANSIENT_DURATION_MS = 1500L;

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static WeakReference<PopupWindow> transientPopup = new WeakReference<>(null);
    private static Runnable transientDismissRunnable;

    private UiFeedback() {
    }

    public static AlertDialog showLoading(Activity activity, int messageRes) {
        if (!isUsable(activity)) {
            return null;
        }
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null);
        TextView message = view.findViewById(R.id.tvLoadingMessage);
        message.setText(messageRes);
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setView(view)
                .setCancelable(false)
                .create();
        dialog.show();
        return dialog;
    }

    public static void setLoadingMessage(AlertDialog dialog, int messageRes) {
        if (dialog == null || !dialog.isShowing()) {
            return;
        }
        TextView message = dialog.findViewById(R.id.tvLoadingMessage);
        if (message != null) {
            message.setText(messageRes);
        }
    }

    public static void dismiss(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static void showError(Activity activity, CharSequence message) {
        if (!isUsable(activity)) {
            return;
        }
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.feedback_error_title)
                .setMessage(message)
                .setPositiveButton(R.string.feedback_ok, null)
                .show();
    }

    public static void showInfo(Activity activity, CharSequence message, Runnable onDismiss) {
        if (!isUsable(activity)) {
            return;
        }
        new MaterialAlertDialogBuilder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.feedback_ok, (dialog, which) -> {
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .show();
    }

    public static void showTransient(Activity activity, CharSequence message, boolean success) {
        showPopup(activity, message, success, TRANSIENT_DURATION_MS);
    }

    public static void showAnswer(Activity activity, boolean correct) {
        if (!isUsable(activity)) {
            return;
        }
        String message = activity.getString(correct
                ? R.string.feedback_correct
                : R.string.feedback_incorrect);
        showPopup(activity, message, correct, ANSWER_DURATION_MS);
    }

    public static void dismissTransient() {
        PopupWindow popup = transientPopup.get();
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
        }
        transientPopup.clear();
        if (transientDismissRunnable != null) {
            MAIN_HANDLER.removeCallbacks(transientDismissRunnable);
            transientDismissRunnable = null;
        }
    }

    private static void showPopup(Activity activity, CharSequence message, boolean success,
                                  long durationMs) {
        if (!isUsable(activity)) {
            return;
        }
        dismissTransient();

        View content = LayoutInflater.from(activity).inflate(R.layout.popup_feedback, null);
        MaterialCardView card = content.findViewById(R.id.cardFeedback);
        TextView text = content.findViewById(R.id.tvFeedbackMessage);

        int background = ContextCompat.getColor(activity, success
                ? R.color.app_secondary_container
                : R.color.app_error_container);
        int foreground = ContextCompat.getColor(activity, success
                ? R.color.app_on_secondary_container
                : R.color.app_on_error_container);
        card.setCardBackgroundColor(background);
        text.setTextColor(foreground);
        text.setText(message);

        PopupWindow popup = new PopupWindow(content,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                false);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(false);
        popup.setFocusable(false);
        popup.setElevation(dp(activity, 8));

        View anchor = activity.findViewById(android.R.id.content);
        popup.showAtLocation(anchor, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, dp(activity, 72));

        transientPopup = new WeakReference<>(popup);
        transientDismissRunnable = UiFeedback::dismissTransient;
        MAIN_HANDLER.postDelayed(transientDismissRunnable, durationMs);
    }

    private static boolean isUsable(Activity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
