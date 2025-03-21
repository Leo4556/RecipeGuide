package com.example.recipeguide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.recipeguide.Dish;
import com.example.recipeguide.R;

import java.io.File;
import java.util.ArrayList;

public class DishAdapter extends ArrayAdapter<Dish> {
    private ArrayList<Dish> originalDishes; // Исходный список блюд
    private ArrayList<Dish> filteredDishes; // Отфильтрованный список
    private Filter dishFilter; // Фильтр для поиска
    public DishAdapter(Context context, ArrayList<Dish> dishes) {
        super(context, 0, dishes);
        this.originalDishes = new ArrayList<>(dishes); // Сохраняем оригинальный список
        this.filteredDishes = new ArrayList<>(dishes); // Создаём копию для фильтрации
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Dish dish = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_search_list, parent, false);
        }

        // Находим элементы интерфейса
        ImageView dishImageView = convertView.findViewById(R.id.recipe_image);
        TextView dishNameTextView = convertView.findViewById(R.id.recipe_name);
        TextView dishCookingTimeTextView = convertView.findViewById(R.id.recipe_cooking_time);

        // Устанавливаем данные
        if (dish != null) {
            dishNameTextView.setText(dish.getRecipeName());

            String imagePath = dish.getRecipeImage();
            if (imagePath != null) {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    dishImageView.setImageBitmap(bitmap); // Устанавливаем изображение в ImageView
                }
            } else {
                // Устанавливаем изображение-заглушку, если данных нет
                dishImageView.setImageResource(R.drawable.stub);
            }

            dishCookingTimeTextView.setText("Время приготовления:\n" + dish.getRecipeCookingTime() + " мин");
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return filteredDishes.size();
    }

    @Nullable
    @Override
    public Dish getItem(int position) {
        return filteredDishes.get(position);
    }

    @Override
    public Filter getFilter() {
        if (dishFilter == null) {
            dishFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    if (constraint == null || constraint.length() == 0) {
                        // Если ничего не введено, возвращаем весь список
                        results.values = originalDishes;
                        results.count = originalDishes.size();
                    } else {
                        String filterString = constraint.toString().toLowerCase().trim();

                        // Отфильтровываем по названию блюда
                        ArrayList<Dish> filteredList = new ArrayList<>();
                        for (Dish dish : originalDishes) {
                            if (dish.getRecipeName().toLowerCase().contains(filterString)) {
                                filteredList.add(dish);
                            }
                        }

                        results.values = filteredList;
                        results.count = filteredList.size();
                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredDishes.clear();
                    if (results.values != null) {
                        filteredDishes.addAll((ArrayList<Dish>) results.values);
                    }
                    notifyDataSetChanged(); // Обновляем ListView
                }
            };
        }
        return dishFilter;
    }
}
