package com.example.recipeguide;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import Data.DatabaseHandler;

public class MainScreen extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listView);

        DatabaseHandler databaseHelper = new DatabaseHandler(this);
        ArrayList<Dish> dishes = databaseHelper.getRecommendedRecipe(this);
        DishAdapter adapter = new DishAdapter(this, dishes); // Создаём адаптер

        listView.setAdapter(adapter); // Устанавливаем адаптер

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

    public void goHome(View view){
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    public void goAddScreen(View view){
        Intent intent = new Intent(this, AddScreen.class);
        startActivity(intent);
    }

    public void goFavourites(View view){
        Intent intent = new Intent(this, FavouritesScreen.class);
        startActivity(intent);
    }

    public void goToSearch(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void goOptions(View view){
        Intent intent = new Intent(this, OptionsScreen.class);
        startActivity(intent);
    }



}

//TODO Сделать рандомные рекомендации на MainScreen
//TODO Изменения AddScreen: загрузка данных в бд, добавление фото, увеличить framelayout,
// кнопку "сохранить" сделать как Button
//TODO Реализовать Настройки
