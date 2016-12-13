package com.fti.sisfo.tours.bookmarks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.fti.sisfo.tours.volley.ListData;
import com.google.gson.Gson;

public class SharedPreference {

    public static final String PREFS_NAME = "Objek_APP";
    public static final String FAVORITES = "Objek_Favorite";

    public SharedPreference() {
        super();
    }

    // This four methods are used for maintaining favorites.
    public void saveFavorites(Context context, List<ListData> favorites) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(FAVORITES, jsonFavorites);

        editor.commit();
    }

    public void addFavorite(Context context, ListData listData) {
        List<ListData> favorites = getFavorites(context);
        if (favorites == null)
            favorites = new ArrayList<ListData>();
        favorites.add(listData);
        saveFavorites(context, favorites);
    }

    public void removeFavorite(Context context, ListData listData) {
        ArrayList<ListData> favorites = getFavorites(context);
        if (favorites != null) {
            favorites.remove(listData);
            saveFavorites(context, favorites);
        }
    }

    public ArrayList<ListData> getFavorites(Context context) {
        SharedPreferences settings;
        List<ListData> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            ListData[] favoriteItems = gson.fromJson(jsonFavorites,
                    ListData[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<ListData>(favorites);
        } else
            return null;

        return (ArrayList<ListData>) favorites;
    }
}