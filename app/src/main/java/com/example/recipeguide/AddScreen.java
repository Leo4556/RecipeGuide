package com.example.recipeguide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import Data.DatabaseHandler;
import Model.Recipe;


public class AddScreen extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Код запроса для галереи
    private EditText recipeNameEditText;
    private EditText preparationTimeEditText;
    private String recipeNameTranslate, ingredientDataTranslate, recipeDataTranslate;
    private String photoFileName; // Название сохранённого файла
    private ImageButton btnAddImage;
    private Bitmap selectedBitmap; // Для хранения выбранного изображения
    private IngredientsFragmentForAddScreen ingredientFragment = new IngredientsFragmentForAddScreen();
    private RecipeFragmentForAddScreen recipeFragment = new RecipeFragmentForAddScreen();
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Uri imageUri;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_screen);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addScreen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DatabaseHandler databaseHelper = new DatabaseHandler(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuth = FirebaseAuth.getInstance();

        setupBackButtonHandler();
        photoFromGallery();

        recipeNameEditText = findViewById(R.id.recipe_name);
        preparationTimeEditText = findViewById(R.id.preparation_time);
        btnAddImage = findViewById(R.id.button_add_photo);
        btnAddImage.setOnClickListener(v -> openImageChooser());
        recipeNameEditText.setMovementMethod(new ScrollingMovementMethod());
        recipeNameEditText.post(() -> scrollingText(recipeNameEditText));

        Button buttonSave = findViewById(R.id.button_save);

        Button ingredientButton = findViewById(R.id.ingredient);
        Button recipeButton = findViewById(R.id.recipe);

        setNewFragment(recipeFragment);
        setNewFragment(ingredientFragment);
        //getSupportFragmentManager().beginTransaction().add(recipeFragment, "recipeFragment").commit();
        ingredientButton.setBackgroundResource(R.drawable.rounded_button_focused);

        ingredientButton.setOnClickListener(v -> {
            ingredientButton.setBackgroundResource(R.drawable.rounded_button_focused);
            recipeButton.setBackgroundResource(R.drawable.rounded_button_default);
            setNewFragment(ingredientFragment);
        });

        recipeButton.setOnClickListener(v -> {
            recipeButton.setBackgroundResource(R.drawable.rounded_button_focused);
            ingredientButton.setBackgroundResource(R.drawable.rounded_button_default);
            setNewFragment(recipeFragment);
        });

        buttonSave.setOnClickListener(v -> saveImageToInternalStorage(databaseHelper));

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
                        imageUri = result.getData().getData(); // Получаем URI выбранного изображения
                        loadImageFromUri(imageUri);
                    }
                }
        );
    }

    private void loadImageFromUri(Uri imageUri) {
        try {
            selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri); // Загружаем Bitmap
            btnAddImage.setImageBitmap(selectedBitmap); // Устанавливаем в ImageView
            btnAddImage.setBackgroundResource(android.R.color.transparent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToInternalStorage(DatabaseHandler databaseHandler) {
        if (validateInputs()) {
            String recipeId = UUID.randomUUID().toString();
            saveData(databaseHandler, recipeId);
            showPublicRecipe(recipeId);
            Toast.makeText(this, getString(R.string.recipe_save_toast), Toast.LENGTH_SHORT).show();
        } else {
            // Show an error message to the user
            Toast.makeText(AddScreen.this, getString(R.string.validation), Toast.LENGTH_SHORT).show();
        }

    }

    private void showPublicRecipe(String recipeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Публикация");
        builder.setMessage("Хотите опубликовать свой рецепт?");
        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            saveDataFirebaseAllRecipe(recipeId); // Метод для загрузки файла из assets
            saveDataFirebaseMyRecipe(recipeId);
            Toast.makeText(this, "Рецепт опубликован", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            saveDataFirebaseMyRecipe(recipeId);
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveDataFirebaseMyRecipe(String recipeId) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("Firebase", "Ошибка: пользователь не найден!");
            return;
        }

        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");


        String recipeName = recipeNameEditText.getText().toString().trim();
        int preparationTime = Integer.parseInt(preparationTimeEditText.getText().toString().trim());
        String ingredientsData = ingredientFragment != null ? ingredientFragment.getIngredientsData() : "";
        String recipeData = recipeFragment != null ? recipeFragment.getRecipeData() : "";

        Dish dish = new Dish();
        ArrayList<Dish> dishList = new ArrayList<>();

        dish.setId(recipeId);
        dish.setRecipeCookingTime(preparationTime);
        // 🔹 ML Kit Перевод
        /*TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sharedPreferences.getBoolean("language", false) ?
                        TranslateLanguage.RUSSIAN : TranslateLanguage.ENGLISH)
                .setTargetLanguage(sharedPreferences.getBoolean("language", false) ?
                        TranslateLanguage.ENGLISH : TranslateLanguage.RUSSIAN)
                .build();

        Translator translator = Translation.getClient(options);
*/

        MediaManager.get().upload(imageUri).option("folder", "recipe_image").callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {

            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String imageUrl = resultData.get("secure_url").toString(); // Ссылка на картинку
                myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("image").setValue(imageUrl.toString());

                dish.setRecipeImage(imageUrl);

            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.e("Cloudinary", "Ошибка загрузки: " + error.getDescription());
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        }).dispatch();



        if (sharedPreferences.getBoolean("language", false)) {

            Translator translator = SplashScreen.ruToEnTranslator();
            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(unused -> {
                        translator.translate(recipeName)
                                .addOnSuccessListener(translatedText ->{
                                        myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("name_en").setValue(translatedText);
                                        dish.setRecipeNameEn(translatedText);
                                        });

                        translator.translate(ingredientsData)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("ingredient_en").setValue(translatedText));

                        translator.translate(recipeData)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("recipe_en").setValue(translatedText));
                    })
                    .addOnFailureListener(e -> Log.e("MLKit", "Ошибка загрузки модели перевода: " + e.getMessage()));

            myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("name").setValue(recipeName);
            myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("recipe").setValue(recipeData);
            myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("ingredient").setValue(ingredientsData);

            dish.setRecipeName(recipeName);
        } else {
            Translator translator = SplashScreen.enToRuTranslator();
            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(unused -> {
                        translator.translate(recipeName)
                                .addOnSuccessListener(translatedText ->{
                                    myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("name").setValue(translatedText);
                                    dish.setRecipeName(translatedText);

                                });
                        translator.translate(ingredientsData)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("ingredient").setValue(translatedText));

                        translator.translate(recipeData)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("recipe").setValue(translatedText));
                    })
                    .addOnFailureListener(e -> Log.e("MLKit", "Ошибка загрузки модели перевода: " + e.getMessage()));

            myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("name_en").setValue(recipeName);
            myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("recipe_en").setValue(recipeData);
            myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("ingredient_en").setValue(ingredientsData);

            dish.setRecipeNameEn(recipeName);

        }

        myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("cookingTime").setValue(preparationTime);
        database.getReference("users").child(user.getUid()).child("isFavorites").child(recipeId).setValue(true);

        SharedPreferences prefs = getSharedPreferences("MODE", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();

        String dishJson = gson.toJson(dishList);
        editor.putString("dish_list", dishJson);
        editor.apply();
        if (!dishList.contains(dish)) {
            dishList.add(dish);
        }

    }

    private void saveDataFirebaseAllRecipe(String recipeId) {

        FirebaseUser user = mAuth.getCurrentUser();

        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("recipes");

        String recipeName = recipeNameEditText.getText().toString().trim();
        int preparationTime = Integer.parseInt(preparationTimeEditText.getText().toString().trim());
        String ingredientsData = ingredientFragment != null ? ingredientFragment.getIngredientsData() : "";
        String recipeData = recipeFragment != null ? recipeFragment.getRecipeData() : "";

        // 🔹 ML Kit Перевод
       /* TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sharedPreferences.getBoolean("language", false) ?
                        TranslateLanguage.RUSSIAN : TranslateLanguage.ENGLISH)
                .setTargetLanguage(sharedPreferences.getBoolean("language", false) ?
                        TranslateLanguage.ENGLISH : TranslateLanguage.RUSSIAN)
                .build();

        Translator translator = Translation.getClient(options);*/


        MediaManager.get().upload(imageUri).option("folder", "recipe_image").callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {

            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String imageUrl = resultData.get("secure_url").toString(); // Ссылка на картинку
                myRef.child(user.getUid()).child("my_recipes").child(recipeId).child("image").setValue(imageUrl.toString());


            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.e("Cloudinary", "Ошибка загрузки: " + error.getDescription());
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        }).dispatch();


        if (sharedPreferences.getBoolean("language", false)) {

            Translator translator = SplashScreen.ruToEnTranslator();
            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(unused -> {
                        translator.translate(recipeName)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(recipeId).child("name_en").setValue(translatedText));

                        translator.translate(ingredientsData)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(recipeId).child("ingredient_en").setValue(translatedText));

                        translator.translate(recipeData)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(recipeId).child("recipe_en").setValue(translatedText));
                    })
                    .addOnFailureListener(e -> Log.e("MLKit", "Ошибка загрузки модели перевода: " + e.getMessage()));

            myRef.child(recipeId).child("name").setValue(recipeName);
            myRef.child(recipeId).child("recipe").setValue(recipeData);
            myRef.child(recipeId).child("ingredient").setValue(ingredientsData);
        } else {
            Translator translator = SplashScreen.enToRuTranslator();
            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(unused -> {
                        translator.translate(recipeName)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(recipeId).child("name").setValue(translatedText));

                        translator.translate(ingredientsData)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(recipeId).child("ingredient").setValue(translatedText));

                        translator.translate(recipeData)
                                .addOnSuccessListener(translatedText ->
                                        myRef.child(recipeId).child("recipe").setValue(translatedText));
                    })
                    .addOnFailureListener(e -> Log.e("MLKit", "Ошибка загрузки модели перевода: " + e.getMessage()));

            myRef.child(recipeId).child("name_en").setValue(recipeName);
            myRef.child(recipeId).child("recipe_en").setValue(recipeData);
            myRef.child(recipeId).child("ingredient_en").setValue(ingredientsData);
        }

        MediaManager.get().upload(imageUri).option("folder", "recipe_image").callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {

            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String imageUrl = resultData.get("secure_url").toString(); // Ссылка на картинку
                myRef.child(recipeId).child("image").setValue(imageUrl.toString());


            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.e("Cloudinary", "Ошибка загрузки: " + error.getDescription());
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        }).dispatch();

        myRef.child(recipeId).child("cookingTime").setValue(preparationTime);
        if (user != null) {
            database.getReference("users").child(user.getUid()).child("isFavorites").child(recipeId).setValue(true);
        }
    }

    private void saveData(DatabaseHandler databaseHandler, String recipeId) {
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

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка сохранения изображения", Toast.LENGTH_SHORT).show();
        }
/*

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sharedPreferences.getBoolean("language", false) ?
                        TranslateLanguage.RUSSIAN : TranslateLanguage.ENGLISH)
                .setTargetLanguage(sharedPreferences.getBoolean("language", false) ?
                        TranslateLanguage.ENGLISH : TranslateLanguage.RUSSIAN)
                .build();
*/

        if (sharedPreferences.getBoolean("language", false)) {

            Translator translator = SplashScreen.ruToEnTranslator();
            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(unused -> {
                        translator.translate(recipeName)
                                .addOnSuccessListener(translatedText ->
                                        recipeNameTranslate = translatedText);

                        translator.translate(ingredientsData)
                                .addOnSuccessListener(translatedText ->
                                        ingredientDataTranslate = translatedText);

                        translator.translate(recipeData)
                                .addOnSuccessListener(translatedText ->
                                        recipeDataTranslate = translatedText);
                    })
                    .addOnFailureListener(e -> Log.e("MLKit", "Ошибка загрузки модели перевода: " + e.getMessage()));

        } else {
            Translator translator = SplashScreen.enToRuTranslator();
            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(unused -> {
                        translator.translate(recipeName)
                                .addOnSuccessListener(translatedText ->
                                        recipeNameTranslate = translatedText);

                        translator.translate(ingredientsData)
                                .addOnSuccessListener(translatedText ->
                                        ingredientDataTranslate = translatedText);

                        translator.translate(recipeData)
                                .addOnSuccessListener(translatedText ->
                                        recipeDataTranslate = translatedText);
                    })
                    .addOnFailureListener(e -> Log.e("MLKit", "Ошибка загрузки модели перевода: " + e.getMessage()));

        }



        //Recipe recipe = new Recipe(recipeId, recipeName, photoFileName, preparationTime, recipeData, ingredientsData, 1);
        Recipe recipe = new Recipe(recipeId, recipeName, recipeNameTranslate, photoFileName, preparationTime, recipeData, recipeDataTranslate, ingredientsData, ingredientDataTranslate, 1);

        databaseHandler.addRecipe(recipe);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Проверка имени рецепта
        if (recipeNameEditText.getText().toString().trim().isEmpty()) {
            recipeNameEditText.setBackgroundResource(R.drawable.error_background_with_border); // Красный контур
            isValid = false;
        } else {
            recipeNameEditText.setBackgroundResource(R.color.background_add_screen); // Сбрасываем ошибку
        }

        // Проверка времени приготовления
        if (preparationTimeEditText.getText().toString().trim().isEmpty()) {
            preparationTimeEditText.setBackgroundResource(R.drawable.error_background_with_border); // Красная линия ввода текста
            isValid = false;
        } else {
            preparationTimeEditText.setBackgroundResource(R.color.background_add_screen); // Сбрасываем ошибку
        }

        // Проверка изображения
        if (selectedBitmap == null) { // Проверка наличия изображения
            btnAddImage.setBackgroundResource(R.drawable.error_underline);
            isValid = false;
        } else {
            btnAddImage.setBackgroundResource(android.R.color.transparent); // Сбрасываем ошибку
        }

        if (ingredientFragment.validateInputs()) {
            ingredientFragment.errorInputs();
            isValid = false;
        } else {
            ingredientFragment.goodInputs();
        }

        if (recipeFragment.validateInputs()) {
            recipeFragment.errorInputs();
            isValid = false;
        } else {
            recipeFragment.goodInputs();
        }

        return isValid;
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
            animatorDown.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
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
