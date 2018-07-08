package com.edu.licenta.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.RadioButton;

import com.delta.activities.R;
import com.edu.licenta.utils.UserSessionManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 09-Jun-18.
 */

public class SettingsActivity extends Activity {

    private UserSessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        session = new UserSessionManager(getApplicationContext());

        String currentLanguage = session.getUserDetails().get(UserSessionManager.KEY_CURRENT_LANG);

        RadioButton radioEnglish = findViewById(R.id.radio_english);
        RadioButton radioRomana = findViewById(R.id.radio_romana);

        if (currentLanguage.equals("ro")) {
            radioRomana.toggle();
        } else {
            radioEnglish.toggle();
        }

        ButterKnife.bind(this);
    }

    @OnClick(R.id.radio_english)
    public void toEn() {
        changeLang("en");
    }
    @OnClick(R.id.radio_romana)
    public void toRo() {
        changeLang("ro");
    }

    private void changeLang(String locale) {
        session.changeLanguage(locale);
        Intent refresh = new Intent(this, DashboardActivity.class);
        startActivity(refresh);
    }
}
