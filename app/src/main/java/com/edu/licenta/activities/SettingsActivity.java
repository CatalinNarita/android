package com.edu.licenta.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.delta.activities.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 09-Jun-18.
 */

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.change_language_btn)
    public void changeLanguage() {
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCALE_SETTINGS);
        startActivity(intent);
    }
}
