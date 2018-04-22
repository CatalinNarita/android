package com.edu.licenta.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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

    @BindView(R.id.artifactsListView)
    public ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artifacts);

        session = new UserSessionManager(getApplicationContext());

        Intent intent = getIntent();

        String galleryId = intent.getStringExtra("galleryId");

        getUserDiscoveredArtifacts(session.getUserDetails().get(UserSessionManager.KEY_USER_ID), galleryId);
        ButterKnife.bind(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pDialog != null) {
            pDialog.dismiss();
        }
    }

    public void getUserDiscoveredArtifacts(String userId, String galleryId) {
         final RequestQueue requestQueue = Volley.newRequestQueue(this);

         String URL = String.format(Constants.GET_USER_DISCOVERED_ARTIFACTS, userId, galleryId);

         final Long requestTime = System.currentTimeMillis();
         pDialog = VolleyUtils.buildProgressDialog("Loading artifacts...", "Please wait...", this);

         CacheRequest cacheRequest = new CacheRequest(
                 Request.Method.GET,
                 URL,
                 (NetworkResponse response) ->  {
                     System.out.println("Request took " + (System.currentTimeMillis() - requestTime) + " milliseconds to complete.");

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
                 (VolleyError error) ->  {
                     pDialog.hide();
                     error.printStackTrace();
                 }
         ) {
             @Override
             public Map<String, String> getHeaders() {
                 return VolleyUtils.getBearerAuthHeaders(session.getUserDetails().get(UserSessionManager.KEY_ACCESS_TOKEN));
             }
         };

        cacheRequest.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(100),//time out in 10second
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,//DEFAULT_MAX_RETRIES = 1;
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

         requestQueue.add(cacheRequest);
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
                Artifact artifact = new Artifact(o.get("name").toString(), o.get("tagId").toString());
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
