package com.delta.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by naritc on 13-Apr-18.
 */

public class VolleyUtils {

    public static Map<String, String> getBasicAuthHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + Constants.CLIENT_CREDENTIALS_ENCODED);

        return headers;
    }

    public static Map<String, String> getBearerAuthheaders(String accessToken) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

        return headers;
    }

    public static void buildAlertDialog(String title, String content, Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(content);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

}
