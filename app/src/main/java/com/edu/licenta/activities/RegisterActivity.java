package com.edu.licenta.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.service.LoginService;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.UserSessionManager;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 10-Apr-18.
 */

public class RegisterActivity extends Activity {
    @BindViews({R.id.firstNameInput, R.id.lastNameInput, R.id.emailInput, R.id.usernameInput, R.id.passwordInput, R.id.repeatPasswordInput})
    List<EditText> userDetails;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
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

        Long requestTimestamp = System.currentTimeMillis();

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest jsonObjectRequest = new StringRequest(
                Request.Method.POST,
                URL,
                (String response) -> {
                    System.out.println("USER REGISTER TIME: " + (System.currentTimeMillis() - requestTimestamp)/1000d + " seconds");
                    JSONObject jsonResponse;
                    String userId = null;
                    String username = null;
                    String password = null;
                    String firstName = null;
                    String lastName = null;
                    String email = null;
                    try {
                        jsonResponse = new JSONObject(response);
                        userId = jsonResponse.get("id").toString();
                        username = jsonResponse.get("username").toString();
                        password = jsonRequest.get("password").toString();
                        firstName = jsonRequest.get("firstName").toString();
                        lastName = jsonRequest.get("lastName").toString();
                        email = jsonRequest.get("email").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    doAutoLogin(userId, username, password, firstName, lastName, email);
                },
                (VolleyError error) -> {
                    pDialog.hide();
                    error.printStackTrace();
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
        String password1 = userDetails.get(4).getText().toString();
        String password = userDetails.get(5).getText().toString();

        if (!password.equals(password1)) {
            VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.password_match), RegisterActivity.this);
        } else {

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

        return null;
    }

    private void doAutoLogin(String userId, String username, String password, final String firstName, final String lastName, final String email) {

        String URL = String.format(Constants.REQUEST_TOKEN_URL, username, password);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        Long requestTimestamp = System.currentTimeMillis();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                null,
                (JSONObject response) -> {
                    System.out.println("USER AUTO LOGIN TIME: " + (System.currentTimeMillis() - requestTimestamp)/1000d + " seconds");
                    LoginService loginService = new LoginService();
                    loginService.handleResponse(response, getApplicationContext(), new UserSessionManager(getApplicationContext()), firstName, lastName, email, userId, "en");
                    pDialog.hide();
                },
                (VolleyError error) -> {
                    error.printStackTrace();
                    pDialog.hide();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return VolleyUtils.getBasicAuthHeaders();
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    @OnClick(R.id.register_screen_btn_register)
    public void checkIfUserExists() {

        final JSONObject jsonRequest = getUserData();

        if (jsonRequest != null) {

            if (!checkUserData(jsonRequest)) {

                String checkUsernameURL = Constants.CHECK_USER;
                final RequestQueue requestQueue = Volley.newRequestQueue(this);

                JSONObject body = new JSONObject();

                try {
                    body.put("username", jsonRequest.get("username"));
                    body.put("email", jsonRequest.get("email"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pDialog = VolleyUtils.buildProgressDialog(null, getString(R.string.registering), this);

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        checkUsernameURL,
                        body,
                        (JSONObject response) -> {
                            try {
                                if (response.get("username").toString().equals("true")) {
                                    pDialog.hide();
                                    VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.username_in_use), RegisterActivity.this);
                                } else {
                                    if (response.get("email").toString().equals("true")) {
                                        pDialog.hide();
                                        VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.email_in_use), RegisterActivity.this);
                                    } else {
                                        registerUser(jsonRequest);
                                    }
                                }
                            } catch (JSONException e) {
                                pDialog.hide();
                                e.printStackTrace();
                            }
                        },
                        (VolleyError error) -> {
                            if (error instanceof NoConnectionError) {
                                VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.no_connection), RegisterActivity.this);
                            } else {
                                if (error.networkResponse != null) {
                                    int statusCode = error.networkResponse.statusCode;
                                    if (statusCode >= 500) {
                                        VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.server_down), RegisterActivity.this);
                                    }
                                }
                            }
                        }
                );
                requestQueue.add(request);
            }
        }
    }

    public boolean checkUserData(JSONObject jsonObject) {
        String firstName = "";
        String lastName = "";
        String email = "";
        String username = "";
        String password = "";

        boolean error = false;

        Pattern ptr = Pattern.compile(Constants.EMAIL_REGEXP);

        try {
            firstName = jsonObject.get("firstName").toString();
            lastName = jsonObject.get("lastName").toString();
            email = jsonObject.get("email").toString();
            username = jsonObject.get("username").toString();
            password = jsonObject.get("password").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (firstName.equals("")) {
            userDetails.get(0).setError(getString(R.string.first_name_r));
            error = true;
        }
        if (lastName.equals("")) {
            userDetails.get(1).setError(getString(R.string.last_name_r));
            error = true;
        }
        if (email.equals("")) {
            userDetails.get(2).setError(getString(R.string.email_r));
            error = true;
        }
        if (!ptr.matcher(email).matches()) {
            userDetails.get(2).setError(getString(R.string.invalid_email));
            error = true;
        }
        if (username.equals("") || username.length() < 6) {
            userDetails.get(3).setError(getString(R.string.username_required));
            error = true;
        }
        if (password.equals("") || password.length() < 6) {
            userDetails.get(4).setError(getString(R.string.password_required));
            error = true;
        }

        return error;
    }
}
