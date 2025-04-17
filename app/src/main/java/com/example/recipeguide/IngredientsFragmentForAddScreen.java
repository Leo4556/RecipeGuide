package com.example.recipeguide;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class IngredientsFragmentForAddScreen extends Fragment {

    private EditText editTextIngredients;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ingredients_input, container, false);

        editTextIngredients = view.findViewById(R.id.ingredients);
        return view;}

        public boolean validateInputs() {
            String ingredients = editTextIngredients.getText().toString().trim();
            return !ingredients.isEmpty();
        }

        public String getIngredientsData() {
            return editTextIngredients.getText().toString().trim();
        }


    }
