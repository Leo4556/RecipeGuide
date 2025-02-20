package com.example.recipeguide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.recipeguide.R;
import java.util.List;
import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.example.recipeguide.R;

import java.util.ArrayList;
import java.util.List;


public class FavouritesScreen extends AppCompatActivity {


    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    private String[] dishesArr = {"ПЕЛЬМЕНИ С УКРОПОМ  ОБЫКНОВЕННЫЕ", "РИС С ОВОЩАМИ", "ЧАЙ"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites_screen);

        listView = findViewById(R.id.listView);


        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dishesArr);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDish = parent.getItemAtPosition(position).toString();
                Toast.makeText(FavouritesScreen.this, "Вы выбрали: " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                Intent intent;
                switch (selectedDish) {

                    case "ПЕЛЬМЕНИ С УКРОПОМ  ОБЫКНОВЕННЫЕ":
                        intent = new Intent(FavouritesScreen.this, recipe_dumplings_activity.class);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + selectedDish);
                }



                intent.putExtra("nameOfDish", dishesArr[position]);
                startActivity(intent);
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