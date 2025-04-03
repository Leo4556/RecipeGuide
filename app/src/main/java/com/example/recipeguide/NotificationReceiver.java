package com.example.recipeguide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.showNotification(context, "Напоминание!", "Не забудьте попробовать новый рецепт сегодня!");
    }
}
