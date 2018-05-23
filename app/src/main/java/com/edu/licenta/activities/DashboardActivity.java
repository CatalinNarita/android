package com.edu.licenta.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.utils.ArtifactsFetchInitiatorEnum;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new UserSessionManager(getApplicationContext());
        if (session.checkLogin()) {
            finish();
        }
        if (session.hasTokenExpired()) {
            System.out.println("test");
            renewBearerToken(session);
        }

        ButterKnife.bind(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagIdBytes = tag.getId();
            String tagId = new String(tagIdBytes);

            addDiscoveredArtifact(binaryToHexa(tagIdBytes), session.getUserDetails().get(UserSessionManager.KEY_USER_ID));
            System.out.println(binaryToHexa(tagIdBytes));
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

        if (pDialog != null) {
            pDialog.dismiss();
        }

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    public void requestNewToken(UserSessionManager session, final String fn, final String ln, final String em) {
        String URL = String.format(Constants.REQUEST_NEW_TOKEN, session.getUserDetails().get(UserSessionManager.KEY_REFRESH_TOKEN));
        String userId = session.getUserDetails().get(UserSessionManager.KEY_USER_ID);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                null,
                (JSONObject response) -> {
                    handleResponse(userId, response, fn, ln, em);
                    pDialog.hide();
                },
                (VolleyError error) -> {
                    error.printStackTrace();
                    pDialog.hide();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return VolleyUtils.getBasicAuthHeaders();
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void handleResponse(String userId, JSONObject response, String fn, String ln, String em) {
        String accessToken;
        String refreshToken;
        Long expiresIn;

        try {
            accessToken = response.get("access_token").toString();
            refreshToken = response.get("refresh_token").toString();
            expiresIn = Long.parseLong(response.get("expires_in").toString());

            session.createUserLoginSession(userId, fn, ln, em, accessToken, refreshToken, expiresIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        Toast.makeText(getApplicationContext(), "Refreshed logged in user token", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.galleriesId)
    public void goToGalleriesActivity() {
        Intent i = new Intent(getApplicationContext(), GalleriesActivity.class);
        // i.putParcelableArrayListExtra("galleries", galleryList);
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

    public void addDiscoveredArtifact(String tagId, String userId) {
        String URL = String.format(Constants.ADD_DISCOVERED_ARTIFACT, tagId, userId);

        System.out.println(URL);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        Long requestTimestamp = System.currentTimeMillis();

        pDialog = VolleyUtils.buildProgressDialog("New artifact discovered!", "Please wat...", this);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                (String response) -> {
                    System.out.println("ADD NEW DISCOVERED ARTIFACT: " + (System.currentTimeMillis() - requestTimestamp)/1000d + " seconds");
                    goToArtifactActivity(response, ArtifactsFetchInitiatorEnum.NFC);
                    System.out.println("AICI: " + response);
                    pDialog.hide();
                },
                VolleyError::printStackTrace
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return VolleyUtils.getBearerAuthHeaders(session.getUserDetails().get(UserSessionManager.KEY_ACCESS_TOKEN));
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(100),//time out in 10second
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,//DEFAULT_MAX_RETRIES = 1;
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private String getEncodedTagId(String tagId) {
        return Base64.encodeToString(tagId.getBytes(), Base64.NO_WRAP);
    }

    private void goToArtifactActivity(String galleryId, ArtifactsFetchInitiatorEnum artifactsFetchSource) {
        Intent i = new Intent(getApplicationContext(), ArtifactsActivity.class);
        i.putExtra("galleryId", galleryId);
        i.putExtra("artifactsFetchSource", artifactsFetchSource);
        startActivity(i);
    }
    static String binaryToHexa(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

}
