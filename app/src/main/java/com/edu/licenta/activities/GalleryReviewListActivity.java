package com.edu.licenta.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.adapter.ReviewsAdapter;
import com.edu.licenta.model.Review;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;

/**
 * Created by naritc
 * on 22-Jun-18.
 */

public class GalleryReviewListActivity extends Activity {
    UserSessionManager session;
    ProgressDialog pDialog;
    private List<Review> reviewList;
    public ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_review_list);
        ButterKnife.bind(this);

        session = new UserSessionManager(getApplicationContext());
        listView = findViewById(R.id.galleryReviewsView);

        Intent i = getIntent();
        Long galleryId = i.getLongExtra("galleryId", -1);

        prepareReviews(galleryId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pDialog.dismiss();
    }

    private void prepareReviews(Long galleryId) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        String URL = String.format(Constants.GET_ALL_GALLERY_REVIEWS_URL, galleryId);
;
        pDialog = VolleyUtils.buildProgressDialog(getString(R.string.loading_reviews), getString(R.string.please_wait), this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                (JSONArray response) -> {
                    pDialog.hide();
                    System.out.println(response);
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

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(100),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
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

        reviewList = new ArrayList<>();

        for (JSONObject o : jsonObjects) {
            try {
                Review review = new Review(Float.parseFloat(o.get("rating").toString()), o.get("comment").toString(), o.get("userFullName").toString());
                reviewList.add(review);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ReviewsAdapter reviewsAdapter = new ReviewsAdapter(getApplicationContext(), R.layout.row_reviews, reviewList);
        reviewsAdapter.notifyDataSetChanged();

        if (listView != null) {
            listView.setAdapter(reviewsAdapter);
        }

    }
}
