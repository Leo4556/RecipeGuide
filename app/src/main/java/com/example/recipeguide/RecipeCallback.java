package com.example.recipeguide;

import java.util.ArrayList;

public interface RecipeCallback {
    void onRecipesLoaded(ArrayList<Dish> dishes);
}
