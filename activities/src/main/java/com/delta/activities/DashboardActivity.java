package com.delta.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class DashboardActivity extends Activity {

    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Bundle extras = getIntent().getExtras();
        String fullName = null;

        if (extras != null) {
            fullName = extras.getString("fullName");
        }

        ((TextView) findViewById(R.id.fullName)).setText("Welcome " + fullName);

    }

    @Override
    protected void onNewIntent(Intent intent) {

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        System.out.println(new String(tag.getId()));

        String URL = "http://76db9428.ngrok.io/user/getByNfcTag/" + new String(tag.getId()) + "1";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ((TextView) findViewById(R.id.firstName)).setText(response.get("firstName").toString());
                            ((TextView) findViewById(R.id.lastName)).setText(response.get("lastName").toString());
                            ((TextView) findViewById(R.id.email)).setText(response.get("email").toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ((TextView) findViewById(R.id.firstName)).setText(error.getCause().getMessage());
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onResume() {

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);

        super.onResume();
    }
}
