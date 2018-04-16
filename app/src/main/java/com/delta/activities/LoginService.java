package com.delta.activities;

import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by naritc on 10-Apr-18.
 */

public class LoginService {

    public void handleResponse(JSONObject response, Context context, UserSessionManager session, String fn, String ln, String em) {

        String accessToken;
        String refreshToken;
        Long expiresIn;

        try {

            accessToken = response.get("access_token").toString();
            refreshToken = response.get("refresh_token").toString();
            expiresIn = Long.parseLong(response.get("expires_in").toString());

            session.createUserLoginSession(fn, ln ,em, accessToken, refreshToken, expiresIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(context, DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public void handleResponse(String accessToken, String refreshToken, Long expiresIn, Context context, UserSessionManager session, String fn, String ln, String em) {

        session.createUserLoginSession(fn, ln ,em, accessToken, refreshToken, expiresIn);

        Intent i = new Intent(context, DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}
