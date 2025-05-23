package Model;

public class Recipe {

    private String id;
    private String name;
    private String name_en;
    private String image;
    private int cookingTime;
    private String recipe;
    private String recipe_en;
    private String ingredient;
    private String ingredient_en;
    private int isFavorite;

    public Recipe() {
    }
    public Recipe(String id, String name, String image, int cookingTime, String recipe, String ingredient, int isFavorite) {
        this.ingredient = ingredient;
        this.id = id;
        this.name = name;
        this.image = image;
        this.cookingTime = cookingTime;
        this.recipe = recipe;
        this.isFavorite = isFavorite;
    }
    public Recipe(String id, String name,String name_en, String image, int cookingTime, String recipe, String recipe_en, String ingredient, String ingredient_en, int isFavorite) {
        this.id = id;
        this.name = name;
        this.name_en = name_en;
        this.image = image;
        this.cookingTime = cookingTime;
        this.recipe = recipe;
        this.recipe_en = recipe_en;
        this.ingredient = ingredient;
        this.ingredient_en = ingredient_en;
        this.isFavorite = isFavorite;
    }

    public Recipe(String name, String image, int cookingTime, String recipe, String ingredient, int isFavorite) {
        this.name = name;
        this.image = image;
        this.cookingTime = cookingTime;
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.isFavorite = isFavorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public String getRecipe_en() {
        return recipe_en;
    }

    public void setRecipe_en(String recipe_en) {
        this.recipe_en = recipe_en;
    }

    public String getIngredient_en() {
        return ingredient_en;
    }

    public void setIngredient_en(String ingredient_en) {
        this.ingredient_en = ingredient_en;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public int getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }
}
