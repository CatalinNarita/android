package com.edu.licenta.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by naritc
 * on 13-Apr-18.
 */

public class VolleyUtils {

    public static Map<String, String> getBasicAuthHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + Constants.CLIENT_CREDENTIALS_ENCODED);

        return headers;
    }

    public static Map<String, String> getBearerAuthHeaders(String accessToken) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        return headers;
    }

    public static void buildAlertDialog(String title, String content, Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(content);
        alertDialog.setPositiveButton("OK", (DialogInterface dialog, int id) -> dialog.dismiss());
        alertDialog.show();
    }

    public static ProgressDialog buildProgressDialog(String title, String content, Context context) {
        ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setTitle(title);
        pDialog.setMessage(content);
        pDialog.setCancelable(false);
        pDialog.show();

        return pDialog;
    }

}
