package com.example.recipeguide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", false);

        if (notificationsEnabled) {
            NotificationHelper.showNotification(context, context.getString(R.string.title_notification), context.getString(R.string.message_notification));
        }
    }
}
