package com.example.recipeguide;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class Ingredient_Fragment_Example extends Fragment {



        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";

        // TODO: Rename and change types of parameters
        private String mParam1;
        private String mParam2;

        public Ingredient_Fragment_Example() {
            // Required empty public constructor
        }

        public static Ingredient_Fragment_Example newInstance(String param1, String param2) {
            Ingredient_Fragment_Example fragment = new Ingredient_Fragment_Example();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mParam1 = getArguments().getString(ARG_PARAM1);
                mParam2 = getArguments().getString(ARG_PARAM2);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            //return inflater.inflate(R.layout.fragment_ingredient__dumplings_, container, false);
            View view = inflater.inflate(R.layout.fragment_ingredient_example, container, false);

            // Получаем данные из аргументов
            Bundle bundle = getArguments();
            if (bundle != null) {

                String ingredients = bundle.getString("dish_ingredients");

                TextView ingredientsTextView = view.findViewById(R.id.ingredients_dish);

                // Отображаем данные
                ingredientsTextView.setText(ingredients);
            }

            return view;
        }
    }
