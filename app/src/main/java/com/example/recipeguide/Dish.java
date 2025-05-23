package com.example.recipeguide;

import java.io.Serializable;

public class Dish implements Serializable {
    private String id;
    private String recipeName;
    private String recipeNameEn;
    private String recipeImage;
    private int recipeCookingTime;

    public Dish() {
    }

    public Dish(String id, String recipeName, String recipeNameEn, String recipeImage, int recipeCookingTime) {
        this.id = id;
        this.recipeName = recipeName;
        this.recipeNameEn = recipeNameEn;
        this.recipeImage = recipeImage;
        this.recipeCookingTime = recipeCookingTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public String getRecipeNameEn() {
        return recipeNameEn;
    }

    public void setRecipeNameEn(String recipeNameEn) {
        this.recipeNameEn = recipeNameEn;
    }

    public void setRecipeCookingTime(int recipeCookingTime) {
        this.recipeCookingTime = recipeCookingTime;
    }

    public String getId() {
        return id;
    }
    public String getRecipeName() {
        return recipeName;
    }

    public String getRecipeImage() {
        return recipeImage;
    }

    public int getRecipeCookingTime() {
        return recipeCookingTime;
    }
}
