package com.edu.licenta.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.service.LoginService;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 10-Apr-18.
 */

public class LoginActivity extends AppCompatActivity {

    @BindViews({R.id.usernameInput, R.id.passwordInput})
    List<EditText> userCredentials;
    ProgressDialog pDialog;
    UserSessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new UserSessionManager(getApplicationContext());
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login_screen_btn_register)
    public void goToRegisterActivity() {
        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.login_screen_btn_login)
    public void requestAccessToken(final View view) {
        final String username = userCredentials.get(0).getText().toString();
        final String password = userCredentials.get(1).getText().toString();

        String URL = String.format(Constants.REQUEST_TOKEN_URL, username, password);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Trying to log in..");
        pDialog.setMessage("Please wat...");
        pDialog.setCancelable(false);
        pDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getUserData(username, response.get("access_token").toString(), response.get("refresh_token").toString(), Long.parseLong(response.get("expires_in").toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            VolleyUtils.buildAlertDialog(Constants.ERROR_TITLE, Constants.NO_CONNECTION, LoginActivity.this);
                        } else {
                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;
                                if (statusCode == 400) {
                                    VolleyUtils.buildAlertDialog(Constants.ERROR_TITLE, Constants.WRONG_CREDENTIALS, LoginActivity.this);
                                } else {
                                    VolleyUtils.buildAlertDialog(Constants.ERROR_TITLE, Constants.SERVER_DOWN, LoginActivity.this);
                                }
                            }
                        }

                        if (error instanceof TimeoutError) {
                            VolleyUtils.buildAlertDialog(Constants.ERROR_TITLE, Constants.SERVER_DOWN, LoginActivity.this);
                        }

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

    private void getUserData(String username, final String accessToken, final String refreshToken, final Long expiresIn) {
        String URL = Constants.BASE_SECURE_URL + "/user/getByUsername/" + username;

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LoginService loginService = new LoginService();
                        try {
                            loginService.handleResponse(accessToken, refreshToken, expiresIn, getApplicationContext(), session, response.get("firstName").toString(), response.get("lastName").toString(), response.get("email").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                return VolleyUtils.getBearerAuthheaders(accessToken);
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

}