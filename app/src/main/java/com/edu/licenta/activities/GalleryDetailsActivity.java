package com.edu.licenta.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.delta.activities.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 23-May-18.
 */

public class GalleryDetailsActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_details);
        String galleryName = getIntent().getStringExtra("position");

        ((TextView)findViewById(R.id.goldName)).setText(galleryName);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.close)
    public void dismissModal() {
        GalleryDetailsActivity.this.finish();
    }
}
