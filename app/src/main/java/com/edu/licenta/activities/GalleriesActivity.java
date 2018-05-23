package com.edu.licenta.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.adapter.GalleriesAdapter;
import com.edu.licenta.model.Gallery;
import com.edu.licenta.utils.ArtifactsFetchInitiatorEnum;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

/**
 * Created by naritc
 * on 11-Apr-18.
 */

public class GalleriesActivity extends AppCompatActivity {
    UserSessionManager session;
    ProgressDialog pDialog;
    private List<Gallery> galleryList;

    /*@BindView(R.id.galleriesListView)*/
    public ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galleries);
        ButterKnife.bind(this);
        session = new UserSessionManager(getApplicationContext());
        prepareGalleries();
        System.out.println("CREATED");

        listView = findViewById(R.id.galleriesListView);

        listView.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            String galleryId = galleryList.get(i).getId().toString();
            goToArtifactsActivity(galleryId, ArtifactsFetchInitiatorEnum.USER);
        });



    }

    /*@Override
    protected void onResume() {
        super.onResume();
        listView.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            String galleryId = galleryList.get(i).getId().toString();
            goToArtifactsActivity(galleryId, ArtifactsFetchInitiatorEnum.FROM_USER_INTERACTION);
        });
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        pDialog.dismiss();
    }

    private void prepareGalleries() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        String URL = Constants.GET_ALL_GALLERIES_URL;

        final Long requestTimestamp = System.currentTimeMillis();
        pDialog = VolleyUtils.buildProgressDialog("Loading galleries...", "Please wait...", this);

        JsonArrayRequest cacheRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                (JSONArray response) -> {
                    System.out.println("FETCH GALLERIES: " + (System.currentTimeMillis() - requestTimestamp) / 1000d + " seconds");

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

        galleryList = new ArrayList<>();

        for (JSONObject o : jsonObjects) {
            try {
                Gallery gallery = new Gallery(Long.parseLong(o.get("id").toString()), o.get("name").toString(), o.get("description").toString());
                galleryList.add(gallery);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        GalleriesAdapter galleriesAdapter = new GalleriesAdapter(getApplicationContext(), R.layout.row_galleries, galleryList);
        galleriesAdapter.notifyDataSetChanged();

        if (listView != null) {
            listView.setAdapter(galleriesAdapter);
        }

    }

    private void goToArtifactsActivity(String galleryId, ArtifactsFetchInitiatorEnum artifactsFetchSource) {
        Intent i = new Intent(getApplicationContext(), ArtifactsActivity.class);
        i.putExtra("galleryId", galleryId);
        i.putExtra("artifactsFetchSource", artifactsFetchSource);
        startActivity(i);
    }


}
