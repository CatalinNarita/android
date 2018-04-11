package com.delta.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    UserSessionManager session;
    Button btnLogout;
    Button btnDashboard;
    ProgressDialog pDialog;

    Tag detectedTag;
    TextView tagContent;
    NfcAdapter nfcAdapter;
    IntentFilter[] readTagFilters;
    PendingIntent pendingIntent;

    private static int readCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new UserSessionManager(getApplicationContext());

        if (session.checkLogin()) {
            finish();
        }

        TextView firstName = findViewById(R.id.lblFirstName);
        TextView lastName = findViewById(R.id.lblLastName);
        TextView email = findViewById(R.id.lblEmail);

        HashMap<String, String> user = session.getUserDetails();

        String fn = user.get(UserSessionManager.KEY_FIRST_NAME);
        String ln = user.get(UserSessionManager.KEY_LAST_NAME);
        String em = user.get(UserSessionManager.KEY_EMAIL);

        if (session.hasTokenExpired()) {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();
            requestNewToken(session, fn, ln, em);
        }

        firstName.setText(Html.fromHtml("First name: <b>" + fn + "</b>"));
        lastName.setText(Html.fromHtml("Last name: <b>" + ln + "</b>"));
        email.setText(Html.fromHtml("Email: <b>" + em + "</b>"));

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                session.logoutUser();
            }
        });

        btnDashboard = findViewById(R.id.btnDashboard);
        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(i);
            }
        });

        tagContent = findViewById(R.id.lblTagContent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }
                NdefMessage msg = messages[0];

                try {
                    String content = new String(msg.getRecords()[0].getPayload(), "UTF-8");
                    tagContent.setText(content + readCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        readCount++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            System.out.println("not supported");
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

    }


    @Override
    protected void onPause() {
        super.onPause();
        if(nfcAdapter!= null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    public void requestNewToken(UserSessionManager session, final String fn, final String ln, final String em) {

        String URL = String.format(Constants.REQUEST_NEW_TOKEN, session.getUserDetails().get(UserSessionManager.KEY_REFRESH_TOKEN));

        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + Constants.CLIENT_CREDENTIALS_ENCODED);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleResponse(response, fn, ln, em);
                        pDialog.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        pDialog.hide();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void handleResponse(JSONObject response, String fn, String ln, String em) {

        String accessToken;
        String refreshToken;
        Long expiresIn;

        try {

            accessToken = response.get("access_token").toString();
            refreshToken = response.get("refresh_token").toString();
            expiresIn = Long.parseLong(response.get("expires_in").toString());

            session.createUserLoginSession(fn, ln, em, accessToken, refreshToken, expiresIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Starting MainActivity
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        Toast.makeText(getApplicationContext(), "Refreshed logged in user token", Toast.LENGTH_LONG).show();
    }

}
