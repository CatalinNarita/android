package com.edu.licenta.activities;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.adapter.GalleriesAdapter;
import com.edu.licenta.model.Gallery;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by naritc
 * on 11-Apr-18.
 */

public class GalleriesActivity extends AppCompatActivity {
    UserSessionManager session;
    ProgressDialog pDialog;
    private List<Gallery> galleryList;

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    private GalleriesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galleries);
        ButterKnife.bind(this);

        session = new UserSessionManager(getApplicationContext());

        //Bundle b = this.getIntent().getExtras();
        prepareGalleries();

        /*if(b != null) {
            pDialog = VolleyUtils.buildProgressDialog(null, "Loading...", this);
            galleryList = b.getParcelableArrayList("galleries");
        }*/

        adapter = new GalleriesAdapter(this, new ArrayList<>());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pDialog.dismiss();
    }

    private void prepareGalleries() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        String URL = Constants.GET_ALL_GALLERIES_URL;

        final Long requestTime = System.currentTimeMillis();
        pDialog = VolleyUtils.buildProgressDialog("Loading galleries...", "Please wait...", this);

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
            public Map<String, String> getHeaders() throws AuthFailureError {
                return VolleyUtils.getBearerAuthheaders(session.getUserDetails().get(UserSessionManager.KEY_ACCESS_TOKEN));
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

        int[] covers = new int[]{
                R.drawable.history,
                R.drawable.science,
                R.drawable.nature};
        galleryList = new ArrayList<>();

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
        buildRecyclerView();
    }

    private void buildRecyclerView() {


        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new GalleriesAdapter(this, galleryList));

        adapter.notifyDataSetChanged();
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx() {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));
    }

    private class CacheRequest extends Request<NetworkResponse> {
        private final Response.Listener<NetworkResponse> mListener;
        private final Response.ErrorListener mErrorListener;

        public CacheRequest(int method, String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.mListener = listener;
            this.mErrorListener = errorListener;
        }


        @Override
        protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
            Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
            if (cacheEntry == null) {
                cacheEntry = new Cache.Entry();
            }
            final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
            final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
            long now = System.currentTimeMillis();
            final long softExpire = now + cacheHitButRefreshed;
            final long ttl = now + cacheExpired;
            cacheEntry.data = response.data;
            cacheEntry.softTtl = softExpire;
            cacheEntry.ttl = ttl;
            String headerValue;
            headerValue = response.headers.get("Date");
            if (headerValue != null) {
                cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
            }
            headerValue = response.headers.get("Last-Modified");
            if (headerValue != null) {
                cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
            }
            cacheEntry.responseHeaders = response.headers;

            System.out.println(response.data);
            System.out.println(cacheEntry.data);

            return Response.success(response, cacheEntry);
        }

        @Override
        protected void deliverResponse(NetworkResponse response) {
            mListener.onResponse(response);
        }

        @Override
        protected VolleyError parseNetworkError(VolleyError volleyError) {
            return super.parseNetworkError(volleyError);
        }

        @Override
        public void deliverError(VolleyError error) {
            mErrorListener.onErrorResponse(error);
        }
    }


}
