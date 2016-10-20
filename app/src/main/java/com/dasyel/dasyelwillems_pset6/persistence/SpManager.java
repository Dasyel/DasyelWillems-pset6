package com.dasyel.dasyelwillems_pset6.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import static com.dasyel.dasyelwillems_pset6.models.Contract.*;

/**
 * Simple wrapper around sharedPreferences for cleaner and more readable code
 */
public class SpManager {
    private static final String SP_NAME = "com.dasyel.dasyelwillems_pset6.SP";
    private static final String CURRENT_LIST = "com.dasyel.dasyelwillems_pset6.CL";
    private static final String CURRENT_MOVIE = "com.dasyel.dasyelwillems_pset6.MOVIE";
    private static final String FRIEND = "com.dasyel.dasyelwillems_pset6.FRIEND";
    private static SpManager instance;
    private SharedPreferences sp;

    private SpManager(Context context){
        this.sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static SpManager getInstance(Context context){
        if(instance == null){
            instance = new SpManager(context);
        }
        return instance;
    }

    public String getCurrentList(){
        return sp.getString(CURRENT_LIST, DEFAULT_LIST);
    }

    public String getCurrentMovie(){
        return sp.getString(CURRENT_MOVIE, "");
    }

    public void setCurrentList(String name){
        sp.edit().putString(CURRENT_LIST, name).apply();
    }

    public void setCurrentMovie(String id){
        sp.edit().putString(CURRENT_MOVIE, id).apply();
    }

    public boolean getFriendListOpen(){
        return sp.getBoolean(FRIEND, false);
    }

    public void setFriendListOpen(boolean bool){
        sp.edit().putBoolean(FRIEND, bool).apply();
    }
}
