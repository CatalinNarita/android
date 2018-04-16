package com.delta.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc on 10-Apr-18.
 */

public class RegisterActivity extends Activity {
    @BindViews({R.id.firstNameInput, R.id.lastNameInput, R.id.emailInput, R.id.usernameInput, R.id.passwordInput})
    List<EditText> userDetails;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.register_screen_btn_login)
    public void goToLoginActivity() {
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }

    public void registerUser(final JSONObject jsonRequest) {

        final String stringRequest = jsonRequest.toString();

        String URL = Constants.REGISTER_USER_URL;

        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.POST,
                URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResponse;
                        String username = null;
                        String password = null;
                        String firstName = null;
                        String lastName = null;
                        String email = null;
                        try {
                            jsonResponse = new JSONObject(response);
                            username = jsonResponse.get("username").toString();
                            password = jsonRequest.get("password").toString();
                            firstName = jsonRequest.get("firstName").toString();
                            lastName = jsonRequest.get("lastName").toString();
                            email = jsonRequest.get("email").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        doAutoLogin(username, password, firstName, lastName, email);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.hide();
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return stringRequest == null ? null : stringRequest.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", stringRequest, "utf-8");
                    return null;
                }
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private JSONObject getUserData() {
        String firstName = userDetails.get(0).getText().toString();
        String lastName = userDetails.get(1).getText().toString();
        String email = userDetails.get(2).getText().toString();
        String username = userDetails.get(3).getText().toString();
        String password = userDetails.get(4).getText().toString();

        final JSONObject user = new JSONObject();

        try {
            user.put("firstName", firstName);
            user.put("lastName", lastName);
            user.put("email", email);
            user.put("username", username);
            user.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    private void doAutoLogin(String username, String password, final String firstName, final String lastName, final String email) {

        String URL = String.format(Constants.REQUEST_TOKEN_URL, username, password);

        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + Constants.CLIENT_CREDENTIALS_ENCODED);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LoginService loginService = new LoginService();
                        loginService.handleResponse(response, getApplicationContext(), new UserSessionManager(getApplicationContext()), firstName, lastName, email);
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
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);

    }

    @OnClick(R.id.register_screen_btn_register)
    public void checkIfUserExists() {

        final JSONObject jsonRequest = getUserData();

        String checkUsernameURL = Constants.CHECK_USER;
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject body = new JSONObject();

        try {
            body.put("username", jsonRequest.get("username"));
            body.put("email", jsonRequest.get("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(checkUsernameURL);

        pDialog = new ProgressDialog(this);

        pDialog.setMessage("Registering...");
        pDialog.setCancelable(false);
        pDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                checkUsernameURL,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.get("username").toString().equals("true")) {
                                pDialog.hide();
                                VolleyUtils.buildAlertDialog(Constants.ERROR_TITLE, Constants.USERNAME_IN_USE, RegisterActivity.this);
                            } else {
                                if (response.get("email").toString().equals("true")){
                                    pDialog.hide();
                                    VolleyUtils.buildAlertDialog(Constants.ERROR_TITLE, Constants.EMAIL_IN_USE, RegisterActivity.this);
                                } else {
                                    registerUser(jsonRequest);
                                }
                            }
                        } catch (JSONException e) {
                            pDialog.hide();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            VolleyUtils.buildAlertDialog(Constants.ERROR_TITLE, Constants.NO_CONNECTION, RegisterActivity.this);
                        } else {
                            if(error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;
                                if (statusCode >= 500) {
                                    VolleyUtils.buildAlertDialog(Constants.ERROR_TITLE, Constants.SERVER_DOWN, RegisterActivity.this);
                                }
                            }
                        }
                    }
                }
        );
        requestQueue.add(request);
    }

}
