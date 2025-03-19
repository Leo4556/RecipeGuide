package com.example.recipeguide;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class Recipe_Dumplings_Fragment extends Fragment {



        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";

        // TODO: Rename and change types of parameters
        private String mParam1;
        private String mParam2;

        public Recipe_Dumplings_Fragment() {
            // Required empty public constructor
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReceptFragment.
         */
        // TODO: Rename and change types and number of parameters
        public static Recipe_Dumplings_Fragment newInstance(String param1, String param2) {
            Recipe_Dumplings_Fragment fragment = new Recipe_Dumplings_Fragment();
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
            //return inflater.inflate(R.layout.fragment_recipe__dumplings_, container, false);
            View view = inflater.inflate(R.layout.fragment_recipe__dumplings_, container, false);

            // Получаем данные из аргументов
            Bundle bundle = getArguments();
            if (bundle != null) {

                String recipe = bundle.getString("dish_recipe");

                TextView recipeTextView = view.findViewById(R.id.recipe_dish);

                // Отображаем данные
                recipeTextView.setText(recipe);
            }

            return view;
        }
    }