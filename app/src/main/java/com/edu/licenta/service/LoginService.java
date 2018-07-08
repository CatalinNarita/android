package com.edu.licenta.service;

import android.content.Context;
import android.content.Intent;

import com.edu.licenta.activities.DashboardActivity;
import com.edu.licenta.utils.UserSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by naritc
 * on 10-Apr-18.
 */

public class LoginService {

    public void handleResponse(JSONObject response, Context context, UserSessionManager session, String fn, String ln, String em, String userId, String locale) {
        String accessToken;
        String refreshToken;
        Long expiresIn;

        try {
            accessToken = response.get("access_token").toString();
            refreshToken = response.get("refresh_token").toString();
            expiresIn = Long.parseLong(response.get("expires_in").toString());

            session.createUserLoginSession(userId, fn, ln ,em, accessToken, refreshToken, expiresIn, locale);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("USER ID 1: " + userId);

        Intent i = new Intent(context, DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public void handleResponse(String accessToken, String refreshToken, Long expiresIn, Context context, UserSessionManager session, String fn, String ln, String em, String userId, String locale) {

        session.createUserLoginSession(userId, fn, ln ,em, accessToken, refreshToken, expiresIn, locale);

        Intent i = new Intent(context, DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}
