package com.edu.licenta.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.model.Review;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 20-Jun-18.
 */

public class GalleryReviewActivity extends Activity {

    @BindView(R.id.rating_bar_gallery)
    RatingBar ratingBar;

    @BindView(R.id.rating_text_gallery)
    EditText editText;

    @BindView(R.id.bubble_gallery_review)
    LinearLayout linearLayout;

    private String userId;
    private Long galleryId;
    private UserSessionManager session;
    private String accessToken;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_review);
        ButterKnife.bind(this);

        Intent i = getIntent();

        float x = i.getFloatExtra("x", -1.0f);
        float y = i.getFloatExtra("y", -1.0f);
        int rbWidth = i.getIntExtra("rbWidth", -1);
        galleryId = i.getLongExtra("galleryId", -1);

        session = new UserSessionManager(getApplicationContext());
        userId = session.getUserDetails().get(UserSessionManager.KEY_USER_ID);
        accessToken = session.getUserDetails().get(UserSessionManager.KEY_ACCESS_TOKEN);

        linearLayout.setX(x - 50 - rbWidth);
        linearLayout.setY(y - 150);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pDialog != null) {
            pDialog.dismiss();
        }
    }

    @OnClick(R.id.main_layout_gallery_review)
    public void exitActivity() {
        finish();
    }

    @OnClick(R.id.btn_cancel_rating_gallery)
    public void exitActivityFromButton() {
        finish();
    }

    @OnClick(R.id.btn_submit_rating_gallery)
    public void createReview() {
        float rating = ratingBar.getRating();
        String reviewText = editText.getText().toString();
        String firstName = session.getUserDetails().get(UserSessionManager.KEY_FIRST_NAME);
        String lastName = session.getUserDetails().get(UserSessionManager.KEY_LAST_NAME);
        String fullName = firstName + " " + lastName;

        Review galleryReview = new Review(rating, reviewText, fullName);
        JSONObject galleryReviewJson = buildJsonObject(galleryReview);
        submitReview(galleryReviewJson);
    }

    private void submitReview(JSONObject galleryReview) {
        String URL = String.format(Constants.ADD_GALLERY_REVIEW, userId, galleryId);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        pDialog = VolleyUtils.buildProgressDialog(getString(R.string.submiting_review_title), getString(R.string.please_wait), this);

        linearLayout.setVisibility(View.INVISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                galleryReview,
                (JSONObject response) -> {
                    pDialog.hide();
                    finish();
                },
                (VolleyError error) -> pDialog.hide()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return VolleyUtils.getBearerAuthHeaders(accessToken);
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(100),//time out in 10second
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,//DEFAULT_MAX_RETRIES = 1;
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private JSONObject buildJsonObject(Review galleryReview) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("rating", galleryReview.getRating());
            jsonObject.put("comment", galleryReview.getComment());
            jsonObject.put("userFullName", galleryReview.getUserFullName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
