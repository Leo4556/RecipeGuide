package com.example.recipeguide;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OptionsScreen extends AppCompatActivity {

    Switch switcher, notificationSwitch;
    boolean nightMode;
    SharedPreferences sharedPreferences, preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_options_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        getSupportActionBar().hide();

        notificationSwitch = findViewById(R.id.switch_notification);
        switcher = findViewById(R.id.switch_theme);
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("night", false);

        if (nightMode) {
            switcher.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        }

        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", false);

                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", true);
                }
                editor.apply();
                
            }
        });

        preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        boolean isEnabled = preferences.getBoolean("notifications_enabled", false);
        notificationSwitch.setChecked(isEnabled);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestNotificationPermission();
            } else {
                disableNotifications();
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
        });


        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", false);

        if (notificationsEnabled) {
            scheduleNotification();
        }

    }

    private void enableNotifications() {
        NotificationHelper.createNotificationChannel(this);
        Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show();
    }

    private void disableNotifications() {
        Toast.makeText(this, "Уведомления отключены", Toast.LENGTH_SHORT).show();
    }


    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                enableNotifications();
            }
        } else {
            enableNotifications();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableNotifications();
                } else {
                    Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show();
                    notificationSwitch.setChecked(false);
                }
            });

    private void scheduleNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = System.currentTimeMillis() + 120 * 60 * 1000; // 120 минут (2 часа)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, 120 * 60 * 1000, pendingIntent);
    }


    public static boolean getCurrentTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MODE", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("night", false);
    }

    public void goHome(View view) {
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    public void goAddScreen(View view) {
        Intent intent = new Intent(this, AddScreen.class);
        startActivity(intent);
    }

    public void goFavourites(View view) {
        Intent intent = new Intent(this, FavouritesScreen.class);
        startActivity(intent);
    }

    public void goOptions(View view) {
        Intent intent = new Intent(this, OptionsScreen.class);
        startActivity(intent);
    }


}