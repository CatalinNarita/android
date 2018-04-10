package com.delta.activities;

import android.util.Base64;

/**
 * Created by naritc on 29-Mar-18.
 */
public class Constants {
    public static final String BASE_URL = "https://ancient-wildwood-65338.herokuapp.com";
    public static final String BASE_SECURE_URL = "https://ancient-wildwood-65338.herokuapp.com/secure";
    public static final String CLIENT_CREDENTIALS = "android-oauth2-client-id:android-oauth2-client-pass";
    public static final String REQUEST_TOKEN_URL = BASE_URL + "/oauth/token?grant_type=password&username=%s&password=%s";
    public static final String CLIENT_CREDENTIALS_ENCODED = Base64.encodeToString(CLIENT_CREDENTIALS.getBytes(), Base64.NO_WRAP);
    public static final String REQUEST_NEW_TOKEN = BASE_URL + "/oauth/token?grant_type=refresh_token&refresh_token=%s";
    public static final String REGISTER_USER_URL = BASE_URL + "/user/add";

}
