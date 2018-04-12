package com.delta.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by naritc on 10-Apr-18.
 */

public class LoginActivity extends Activity {

    Button btnLogin;
    EditText username, password;
    UserSessionManager session;
    TextView registerLink;
    ProgressDialog pDialog;

    ProgressBar progressBar;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new UserSessionManager(getApplicationContext());

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        username = findViewById(R.id.usernameInput);
        password = findViewById(R.id.passwordInput);

        btnLogin = findViewById(R.id.login_screen_btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestAccessToken(v);
            }
        });

        registerLink = findViewById(R.id.login_screen_btn_register);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    public void requestAccessToken(final View view) {
        final String username = ((EditText) findViewById(R.id.usernameInput)).getText().toString();
        final String password = ((EditText) findViewById(R.id.passwordInput)).getText().toString();

        String URL = String.format(Constants.REQUEST_TOKEN_URL, username, password);

        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + Constants.CLIENT_CREDENTIALS_ENCODED);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Trying to log in..");
        pDialog.setMessage("Please wat...");
        pDialog.setCancelable(false);
        pDialog.show();

//        inAnimation = new AlphaAnimation(0f, 1f);
//        inAnimation.setDuration(200);
//        progressBar.setAnimation(inAnimation);
//        progressBar.setVisibility(View.VISIBLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

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
                            Toast.makeText(getApplicationContext(), "You are not connected to the internet!", Toast.LENGTH_LONG).show();
                        } else {
                            if(error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;
                                if (statusCode == 400) {
                                    Toast.makeText(getApplicationContext(), "Wrong username of password!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Could not connect. Please try again later", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

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

    private void getUserData(String username, final String accessToken, final String refreshToken, final Long expiresIn) {
        String URL = Constants.BASE_SECURE_URL + "/user/getByUsername/" + username;

        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + accessToken);

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
                        /*outAnimation = new AlphaAnimation(1f, 0f);
                        outAnimation.setDuration(200);
                        progressBar.setAnimation(outAnimation);
                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
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

}
