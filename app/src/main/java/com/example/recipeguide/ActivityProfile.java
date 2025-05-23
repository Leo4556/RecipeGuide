package com.example.recipeguide;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import Data.DatabaseHandler;
import Model.Recipe;

public class ActivityProfile extends AppCompatActivity {

    private boolean isEdit, isAvatar = false;
    TextView user_name, my_recipe;
    ListView user_recipes_list;
    ImageView user_avatar;
    ProgressBar uploadProgressBar;
    AppCompatButton logout_button, edit_profile_button;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Uri imageUri;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mAuth = FirebaseAuth.getInstance();

        photoFromGallery();

        uploadProgressBar = findViewById(R.id.uploadProgressBar);
        my_recipe = findViewById(R.id.my_recipe);
        user_recipes_list = findViewById(R.id.user_recipes_list);
        user_name = findViewById(R.id.user_name);
        user_name.setText(User.username);
        user_avatar = findViewById(R.id.user_avatar);

        if(User.userImage != null){
            loadAvatar(User.userImage);
        }else{
            user_avatar.setImageResource(R.drawable.avatar_icon);
        }

        ArrayList<Dish> dishes = null;
        SharedPreferences prefs = getSharedPreferences("MODE", MODE_PRIVATE);
        String dishJson = prefs.getString("dish_list", null);
        Gson gson = new Gson();


        if (dishJson != null) {

            dishes = gson.fromJson(dishJson, new TypeToken<ArrayList<Dish>>(){}.getType());
            Log.d("ActivityProfile", "Загружено рецептов: " + dishes.size());
        }else{
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("dish_list")) {
                dishes = (ArrayList<Dish>) getIntent().getSerializableExtra("dish_list");
                Log.d("ActivityProfile", "Загружено рецептов из Intent: " + dishes.size());

                // 📌 Сохраняем `dishes` в `SharedPreferences` для последующих запусков
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("dish_list", gson.toJson(dishes));
                editor.apply();
            } else {
                Log.w("ActivityProfile", "Нет данных для загрузки, `dishes` остаётся `null`");
            }
        }

        DishAdapter adapter = new DishAdapter(this, dishes); // Создаём адаптер

        user_recipes_list.setAdapter(adapter); // Устанавливаем адаптер

        user_recipes_list.setOnItemClickListener((parent, view, position, id) -> {
            // Получаем выбранное блюдо
            Dish selectedDish = adapter.getItem(position);

            if (selectedDish != null) {
                // Создаём Intent и передаём ID блюда
                Intent intent = new Intent(getApplicationContext(), recipe_example_activity.class);
                intent.putExtra("dish_id", selectedDish.getId()); // Передаём ID блюда
                startActivity(intent);
            }
        });

        logout_button = findViewById(R.id.logout_button);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ActivityProfile.this, MainScreen.class);
                startActivity(intent);
                finish();
            }
        });
        edit_profile_button = findViewById(R.id.edit_profile_button);

    }

    private ArrayList<Dish> getMyRecipe() {
        FirebaseUser user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users").child(user.getUid()).child("my_recipes");

        ArrayList<Dish> dishList = new ArrayList<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Dish dish = new Dish();

                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    dish.setId(recipeSnapshot.getKey());
                    dish.setRecipeName(recipeSnapshot.child("name").getValue(String.class));
                    dish.setRecipeNameEn(recipeSnapshot.child("name_en").getValue(String.class));
                    dish.setRecipeImage(recipeSnapshot.child("image").getValue(String.class));
                    dish.setRecipeCookingTime(recipeSnapshot.child("cookingTime").getValue(Integer.class));

                    dishList.add(dish);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Ошибка загрузки данных: " + error.getMessage());
            }
        });

        return dishList;
    }

    public void editProfile(View view) {
        if(!isEdit){
            isEdit = true;
            user_avatar.setEnabled(true);
            if(User.userImage == null){
                user_avatar.setImageResource(R.drawable.edit_avatar);
            }
            user_avatar.setOnClickListener(v -> openImageChooser());

            my_recipe.setVisibility(View.INVISIBLE);
            user_recipes_list.setVisibility(View.INVISIBLE);
            edit_profile_button.setText("Отмена");
            edit_profile_button.setTextColor(Color.RED);
            logout_button.setText("Сохранить");
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textColor, typedValue, true);
            int color = typedValue.data;
            logout_button.setTextColor(color);
            logout_button.setOnClickListener(v -> saveProfile());

            user_name.setBackgroundResource(R.drawable.edit_text_border); // Добавляем нижнюю линию
            user_name.setCursorVisible(true);
            user_name.setFocusable(true);
            user_name.setFocusableInTouchMode(true);
            user_name.requestFocus();




        }else {
            isEdit = false;
            user_avatar.setEnabled(false);
            if(User.userImage != null){
                loadAvatar(User.userImage);
            }else{
                user_avatar.setImageResource(R.drawable.avatar_icon);
            }
            my_recipe.setVisibility(View.VISIBLE);
            user_recipes_list.setVisibility(View.VISIBLE);
            edit_profile_button.setText("Редактировать профиль");
            logout_button.setTextColor(Color.RED);
            logout_button.setText("Выйти");
            logout_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(ActivityProfile.this, MainScreen.class);
                    startActivity(intent);
                    finish();
                }
            });
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textColor, typedValue, true);
            int color = typedValue.data;
            edit_profile_button.setTextColor(color);

            user_name.setText(User.username);
            user_name.setBackground(null); // Убираем нижнюю линию
            user_name.setCursorVisible(false);
            user_name.setFocusable(false);
            user_name.setFocusableInTouchMode(false);
        }
    }
    private void photoFromGallery() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        imageUri = result.getData().getData(); // Получаем URI выбранного изображения
                        Glide.with(this).load(imageUri).circleCrop().into(user_avatar);
                        isAvatar = true;
                    }
                }
        );

    }


    private void saveProfile() {

        if(!String.valueOf(user_name.getText()).equals(User.username)){
            saveUsernameToDatabase();
        }
        if(imageUri != null){
            saveCloudinary();
        }else {
            Intent intent = new Intent(ActivityProfile.this, ActivityProfile.class);
            startActivity(intent);
            finish();
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        myRef.child(user.getUid()).child("imageUrl").setValue(imageUrl.toString());

        User.userImage = imageUrl;
        editor.putString("userImage", User.userImage);
        editor.apply();
    }

    private void saveUsernameToDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        myRef.child(user.getUid()).child("username").setValue(user_name.getText().toString());

        User.username = user_name.getText().toString().trim();
        editor.putString("username", User.username);
        editor.apply();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Выберите изображение"));
    }


    private void saveCloudinary(){
        if(isAvatar){
            MediaManager.get().upload(imageUri).option("folder", "avatar_icon").callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    uploadProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {

                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    uploadProgressBar.setVisibility(View.GONE);
                    String imageUrl = resultData.get("secure_url").toString(); // Ссылка на картинку
                    saveImageUrlToDatabase(imageUrl); // Сохранение в Firebase
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(ActivityProfile.this, ActivityProfile.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 500);

                }

                @Override
                public void onError(String requestId, ErrorInfo error) {

                    uploadProgressBar.setVisibility(View.GONE);
                    Log.e("Cloudinary", "Ошибка загрузки: " + error.getDescription());
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {

                }
            }).dispatch();
        }
    }

    private void loadAvatar(String url) {
        Glide.with(this)
                .load(url).circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Загружаем из кеша, если интернета нет
                .into(user_avatar);
    }
}