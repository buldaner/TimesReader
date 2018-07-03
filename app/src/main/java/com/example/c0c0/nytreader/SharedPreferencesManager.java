package com.example.c0c0.nytreader;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static SharedPreferencesManager mInstance;
    private static Context mCtx;

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    public static SharedPreferencesManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPreferencesManager(context);
        }
        return mInstance;
    }

    private SharedPreferencesManager(Context context) {
        mCtx = context;
        mPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mEditor = mPref.edit();
    }

    public void putString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public String getString(String key) {
        return mPref.getString(key, "");
    }

    public void putInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public int getInt(String key) {
        return mPref.getInt(key, 0);
    }
}
