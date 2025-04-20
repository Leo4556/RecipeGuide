package com.example.recipeguide;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

        setupNotificationSwitch();

        Button supportButton = findViewById(R.id.support);
        supportButton.setOnClickListener(v -> showConfirmationSupport());

        Button politicsConfidence = findViewById(R.id.politicsConfidence);
        politicsConfidence.setOnClickListener(v -> showConfirmationPolicy());

    }

    private void setupNotificationSwitch() {
        preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        // Устанавливаем состояние переключателя из настроек
        boolean isEnabled = preferences.getBoolean("notifications_enabled", false);
        notificationSwitch.setChecked(isEnabled);

        // Обработчик изменения состояния переключателя
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            handleNotificationToggle(isChecked);
        });
    }

    private void handleNotificationToggle(boolean isChecked) {
        if (isChecked) {
            checkAndRequestNotificationPermission();
        } else {
            disableNotifications();
        }

        preferences.edit().putBoolean("notifications_enabled", isChecked).apply();
    }

    private void enableNotifications() {
        NotificationHelper.createNotificationChannel(this);
        scheduleNotification();
        Toast.makeText(this, getString(R.string.notification_on), Toast.LENGTH_SHORT).show();
    }

    private void disableNotifications() {
        Toast.makeText(this, getString(R.string.notification_off), Toast.LENGTH_SHORT).show();
    }

    // Проверка и запрос разрешений на уведомления
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                enableNotifications();
            }
        } else {
            enableNotifications();
        }
    }

    // Обработчик результата запроса разрешений
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = System.currentTimeMillis() + 5 * 60 * 1000;

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, triggerTime, 24 * 60 * 60 * 1000, pendingIntent); // 24 часа
    }

    private void showConfirmationSupport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation));
        builder.setMessage(getString(R.string.support_message));
        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            openLink("Tokarchuk_VJ_22@student.grsu.by"); // Укажите вашу ссылку здесь
            dialog.dismiss();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("mailto:" + url +
                "?subject=" + Uri.encode(getString(R.string.subject)) +
                "&body=" + Uri.encode(getString(R.string.body))));

        startActivity(intent);

    }


    private void showConfirmationPolicy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation));
        builder.setMessage(getString(R.string.policy_message));
        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            downloadFile(); // Метод для загрузки файла из assets
            dialog.dismiss();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void downloadFile() {
        String fileName = "privacy_policy.pdf"; // Имя файла в папке assets
        String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

        try {
            // Читаем файл из папки assets
            InputStream inputStream = getAssets().open(fileName);
            File destinationFile = new File(destinationPath, fileName);

            // Создаём поток для записи в папку Downloads
            FileOutputStream outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Закрываем потоки
            outputStream.close();
            inputStream.close();

            Toast.makeText(this, getString(R.string.successful_saving_policy) + " " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.unsuccessful_saving_policy), Toast.LENGTH_SHORT).show();
        }
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