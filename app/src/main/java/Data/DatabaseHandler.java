package Data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


import android.util.Log;

import androidx.annotation.Nullable;

import com.example.recipeguide.Dish;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import Model.Recipe;
import Utils.Util;

public class DatabaseHandler extends SQLiteAssetHelper {

    private static final String TAG = "TAG";
    private Context myContext;
    private SQLiteDatabase myDataBase;
    public DatabaseHandler( Context context) {
        super(context, Util.DATABASE_NAME, null, Util.DATABASE_VERSION);
        this.myContext=context;
        try {
            myDataBase = this.getWritableDatabase();
        } catch (SQLiteException e) {
            try {
                clearDB();
                myDataBase.close();
            } catch (Exception ex) {
                setForcedUpgrade();
            }

            Log.d(TAG, "MyDatabase: " + e);
        }
        copyAllImagesFromAssets(context);

    }

    public Cursor getAllData() {
        myDataBase = getReadableDatabase();
        return myDataBase.query(Util.TABLE_NAME, null, null, null,
                null, null, null);
    }

    //Добавить рецепт
    public void addRecipe (Recipe recipe){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Util.KEY_NAME, recipe.getName());
        contentValues.put(Util.KEY_IMAGE, recipe.getImage());
        contentValues.put(Util.KEY_COOKINGTIME, recipe.getCookingTime());
        contentValues.put(Util.KEY_RECIPE, recipe.getRecipe());
        contentValues.put(Util.KEY_INGREDIENT, recipe.getIngredient());
        contentValues.put(Util.KEY_ISFAVORITE, recipe.getIsFavorite());

        db.insert(Util.TABLE_NAME, null, contentValues);
        db.close();
    }

    //Найти рецерт по id(можно поменять вместо id другую переменную)
    public Recipe getRecipe(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Util.TABLE_NAME, new String[] {Util.KEY_ID, Util.KEY_NAME, Util.KEY_IMAGE,
                Util.KEY_COOKINGTIME,Util.KEY_RECIPE,Util.KEY_INGREDIENT, Util.KEY_ISFAVORITE},Util.KEY_ID + "=?",
                new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor != null){
            cursor.moveToFirst();
        }
        Recipe recipe = new Recipe(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),  cursor.getString(2), Integer.parseInt(cursor.getString(3)),
                cursor.getString(4),  cursor.getString(5), Integer.parseInt(cursor.getString(6)));
        return recipe;
    }

    //Возвращает все рецепты
    public ArrayList<Dish> getAllRecipe(){
        try {
            myDataBase = getReadableDatabase();
            if (myDataBase != null) {
                Log.d("DB_DEBUG", "Database opened successfully");
            } else {
                Log.e("DB_ERROR", "Database is null");
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error opening database: " + e.getMessage());
        }
        ArrayList<Dish> dishList = new ArrayList<>();
        String selectAllRecipe = "SELECT " + Util.KEY_ID + ", " + Util.KEY_NAME + ", " + Util.KEY_IMAGE + ", " + Util.KEY_COOKINGTIME + " FROM " + Util.TABLE_NAME;
        Cursor cursor = myDataBase.rawQuery(selectAllRecipe, null);
        if (cursor.moveToFirst()){
            do{
                Dish dish = new Dish();
                dish.setId(Integer.parseInt(cursor.getString(0)));
                dish.setRecipeName(cursor.getString(1));
                dish.setRecipeImage(cursor.getString(2));
                dish.setRecipeCookingTime(Integer.parseInt(cursor.getString(3)));

                dishList.add(dish);
            }while (cursor.moveToNext());
        }
        return dishList;
    }

    public ArrayList<Dish> getFavoriteRecipe(){
        try {
            myDataBase = getReadableDatabase();
            if (myDataBase != null) {
                Log.d("DB_DEBUG", "Database opened successfully");
            } else {
                Log.e("DB_ERROR", "Database is null");
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error opening database: " + e.getMessage());
        }
        ArrayList<Dish> dishList = new ArrayList<>();
        String selectFavoriteRecipe = "SELECT " + Util.KEY_ID + ", " + Util.KEY_NAME + ", " + Util.KEY_IMAGE + ", " + Util.KEY_COOKINGTIME + ", " + Util.KEY_ISFAVORITE + " FROM " + Util.TABLE_NAME + " WHERE " + Util.KEY_ISFAVORITE + " = 1";
        Cursor cursor = myDataBase.rawQuery(selectFavoriteRecipe, null);
        if (cursor.moveToFirst()){
            do{
                Dish dish = new Dish();
                dish.setId(Integer.parseInt(cursor.getString(0)));
                dish.setRecipeName(cursor.getString(1));
                dish.setRecipeImage(cursor.getString(2));
                dish.setRecipeCookingTime(Integer.parseInt(cursor.getString(3)));

                dishList.add(dish);
            }while (cursor.moveToNext());
        }
        return dishList;
    }

    //Обновляет конкретный рецепт
    public int updateRecipe(Recipe recipe){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Util.KEY_NAME, recipe.getName());
        contentValues.put(Util.KEY_IMAGE, recipe.getImage());
        contentValues.put(Util.KEY_COOKINGTIME, recipe.getCookingTime());
        contentValues.put(Util.KEY_RECIPE, recipe.getRecipe());
        contentValues.put(Util.KEY_INGREDIENT, recipe.getIngredient());
        contentValues.put(Util.KEY_ISFAVORITE, recipe.getIsFavorite());

        return db.update(Util.TABLE_NAME, contentValues, Util.KEY_ID + "=?", new String[]{String.valueOf(recipe.getId())});
    }

    //Удаляет конкретный рецепт
    public void deleteRecipe (Recipe recipe){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(Util.TABLE_NAME, Util.KEY_ID + "=?", new String[]{String.valueOf(recipe.getId())});
        db.close();
    }

    public void copyAllImagesFromAssets(Context context) {
        AssetManager assetManager = context.getAssets();

        try {
            // Получаем список всех файлов в папке assets
            String[] files = assetManager.list("image");
            if (files != null) {
                for (String fileName : files) {
                    // Проверяем, является ли файл изображением (например, по расширению)
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
                        // Копируем файл
                        copyFileFromAssets(context, fileName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFileFromAssets(Context context, String fileName) {
        // Путь, куда будет скопирован файл
        String assetFilePath = "image/" + fileName;
        File outFile = new File(context.getFilesDir(), fileName);

        if (!outFile.exists()) {
            try (InputStream inputStream = context.getAssets().open(assetFilePath);
                 FileOutputStream outputStream = new FileOutputStream(outFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();
                System.out.println("Файл скопирован: " + outFile.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
    public void clearDB() {
        if (myDataBase != null && myDataBase.isOpen()) {
            myDataBase.close();
        }
        File file = new File(myContext.getDatabasePath(Util.DATABASE_NAME).getPath());
        SQLiteDatabase.deleteDatabase(file);
    }

}
