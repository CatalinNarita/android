package com.edu.licenta.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.activities.model.Gallery;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 11-Apr-18.
 */
public class DashboardActivity extends Activity {

    private UserSessionManager session;
    private ProgressDialog pDialog;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private ArrayList<Gallery> galleryList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new UserSessionManager(getApplicationContext());
        if (session.checkLogin()) {
            finish();
        }
        if (session.hasTokenExpired()) {
            renewBearerToken(session);
        }

        galleryList = new ArrayList<>();
        prepareGalleries();
        ButterKnife.bind(this);
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
                    //tagContent.setText(content + readCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    public void requestNewToken(UserSessionManager session, final String fn, final String ln, final String em) {

        String URL = String.format(Constants.REQUEST_NEW_TOKEN, session.getUserDetails().get(UserSessionManager.KEY_REFRESH_TOKEN));

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
                return VolleyUtils.getBasicAuthHeaders();
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

        Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        Toast.makeText(getApplicationContext(), "Refreshed logged in user token", Toast.LENGTH_LONG).show();
    }

    private void prepareGalleries() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        String URL = Constants.GET_ALL_GALLERIES_URL;

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Logging in...");
        pDialog.setCancelable(false);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return VolleyUtils.getBearerAuthheaders(session.getUserDetails().get(UserSessionManager.KEY_ACCESS_TOKEN));
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void parseResponse(JSONArray response) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        try {
            for (int i = 0; i < response.length(); i++) {
                jsonObjects.add(response.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int[] covers = new int[]{
                R.drawable.history,
                R.drawable.science,
                R.drawable.nature};

        for (JSONObject o : jsonObjects) {
            try {
                int image = 0;
                String category = o.get("category").toString();

                switch (category) {
                    case "HISTORY":
                        image = 0;
                        break;
                    case "SCIENCE":
                        image = 1;
                        break;
                    case "NATURE":
                        image = 2;
                        break;
                    default:
                        break;
                }

                Gallery gallery = new Gallery(o.get("name").toString(), o.get("description").toString(), covers[image]);
                galleryList.add(gallery);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.galleriesId)
    public void goToGalleriesActivity() {
        Intent i = new Intent(getApplicationContext(), GalleriesActivity.class);
        i.putParcelableArrayListExtra("galleries", galleryList);
        startActivity(i);
    }

    public void renewBearerToken(UserSessionManager session) {
        HashMap<String, String> user = session.getUserDetails();

        String fn = user.get(UserSessionManager.KEY_FIRST_NAME);
        String ln = user.get(UserSessionManager.KEY_LAST_NAME);
        String em = user.get(UserSessionManager.KEY_EMAIL);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        requestNewToken(session, fn, ln, em);
    }

    @OnClick(R.id.btnLogout)
    public void logoutUser() {
        session.logoutUser();
    }
}
