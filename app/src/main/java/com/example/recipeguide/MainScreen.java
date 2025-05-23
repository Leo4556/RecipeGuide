package com.example.recipeguide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import Data.DatabaseHandler;

public class MainScreen extends AppCompatActivity {

    ListView listView;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isNightMode, russianLanguage;
    private static boolean isCloudinaryInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        initConfig();
        isNightMode = OptionsScreen.getCurrentTheme(this);

        if (isNightMode) {
            // Действия для тёмной темы
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            // Действия для светлой темы
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.main_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listView);

        DatabaseHandler databaseHelper = new DatabaseHandler(this);
        ArrayList<Dish> dishes = databaseHelper.getRecommendedRecipe(this);
        DishAdapter adapter = new DishAdapter(this, dishes);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Получаем выбранное блюдо
            Dish selectedDish = adapter.getItem(position);

            if (selectedDish != null) {
                // Создаём Intent и передаём ID блюда
                Intent intent = new Intent(getApplicationContext(), recipe_example_activity.class);
                intent.putExtra("dish_id", selectedDish.getId()); // Передаём ID блюда
                startActivity(intent);
            }
        });

    }

    private void initConfig() {
        if (!isCloudinaryInitialized) { // Проверяем, была ли уже инициализация
            Map config = new HashMap();
            config.put("cloud_name", "dx7hf8znl");
            config.put("api_key", "182439927864613");
            config.put("api_secret", "U-dXjr3iiTkAM_SnAOu3C613_vE");

            MediaManager.init(this, config);
            isCloudinaryInitialized = true; // Устанавливаем флаг после инициализации
            Log.d("Cloudinary", "Cloudinary успешно инициализирован!");
        } else {
            Log.d("Cloudinary", "Cloudinary уже был инициализирован, повторное выполнение не требуется.");
        }
    }


    private String getSystemLanguage() {
        return Locale.getDefault().getLanguage(); // Возвращает "ru" для русского, "en" для английского
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

    public void goToSearch(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void goOptions(View view) {
        Intent intent = new Intent(this, OptionsScreen.class);
        startActivity(intent);
    }


}