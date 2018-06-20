package com.edu.licenta.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.adapter.ArtifactsAdapter;
import com.edu.licenta.model.Artifact;
import com.edu.licenta.utils.ArtifactsFetchInitiatorEnum;
import com.edu.licenta.utils.CacheRequest;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by naritc
 * on 19-Apr-18.
 */

public class ArtifactsActivity extends AppCompatActivity {

    private List<Artifact> artifactList = new ArrayList<>();
    private ArtifactsAdapter artifactsAdapter;
    private ProgressDialog pDialog;
    private UserSessionManager session;

    public ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artifacts);

        session = new UserSessionManager(getApplicationContext());

        Intent intent = getIntent();

        String galleryId = intent.getStringExtra("galleryId");
        ArtifactsFetchInitiatorEnum artifactsFetchSource = (ArtifactsFetchInitiatorEnum) intent.getSerializableExtra("artifactsFetchSource");

        getUserDiscoveredArtifacts(session.getUserDetails().get(UserSessionManager.KEY_USER_ID), galleryId, artifactsFetchSource);

        listView = findViewById(R.id.artifactsListView);

        listView.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            Intent intent1 = new Intent(getApplicationContext(), ArtifactDetailsActivity.class);
            intent1.putExtra("artifact", artifactList.get(i));
            startActivity(intent1);
        });

        ButterKnife.bind(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pDialog != null) {
            pDialog.dismiss();
        }
    }

    public void getUserDiscoveredArtifacts(String userId, String galleryId, ArtifactsFetchInitiatorEnum artifactsFetchSource) {
        String locale = Locale.getDefault().getLanguage();
        String URL = String.format(Constants.GET_USER_DISCOVERED_ARTIFACTS, userId, galleryId, locale);
        pDialog = VolleyUtils.buildProgressDialog(getString(R.string.loading_artifacts), getString(R.string.please_wait), this);

        switch (artifactsFetchSource) {
            case NFC:
                makeNormalRequest(URL, pDialog);
                break;
            case USER:
                makeNormalRequest(URL, pDialog);
                break;
            default:
                break;
        }
    }

    private void makeNormalRequest(String URL, ProgressDialog pDialog) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final Long requestTimestamp = System.currentTimeMillis();
        //requestQueue.getCache().clear();

        System.out.println(URL);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                (JSONArray response) -> {
                    System.out.println("FETCH ARTIFACTS FROM DB: " + (System.currentTimeMillis() - requestTimestamp) / 1000d + " seconds");

                    pDialog.hide();
                    parseResponse(response);
                },
                (VolleyError error) -> {
                    pDialog.hide();
                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return VolleyUtils.getBearerAuthHeaders(session.getUserDetails().get(UserSessionManager.KEY_ACCESS_TOKEN));
            }
        };

//        request.setRetryPolicy(new DefaultRetryPolicy(
//                (int) TimeUnit.SECONDS.toMillis(100),//time out in 10second
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,//DEFAULT_MAX_RETRIES = 1;
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private void makeCachedRequest(String URL, ProgressDialog pDialog) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final Long requestTimestamp = System.currentTimeMillis();

        CacheRequest request = new CacheRequest(
                Request.Method.GET,
                URL,
                (NetworkResponse response) -> {
                    System.out.println("FETCH ARTIFACTS FROM CACHE: " + (System.currentTimeMillis() - requestTimestamp) / 1000d + " seconds");

                    final String jsonString;
                    JSONArray jsonArray = new JSONArray();

                    try {
                        jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        jsonArray = new JSONArray(jsonString);
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    pDialog.hide();
                    parseResponse(jsonArray);
                },
                (VolleyError error) -> {
                    pDialog.hide();
                    error.printStackTrace();
                }
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

    private void parseResponse(JSONArray response) {

        List<JSONObject> jsonObjects = new ArrayList<>();
        try {
            for (int i = 0; i < response.length(); i++) {
                jsonObjects.add(response.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        artifactList = new ArrayList<>();

        for (JSONObject o : jsonObjects) {
            try {
                Artifact artifact = new Artifact(Long.parseLong(o.get("id").toString()), o.get("name").toString(), o.get("textBasic").toString(), o.get("textAdvanced").toString(), o.get("tagId").toString());
                artifactList.add(artifact);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        artifactsAdapter = new ArtifactsAdapter(getApplicationContext(), R.layout.row_artifacts, artifactList);

        if (listView != null) {
            listView.setAdapter(artifactsAdapter);
        }
    }
}
