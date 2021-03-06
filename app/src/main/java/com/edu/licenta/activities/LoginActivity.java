package com.edu.licenta.activities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

    @Override
    protected void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pDialog != null) {
            pDialog.dismiss();
        }
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

        pDialog = VolleyUtils.buildProgressDialog(getString(R.string.trying_to_log_in), getString(R.string.please_wait), this);

        Long requestTimestamp = System.currentTimeMillis();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                null,
                (JSONObject response) -> {

                    System.out.println("REQUEST ACCESS TOKEN RESPONSE TIME: " + (System.currentTimeMillis() - requestTimestamp)/1000d + " seconds");

                    try {
                        getUserData(username, response.get("access_token").toString(), response.get("refresh_token").toString(), Long.parseLong(response.get("expires_in").toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                (VolleyError error) -> {
                    if (error instanceof NoConnectionError) {
                        VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.no_connection), LoginActivity.this);
                        pDialog.hide();
                    } else {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            if (statusCode == 400) {
                                VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.wrong_credentials), LoginActivity.this);
                            } else {
                                VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.server_down), LoginActivity.this);
                            }
                        }
                        if (error instanceof TimeoutError) {
                            VolleyUtils.buildAlertDialog(getString(R.string.error_title), getString(R.string.server_down), LoginActivity.this);
                        }

                        pDialog.hide();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return VolleyUtils.getBasicAuthHeaders();
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void getUserData(String username, final String accessToken, final String refreshToken, final Long expiresIn) {
        String URL = Constants.BASE_SECURE_URL + "/user/getByUsername/" + username;

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        Long requestTimestamp = System.currentTimeMillis();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                (JSONObject response) -> {

                    System.out.println("USER LOGIN TIME: " + (System.currentTimeMillis() - requestTimestamp)/1000d + " seconds");

                    LoginService loginService = new LoginService();
                    String locale = session.getUserDetails().get(UserSessionManager.KEY_CURRENT_LANG);
                    try {
                        Log.d("User data:", response.toString());
                        loginService.handleResponse(accessToken, refreshToken, expiresIn, getApplicationContext(), session, response.get("firstName").toString(), response.get("lastName").toString(), response.get("email").toString(), response.get("id").toString(), locale);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    pDialog.hide();
                },
                (VolleyError error) -> {
                    error.printStackTrace();
                    pDialog.hide();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return VolleyUtils.getBearerAuthHeaders(accessToken);
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

}
