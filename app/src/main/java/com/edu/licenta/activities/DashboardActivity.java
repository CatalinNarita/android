package com.edu.licenta.activities;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.utils.ArtifactsFetchInitiatorEnum;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Locale;
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
        session = new UserSessionManager(getApplicationContext());
        loadLocale();
        setContentView(R.layout.activity_dashboard);

        if (session.checkLogin()) {
            finish();
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        ButterKnife.bind(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagIdBytes = tag.getId();
            String hexaTagId = binaryToHexa(tagIdBytes);

            System.out.println(tagIdBytes);
            System.out.println(hexaTagId);

            addDiscoveredArtifact(hexaTagId, session.getUserDetails().get(UserSessionManager.KEY_USER_ID));

            updateUserPosition(hexaTagId);

            System.out.println(binaryToHexa(tagIdBytes));
        }
    }

    public void updateUserPosition(String tagId) {
        switch (tagId) {
            case "040B62E26F3F81":
                Constants.userPositionX = Constants.artifact1PosX - 540;
                Constants.userPositionY = Constants.artifact1PosY - 1580;
                break;
            case "047464E26F3F80":
                Constants.userPositionX = Constants.artifact2PosX - 540;
                Constants.userPositionY = Constants.artifact2PosY - 1580;
                break;
            case "04725DE26F3F80":
                Constants.userPositionX = Constants.artifact21PosX - 540;
                Constants.userPositionY = Constants.artifact21PosY - 1580;
                break;
            case "04BE63E26F3F80":
                Constants.userPositionX = Constants.artifact22PosX - 540;
                Constants.userPositionY = Constants.artifact22PosY - 1580;
                break;
            case "047A64E26F3F80":
                Constants.userPositionX = Constants.artifact41PosX - 540;
                Constants.userPositionY = Constants.artifact41PosY - 1580;
                break;
            case "047B65E26F3F80":
                Constants.userPositionX = Constants.artifact42PosX - 540;
                Constants.userPositionY = Constants.artifact42PosY - 1580;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session.checkLogin()) {
            finish();
        }

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

    @OnClick(R.id.galleriesId)
    public void goToGalleriesActivity() {
        Intent i = new Intent(getApplicationContext(), GalleriesActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.btnLogout)
    public void logoutUser() {
        session.logoutUser();
    }

    public void addDiscoveredArtifact(String tagId, String userId) {
        String URL = String.format(Constants.ADD_DISCOVERED_ARTIFACT, tagId, userId);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        Long requestTimestamp = System.currentTimeMillis();
        String locale = Locale.getDefault().getLanguage();

        pDialog = VolleyUtils.buildProgressDialog(getString(R.string.new_artifact), getString(R.string.please_wait), this);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.POST,
                URL,
                null,
                (JSONArray response) -> {
                    System.out.println("ADD NEW DISCOVERED ARTIFACT: " + (System.currentTimeMillis() - requestTimestamp) / 1000d + " seconds");
                    String galleryId = null;
                    try {
                        galleryId = locale.equals("en") ? String.valueOf((Integer) response.get(0)) : String.valueOf((Integer) response.get(1));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (galleryId.equals("-1")) {
                        pDialog.hide();
                        VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.no_artifact), this);
                    } else {
                        goToArtifactActivity(galleryId, ArtifactsFetchInitiatorEnum.NFC);
                        System.out.println("AICI: " + response);
                        pDialog.hide();
                    }
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

    private void goToArtifactActivity(String galleryId, ArtifactsFetchInitiatorEnum artifactsFetchSource) {
        Intent i = new Intent(getApplicationContext(), ArtifactsActivity.class);
        i.putExtra("galleryId", galleryId);
        i.putExtra("artifactsFetchSource", artifactsFetchSource);
        startActivity(i);
    }

    static String binaryToHexa(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    @OnClick(R.id.settingsId)
    public void goToSettingsActivity() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.poiId)
    public void goToPoiActivity() {
        Intent intent = new Intent(getApplicationContext(), POIActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.mapId)
    public void goToMapActivity() {
        Intent i = new Intent(getApplicationContext(), MapActivity.class);
        startActivity(i);
    }

    public void loadLocale() {
        String language = session.getUserDetails().get(UserSessionManager.KEY_CURRENT_LANG);
        if (language != null) {
            changeLang(language);
        } else {
            session.changeLanguage("en");
            changeLang("en");
        }
        System.out.println("AICI:" + language);
    }

    public void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;
        Locale myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

    }
}
