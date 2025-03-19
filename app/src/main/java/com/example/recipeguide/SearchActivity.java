package com.example.recipeguide;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import Data.DatabaseHandler;
import Model.Recipe;
import Utils.Util;


public class SearchActivity extends AppCompatActivity {

    SearchView searchView;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = findViewById(R.id.listView);
        searchView = findViewById(R.id.search_field);
        searchView.setQueryHint("Поиск...");

        DatabaseHandler databaseHelper = new DatabaseHandler(this);
        ArrayList<Dish> dishes = databaseHelper.getAllRecipe();
        DishAdapter adapter = new DishAdapter(this, dishes); // Создаём адаптер

        listView.setAdapter(adapter); // Устанавливаем адаптер

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Получаем выбранное блюдо
            Dish selectedDish = adapter.getItem(position);

            if (selectedDish != null) {
                // Создаём Intent и передаём ID блюда
                Intent intent = new Intent(getApplicationContext(), recipe_dumplings_activity.class);
                intent.putExtra("dish_id", selectedDish.getId()); // Передаём ID блюда
                startActivity(intent);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
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

}