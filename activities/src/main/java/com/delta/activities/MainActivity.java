package com.delta.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


public class MainActivity extends Activity {

    public static final int LOGIN_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button singInButton = (Button) findViewById(R.id.signIn);
        singInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryLogin(view);
            }
        });

    }

    public void tryLogin(final View view) {
        String username = ((EditText)findViewById(R.id.username)).getText().toString();
        final String password = ((EditText)findViewById(R.id.password)).getText().toString();

        String URL = "http://76db9428.ngrok.io/user/get/" + username;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.get("password").equals(password)) {
                                Intent i = new Intent(view.getContext(), DashboardActivity.class);
                                i.putExtra("fullName", response.get("firstName").toString() + " " + response.get("lastName").toString());
                                startActivityForResult(i, LOGIN_REQUEST);
                            } else {
                                ((TextView) findViewById(R.id.errorText)).setText("Wrong username or password");
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ((TextView) findViewById(R.id.errorText)).setText("No record found");
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }
}
