package Model;

public class Recipe {

    private int id;
    private String name;
    private String image;
    private int cookingTime;
    private String recipe;
    private String ingredient;
    private int isFavorite;

    public Recipe() {
    }
    public Recipe(int id, String name, String image, int cookingTime, String recipe, String ingredient, int isFavorite) {
        this.ingredient = ingredient;
        this.id = id;
        this.name = name;
        this.image = image;
        this.cookingTime = cookingTime;
        this.recipe = recipe;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
