package com.edu.licenta.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to create custom request headers, progress dialogs etc.
 * @author Catalin-Ioan Narita
 */

public class VolleyUtils {

    /**
     * Builds a map of http headers for application/json Content-Type and basic Base64 authorization
     * @return the built headers map
     */
    public static Map<String, String> getBasicAuthHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + Constants.CLIENT_CREDENTIALS_ENCODED);

        return headers;
    }

    /**
     * Builds a map of http headers for application/json Content-Type and oauth2
     * bearer token authorization
     * @param accessToken the access token provided by the server following authentication
     * @return the built headers map
     */
    public static Map<String, String> getBearerAuthHeaders(String accessToken) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        return headers;
    }

    /**
     * Builds an alert dialog with dynamic title and text body
     * @param title the dialog's title
     * @param content the dialog's content
     * @param context the current context of the application
     */
    public static void buildAlertDialog(String title, String content, Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(content);
        alertDialog.setPositiveButton("OK", (DialogInterface dialog, int id) -> dialog.dismiss());
        alertDialog.show();
    }

    /**
     * Builds a progress dialog with dynamic title and text body
     * @param title the dialog's title
     * @param content the dialog's content
     * @param context the current context of the application
     * @return the built progress dialog to have access to it's other methods (i.e hide(), dismiss() etc.)
     */
    public static ProgressDialog buildProgressDialog(String title, String content, Context context) {
        ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setTitle(title);
        pDialog.setMessage(content);
        pDialog.setCancelable(false);
        pDialog.show();

        return pDialog;
    }

}
