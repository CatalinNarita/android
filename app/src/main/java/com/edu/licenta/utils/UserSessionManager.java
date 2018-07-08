package com.edu.licenta.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.edu.licenta.activities.LoginActivity;

import java.util.HashMap;

/**
 * Class used to store user information. It uses Android's SharedPreferences and
 * SharedPreferences.Editor interfaces to store and manage user data
 * @author Catalin-Ioan Narita
 */

public class UserSessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;
    private static final String PREFER_NAME = "AndroidExamplePref";
    private static final String IS_USER_LOGGED_IN = "IsUserLoggedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ACCESS_TOKEN_EXPIRE_TIME = "expires_in";
    public static final String KEY_CURRENT_LANG = "current_lang";

    public UserSessionManager(Context context){
        // Shared pref mode
        int PRIVATE_MODE = 0;

        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    /**
     * Creates a new login session storing the provided user data
     * @param userId the user's id
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email
     * @param accessToken the user's access token
     * @param refreshToken the user's refresh token
     * @param expiresIn access token's remaining duration (in seconds)
     */
    public void createUserLoginSession(String userId, String firstName, String lastName, String email, String accessToken, String refreshToken, Long expiresIn, String locale){
        editor.putBoolean(IS_USER_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_CURRENT_LANG, locale);
        editor.putLong(KEY_ACCESS_TOKEN_EXPIRE_TIME, System.currentTimeMillis() / 1000L + expiresIn);

        editor.commit();
    }

    public void changeLanguage(String locale) {
        editor.putString(KEY_CURRENT_LANG, locale);

        editor.commit();
    }

    /**
     * Checks whether the user is logged in or not
     * @return if the user is not logged in the method will start a new LoginActivity,
     * else, custom handling can be made based on this (i.e redirect to Dashboard, Start etc.)
     */
    public boolean checkLogin(){
        // Check login status
        if(!this.isUserLoggedIn()){

            Intent i = new Intent(_context, LoginActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);

            return true;
        }
        return false;
    }


    /**
     * Builds a hash map with the user's data
     * @return the built hash map
     */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();

        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));
        user.put(KEY_FIRST_NAME, pref.getString(KEY_FIRST_NAME, null));
        user.put(KEY_LAST_NAME, pref.getString(KEY_LAST_NAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_ACCESS_TOKEN, pref.getString(KEY_ACCESS_TOKEN, null));
        user.put(KEY_REFRESH_TOKEN, pref.getString(KEY_REFRESH_TOKEN, null));
        user.put(KEY_CURRENT_LANG, pref.getString(KEY_CURRENT_LANG, null));

        return user;
    }

    /**
     * Clears all user stored data and starts a new LoginActivity
     */
    public void logoutUser(){

        // Clearing all user data from Shared Preferences
        String currentLanguage = pref.getString(KEY_CURRENT_LANG, null);
        editor.clear();
        editor.putString(KEY_CURRENT_LANG, currentLanguage);
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }


    /**
     * Checks whether the user logged in flag is set or not
     * @return flag's value
     */
    private boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGGED_IN, false);
    }

    /**
     * Checks whether the user's token has expired or not
     * @return true if token expired, false otherwise
     */
    public boolean hasTokenExpired() {
        long expireTime = pref.getLong(KEY_ACCESS_TOKEN_EXPIRE_TIME, 0L);
        long systemTime = System.currentTimeMillis() / 1000L;

        return expireTime != 0 && systemTime > expireTime;
    }

}
