package com.example.recipeguide;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;

import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;

import android.net.Uri;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import Data.DatabaseHandler;
import Model.Recipe;

public class recipe_example_activity extends AppCompatActivity {


    private Ingredient_Fragment_Example ingredientFragment = new Ingredient_Fragment_Example();
    private Recipe_Fragment_Example receptFragment = new Recipe_Fragment_Example();
    TypedValue typedValue = new TypedValue();
    private boolean isFavorite = false;
    private Recipe selectedDish;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recipe_example);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBackButtonHandler();
        ImageButton saveFavoritesButton = findViewById(R.id.button_save_favorites);
        int dishId = getIntent().getIntExtra("dish_id", -1); // -1 — значение по умолчанию
        DatabaseHandler databaseHelper = new DatabaseHandler(this);

        saveFavoritesButton.setOnClickListener(view -> toggleFavoriteButton(dishId, saveFavoritesButton, databaseHelper));
        loadData(dishId, databaseHelper);

        TextView nameDish = findViewById(R.id.name_dish);
        nameDish.setMovementMethod(new ScrollingMovementMethod());
        nameDish.post(() -> scrollingText(nameDish));

        Button ingredientButton = findViewById(R.id.ingredient);
        Button recipeButton = findViewById(R.id.recipe);

        setNewFragment(ingredientFragment);
        ingredientButton.setBackgroundResource(R.drawable.rounded_button_focused);

        Recipe selectedDish = databaseHelper.getRecipe(dishId);
        ImageButton buttonShare = findViewById(R.id.button_share);
        buttonShare.setOnClickListener(v -> {
            try {
                createPdf(selectedDish);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });


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

    }


    private void loadData(int dishId, DatabaseHandler databaseHelper) {
        if (dishId != -1) {
            // Здесь можно использовать dishId для загрузки данных из базы

            selectedDish = databaseHelper.getRecipe(dishId);

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
                //dishCookingTime.setText("Время приготовления: " + selectedDish.getCookingTime() + " мин");
                String cookingTimeText = getString(R.string.cooking_time, selectedDish.getCookingTime());
                dishCookingTime.setText(cookingTimeText);
                if (selectedDish.getIsFavorite() == 0) {
                    getTheme().resolveAttribute(R.attr.buttonHeartIcon, typedValue, true);
                    dishFavorite.setImageResource(typedValue.resourceId);
                } else {
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

    private void scrollingText(TextView nameDish) {
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

    private void updateFavorite(int dishId, DatabaseHandler databaseHandler, int isFavorite) {
        if (dishId != -1) {
            Recipe selectedDish = databaseHandler.getRecipe(dishId);
            if (selectedDish != null) {
                selectedDish.setIsFavorite(isFavorite);
                databaseHandler.updateRecipe(selectedDish);
            }
        }
    }

    private void createPdf(Recipe selectedDish) throws FileNotFoundException {
        try {
            // Создаём временный PDF-файл
            File tempFile = new File(getCacheDir(), selectedDish.getName() + ".pdf");
            FileOutputStream fos = new FileOutputStream(tempFile);

            // Создаём документ iText
            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            String fontPath = "assets/times_new_roman.ttf"; // Если шрифт в папке assets
            PdfFont font = PdfFontFactory.createFont(fontPath);

            // Устанавливаем размер страницы и отступы
            pdfDocument.setDefaultPageSize(PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            // Заголовок
            document.add(new Paragraph(selectedDish.getName())
                    .setFont(font)
                    .setFontSize(25)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            String imagePath = selectedDish.getImage();
            if (imagePath != null) {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    // Конвертируем файл изображения в объект Bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    // Конвертируем Bitmap в байты
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bitmapData = stream.toByteArray();

                    // Создаём объект Image из байтов
                    ImageData imageData = ImageDataFactory.create(bitmapData);
                    Image image = new Image(imageData);

                    image.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    // Настраиваем размер изображения (опционально)
                    image.setWidth(300); // Устанавливаем ширину в пикселях
                    image.setHeight(200); // Устанавливаем высоту в пикселях

                    // Добавляем изображение в PDF
                    document.add(image);
                }
            } else {
                // Если изображения нет, добавляем заглушку
                document.add(new Paragraph("Изображение недоступно."));
            }
            // Время приготовления
            document.add(new Paragraph("Время приготовления: " + selectedDish.getCookingTime() + " минут")
                    .setFont(font)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            // Ингредиенты
            document.add(new Paragraph("Ингредиенты:")
                    .setFont(font)
                    .setFontSize(20)
                    .setBold());
            document.add(new Paragraph(selectedDish.getIngredient()).setFontSize(20).setFont(font));

            // Рецепт
            document.add(new Paragraph("\nРецепт:")
                    .setFont(font)
                    .setFontSize(20)
                    .setBold());
            document.add(new Paragraph(selectedDish.getRecipe()).setFontSize(20).setFont(font));

            // Закрываем документ
            document.close();

            // Передаём файл для отправки
            sharePdf(tempFile);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при создании PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdf(File pdfFile) {
        Uri uri = FileProvider.getUriForFile(this, "com.example.recipeguide.fileprovider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Share PDF via"));
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