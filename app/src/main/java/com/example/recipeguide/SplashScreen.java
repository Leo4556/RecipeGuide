package com.example.recipeguide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.common.MlKit;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Locale;

import Data.DatabaseHandler;
import Model.Recipe;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean russianLanguage;
    private static Translator enToRuTranslator, ruToEnTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.splash_activity);

        FirebaseApp.initializeApp(this);
        MlKit.initialize(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.motionLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        uploadRecipesFirebase(this);

        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("language")) {
            // Определяем системный язык
            String systemLanguage = getSystemLanguage();

            // Если системный язык русский, устанавливаем русский, иначе — английский
            boolean defaultRussian = systemLanguage.equals("ru");
            editor.putBoolean("language", defaultRussian);
            editor.apply();

            russianLanguage = defaultRussian; // Применяем значение в переменную
        } else {
            russianLanguage = sharedPreferences.getBoolean("language", false); // Загружаем сохранённое значение
        }

        if (sharedPreferences.getBoolean("language", false)){
            setAppLocale("ru");
        }else{
            setAppLocale("en");
        }

        User.username = sharedPreferences.getString("username", User.username);
        User.userImage = sharedPreferences.getString("userImage", User.userImage);

        // Перевод с английского на русский
        enToRuTranslator();
        // Перевод с русского на английский
        ruToEnTranslator();


        /*TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sharedPreferences.getBoolean("language", false) ?
                        TranslateLanguage.RUSSIAN : TranslateLanguage.ENGLISH)
                .setTargetLanguage(sharedPreferences.getBoolean("language", false) ?
                        TranslateLanguage.ENGLISH : TranslateLanguage.RUSSIAN)
                .build();

        Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded();*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this, MainScreen.class));
                finish();
            }
        }, 2500);

    }

    private void uploadRecipesFirebase(Context context) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes");

        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseHandler dbHelper = new DatabaseHandler(context);
                Recipe recipe = new Recipe();

                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    recipe.setId(recipeSnapshot.getKey());
                    recipe.setName(recipeSnapshot.child("name").getValue(String.class));
                    recipe.setName_en(recipeSnapshot.child("name_en").getValue(String.class));
                    recipe.setImage(recipeSnapshot.child("image").getValue(String.class));
                    recipe.setCookingTime(recipeSnapshot.child("cookingTime").getValue(Integer.class));
                    recipe.setIngredient(recipeSnapshot.child("ingredient").getValue(String.class));
                    recipe.setIngredient_en(recipeSnapshot.child("ingredient_en").getValue(String.class));
                    recipe.setRecipe(recipeSnapshot.child("recipe").getValue(String.class));
                    recipe.setRecipe_en(recipeSnapshot.child("recipe_en").getValue(String.class));
                    recipe.setIsFavorite(0);

                    dbHelper.insertOrUpdateRecipe(recipe);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Ошибка загрузки данных: " + error.getMessage());
            }
        });

    }

    public static Translator enToRuTranslator() {
        if (enToRuTranslator == null) {
            TranslatorOptions enToRuOptions = new TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.RUSSIAN)
                    .build();
            Translator enToRuTranslator = Translation.getClient(enToRuOptions);
            enToRuTranslator.downloadModelIfNeeded();
        }
        return enToRuTranslator;
    }

    public static Translator ruToEnTranslator() {
        if (ruToEnTranslator == null) {
            TranslatorOptions ruToEnOptions = new TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.RUSSIAN)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build();
            Translator ruToEnTranslator = Translation.getClient(ruToEnOptions);
            ruToEnTranslator.downloadModelIfNeeded();
        }
        return ruToEnTranslator;
    }

    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

    }
    private String getSystemLanguage() {
        return Locale.getDefault().getLanguage(); // Возвращает "ru" для русского, "en" для английского
    }

}