package com.edu.licenta.utils;

import android.util.Base64;

/**
 * Class that stores application's credentials, request URLs, regexps etc.
 * @author Catalin-Ioan Narita
 */
public class Constants {

    //server requests
    public static final String BASE_URL = "https://ancient-wildwood-65338.herokuapp.com";
    public static final String BASE_SECURE_URL = "https://ancient-wildwood-65338.herokuapp.com/secure";
    private static final String CLIENT_CREDENTIALS = "android-oauth2-client-id:android-oauth2-client-pass"; //YW5kcm9pZC1vYXV0aDItY2xpZW50LWlkOmFuZHJvaWQtb2F1dGgyLWNsaWVudC1wYXNz
    public static final String REQUEST_TOKEN_URL = BASE_URL + "/oauth/token?grant_type=password&username=%s&password=%s";
    static final String CLIENT_CREDENTIALS_ENCODED = Base64.encodeToString(CLIENT_CREDENTIALS.getBytes(), Base64.NO_WRAP);
    public static final String REQUEST_NEW_TOKEN = BASE_URL + "/oauth/token?grant_type=refresh_token&refresh_token=%s";
    public static final String REGISTER_USER_URL = BASE_URL + "/user/add";
    public static final String GET_ALL_GALLERIES_URL = BASE_SECURE_URL + "/gallery/get/all/";

    //error messages
    public static final String NO_CONNECTION = "You are not connected to the internet!";
    public static final String WRONG_CREDENTIALS = "Wrong username or password!";
    public static final String SERVER_DOWN = "Could not connect. Please try again later";
    public static final String ERROR_TITLE = "Error";
    public static final String USERNAME_IN_USE = "This username is already in use!";
    public static final String EMAIL_IN_USE = "This email is already in use!";

    //user calls
    public static final String CHECK_USER = BASE_URL + "/user/checkUser";

    //email regex
    public static final String EMAIL_REGEXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    //artifacts requests
    public static final String GET_USER_DISCOVERED_ARTIFACTS = BASE_SECURE_URL + "/artifact/userDiscovered/%s/%s/%s";
    public static final String ADD_DISCOVERED_ARTIFACT = BASE_SECURE_URL + "/discoveredArtifact/add/%s/%s";

    //gallery review
    public static final String ADD_GALLERY_REVIEW = BASE_SECURE_URL + "/gallery/addReview/%s/%s";

    //artifact review
    public static final String ADD_ARTIFACT_REVIEW = BASE_SECURE_URL + "/artifact/addReview/%s/%s";
    public static final String GET_ALL_ARTIFACT_REVIEWS_URL = BASE_SECURE_URL + "/artifact/getReview/%s";

    //text-to-speech API url
    public static final String TTS_URL = "https://texttospeech.googleapis.com/v1beta1/text:synthesize";

    //gallery reviews
    public static final String GET_ALL_GALLERY_REVIEWS_URL = BASE_SECURE_URL + "/gallery/getReview/%s";

    public static float lastX = 0;
    public static float lastY = 0;

    //artifacts positions
    public static float artifact1PosX = 90;
    public static float artifact1PosY = 1630;

    public static float artifact2PosX = 900;
    public static float artifact2PosY = 1630;

    public static float artifact21PosX = 90;
    public static float artifact21PosY = 880;

    public static float artifact22PosX = 900;
    public static float artifact22PosY = 880;

    public static float artifact41PosX = 90;
    public static float artifact41PosY = 130;

    public static float artifact42PosX = 900;
    public static float artifact42PosY = 130;

    //user position
    public static float userPositionX = 0;
    public static float userPositionY = 0;

    public static boolean firstEnter = true;
}
