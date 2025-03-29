package com.example.recipeguide;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import Data.DatabaseHandler;
import Model.Recipe;


public class AddScreen extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Код запроса для галереи
    private EditText recipeNameEditText;
    private EditText preparationTimeEditText;
    private String photoFileName; // Название сохранённого файла
    private ImageButton btnAddImage;
    private Bitmap selectedBitmap; // Для хранения выбранного изображения
    private IngredientsFragmentForAddScreen ingredientFragment = new IngredientsFragmentForAddScreen();
    private RecipeFragmentForAddScreen recipeFragment = new RecipeFragmentForAddScreen();
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_screen);


        SharedPreferences sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        boolean nightMode = sharedPreferences.getBoolean("night", false);

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addScreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DatabaseHandler databaseHelper = new DatabaseHandler(this);

        setupBackButtonHandler();
        photoFromGallery();

        recipeNameEditText = findViewById(R.id.recipe_name);
        preparationTimeEditText = findViewById(R.id.preparation_time);
        btnAddImage = findViewById(R.id.button_add_photo);
        btnAddImage.setOnClickListener(v -> openImageChooser());
        recipeNameEditText.setMovementMethod(new ScrollingMovementMethod());
        recipeNameEditText.post(() -> scrollingText(recipeNameEditText));

        Button buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> saveImageToInternalStorage(databaseHelper));

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
                ingredientButton.setBackgroundResource(R.drawable.rounded_button_default);

                // Устанавливаем фокус на текущую кнопку
                recipeButton.setBackgroundResource(R.drawable.rounded_button_focused);
                setNewFragment(recipeFragment);
            }
        });



    }



    // Открытие галереи для выбора изображения
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Выберите изображение"));
    }

    private void photoFromGallery() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData(); // Получаем URI выбранного изображения
                        loadImageFromUri(imageUri);
                    }
                }
        );
    }

    private void loadImageFromUri(Uri imageUri) {
        try {
            selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri); // Загружаем Bitmap
            btnAddImage.setImageBitmap(selectedBitmap); // Устанавливаем в ImageView
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToInternalStorage(DatabaseHandler databaseHandler) {
        if (validateInputs()) {
            saveData(databaseHandler);
        } else {
            // Show an error message to the user
            Toast.makeText(AddScreen.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveData(DatabaseHandler databaseHandler) {
        String recipeName = recipeNameEditText.getText().toString().trim();
        int preparationTime = Integer.parseInt(preparationTimeEditText.getText().toString().trim());
        String ingredientsData = ingredientFragment != null ? ingredientFragment.getIngredientsData() : "";
        String recipeData = recipeFragment != null ? recipeFragment.getRecipeData() : "";
        // Генерация имени файла
        photoFileName = getFilesDir() + "/saved_image_" + System.currentTimeMillis() + ".png";

        // Сохранение изображения
        File file = new File(photoFileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos); // Сохраняем изображение
            Toast.makeText(this, "Блюдо сохранено в 'Избранное'", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка сохранения изображения", Toast.LENGTH_SHORT).show();
        }

        Recipe recipe = new Recipe(recipeName, photoFileName, preparationTime, recipeData, ingredientsData, 1);

        databaseHandler.addRecipe(recipe);
    }

    private boolean validateInputs() {
        String recipeName = recipeNameEditText.getText().toString().trim();
        String preparationTime = preparationTimeEditText.getText().toString().trim();

        boolean isActivityDataValid = !recipeName.isEmpty() && !preparationTime.isEmpty();
        boolean isIngredientsDataValid = ingredientFragment != null && ingredientFragment.validateInputs();
        boolean isRecipeDataValid = recipeFragment != null && recipeFragment.validateInputs();
        boolean isImageDataValid = selectedBitmap != null;

        return isActivityDataValid && isIngredientsDataValid && isRecipeDataValid && isImageDataValid;
    }

    private void scrollingText(EditText nameDish) {
        if (isTextOverflowing(nameDish)) {
            // Запускаем анимацию прокрутки вниз
            float fullHeight = nameDish.getLineCount() * nameDish.getLineHeight() - nameDish.getHeight();
            ObjectAnimator animatorDown = ObjectAnimator.ofInt(nameDish, "scrollY", 0, (int) fullHeight);
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
    private boolean isTextOverflowing(EditText textView) {
        Rect bounds = new Rect();
        textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().length(), bounds);
        int textHeight = textView.getLineCount() * textView.getLineHeight();
        return textHeight > textView.getHeight(); // Если высота текста больше TextView
    }

    private void setNewFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_layout_ingredients, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }


    public void goAddScreen(View view) {
        Intent intent = new Intent(this, AddScreen.class);
        startActivity(intent);
    }

    public void goHome(View view) {
        Intent intent = new Intent(this, MainScreen.class);
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

    public void goBack(View view) {
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
