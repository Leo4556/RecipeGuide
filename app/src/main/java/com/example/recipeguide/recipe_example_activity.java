package com.example.recipeguide;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;

import Data.DatabaseHandler;
import Model.Recipe;

public class recipe_example_activity extends AppCompatActivity {


    private Ingredient_Fragment_Example ingredientFragment = new Ingredient_Fragment_Example();
    private Recipe_Fragment_Example receptFragment = new Recipe_Fragment_Example();
    TypedValue typedValue = new TypedValue();
    private boolean isFavorite = false;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recipe_example);
        setupBackButtonHandler();
        ImageButton saveFavoritesButton = findViewById(R.id.button_save_favorites);
        int dishId = getIntent().getIntExtra("dish_id", -1); // -1 — значение по умолчанию
        DatabaseHandler databaseHelper = new DatabaseHandler(this);

        saveFavoritesButton.setOnClickListener(view -> toggleFavoriteButton(dishId, saveFavoritesButton, databaseHelper));
        loadData(dishId, databaseHelper);

        TextView nameDish = findViewById(R.id.name_dish);
        nameDish.setMovementMethod(new ScrollingMovementMethod());
        nameDish.post(() ->scrollingText(nameDish));

        Button ingredientButton = findViewById(R.id.ingredient);
        Button recipeButton = findViewById(R.id.recipe);

        setNewFragment(ingredientFragment);
        ingredientButton.setBackgroundResource(R.drawable.rounded_button_focused);


        ingredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                recipeButton.setBackgroundResource(R.drawable.rounded_button_default);

                // Устанавливаем фокус на текущую кнопку
                ingredientButton.setBackgroundResource(R.drawable.rounded_button_focused);
                setNewFragment(ingredientFragment);

            }
        });

        recipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dishId != -1) {
                    Recipe selectedDish = databaseHelper.getRecipe(dishId);
                    sendDishDataToFragment(receptFragment, selectedDish);
                }
                ingredientButton.setBackgroundResource(R.drawable.rounded_button_default);

                // Устанавливаем фокус на текущую кнопку
                recipeButton.setBackgroundResource(R.drawable.rounded_button_focused);
                setNewFragment(receptFragment);
            }
        });


        //Статусбар белого цвета
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }

    private void loadData(int dishId, DatabaseHandler databaseHelper){
        if (dishId != -1) {
            // Здесь можно использовать dishId для загрузки данных из базы

            Recipe selectedDish = databaseHelper.getRecipe(dishId);

            // Логика отображения данных выбранного блюда
            if (selectedDish != null) {
                TextView dishName = findViewById(R.id.name_dish);
                ImageView dishImage = findViewById(R.id.image_dish);
                TextView dishCookingTime = findViewById(R.id.Cooking_time);
                ImageButton dishFavorite = findViewById(R.id.button_save_favorites);

                dishName.setText(selectedDish.getName());
                String imagePath = selectedDish.getImage();
                if (imagePath != null) {
                    File imgFile = new File(imagePath);
                    if (imgFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        dishImage.setImageBitmap(bitmap); // Устанавливаем изображение в ImageView
                    }
                } else {
                    // Устанавливаем изображение-заглушку, если данных нет
                    dishImage.setImageResource(R.drawable.dumplings);
                }

                dishCookingTime.setText("Время приготовления: " + selectedDish.getCookingTime() + " мин");
                if(selectedDish.getIsFavorite() == 0){
                    getTheme().resolveAttribute(R.attr.buttonHeartIcon, typedValue, true);
                    dishFavorite.setImageResource(typedValue.resourceId);
                }else {
                    dishFavorite.setImageResource(R.drawable.button_heart_red);
                }
                sendDishDataToFragment(ingredientFragment, selectedDish);
            }
        }
    }
    private void setNewFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_layout, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void sendDishDataToFragment(Fragment fragment, Recipe selectedDish) {
        // Создаём объект Bundle для передачи данных
        Bundle bundle = new Bundle();
        bundle.putString("dish_ingredients", selectedDish.getIngredient()); // Например, список ингредиентов
        bundle.putString("dish_recipe", selectedDish.getRecipe()); // Например, текст рецепта

        // Передаём Bundle во фрагмент
        fragment.setArguments(bundle);
    }

    private void scrollingText(TextView nameDish){
        if (isTextOverflowing(nameDish)) {
            // Запускаем анимацию прокрутки вниз
            float fullHeight = nameDish.getLineCount() * nameDish.getLineHeight() - nameDish.getHeight();
            ObjectAnimator animatorDown = ObjectAnimator.ofInt(nameDish, "scrollY", 0, (int) fullHeight );
            animatorDown.setDuration(nameDish.getLineCount() * 600);

            // Анимация возврата вверх
            ObjectAnimator animatorUp = ObjectAnimator.ofInt(nameDish, "scrollY", (int) fullHeight, 0);
            animatorUp.setDuration(nameDish.getLineCount() * 600);

            // Последовательное выполнение анимаций
            animatorDown.start();
            animatorDown.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    animatorUp.start();
                }
            });
        }
    }

    // Метод для проверки, выходит ли текст за пределы TextView
    private boolean isTextOverflowing(TextView textView) {
        Rect bounds = new Rect();
        textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().length(), bounds);
        int textHeight = textView.getLineCount() * textView.getLineHeight();
        return textHeight > textView.getHeight(); // Если высота текста больше TextView
    }
    private void toggleFavoriteButton(int dishId, ImageButton saveFavoritesButton, DatabaseHandler databaseHandler) {
        if (isFavorite) {
            updateFavorite(dishId, databaseHandler, 0);

            // Получаем ресурс атрибута темы
            getTheme().resolveAttribute(R.attr.buttonHeartIcon, typedValue, true);
            // Устанавливаем ресурс в ImageButton
            saveFavoritesButton.setImageResource(typedValue.resourceId);
            isFavorite = false;
        } else {
            updateFavorite(dishId, databaseHandler, 1);
            // Устанавливаем "красное сердце"
            saveFavoritesButton.setImageResource(R.drawable.button_heart_red);
            isFavorite = true;
        }
    }

    private void updateFavorite(int dishId, DatabaseHandler databaseHandler, int isFavorite){
        if(dishId != -1){
            Recipe selectedDish = databaseHandler.getRecipe(dishId);
            if (selectedDish != null){
                selectedDish.setIsFavorite(isFavorite);
                databaseHandler.updateRecipe(selectedDish);
            }
        }
    }

    public void goAddScreen(View view){
        Intent intent = new Intent(this, AddScreen.class);
        startActivity(intent);
    }

    public void goHome(View view){
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    public void goFavourites(View view){
        Intent intent = new Intent(this, FavouritesScreen.class);
        startActivity(intent);
    }

    public void goOptions(View view){
        Intent intent = new Intent(this, OptionsScreen.class);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

    private void setupBackButtonHandler() {
        // Устанавливаем обработчик для кнопки "Назад"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Завершаем текущую Activity, возвращаемся на предыдущую
                finish();
            }
        });
    }
}