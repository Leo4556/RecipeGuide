package com.example.recipeguide;

public class Dish {
    private int id;
    private String recipeName;
    private String recipeImage;
    private int recipeCookingTime;

    public Dish() {
    }

    public Dish(int id, String recipeName, String recipeImage, int recipeCookingTime) {
        this.id = id;
        this.recipeName = recipeName;
        this.recipeImage = recipeImage;
        this.recipeCookingTime = recipeCookingTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public void setRecipeCookingTime(int recipeCookingTime) {
        this.recipeCookingTime = recipeCookingTime;
    }

    public int getId() {
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
