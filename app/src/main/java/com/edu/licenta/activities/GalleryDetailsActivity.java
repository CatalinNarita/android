package com.edu.licenta.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.model.Gallery;
import com.edu.licenta.utils.Constants;
import com.edu.licenta.utils.VolleyUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naritc
 * on 23-May-18.
 */

public class GalleryDetailsActivity extends AppCompatActivity {
    @BindView(R.id.btn_play_pause_gallery_details)
    ImageButton playButton;

    @BindView(R.id.seek_bar_gallery_details)
    SeekBar seekBar;

    @BindViews({R.id.gallery_name_gallery_details, R.id.gallery_description_gallery_details})
    List<TextView> gTextViews;

    @BindView(R.id.dummy_rating_bar_gallery)
    RatingBar ratingBar;

    MediaPlayer mp = new MediaPlayer();
    boolean paused = true;

    Toolbar toolbar;
    Gallery gallery;

    File temp = null;
    String galleryDescription;
    Handler mSeekBarUpdateHandler = new Handler();
    Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(mp.getCurrentPosition());
            mSeekBarUpdateHandler.postDelayed(this, 50);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_details);
        ButterKnife.bind(this);

        gallery = (Gallery) getIntent().getSerializableExtra("gallery");
        String galleryName = gallery.getName();
        galleryDescription = gallery.getDescription();

        gTextViews.get(0).setText(galleryName);
        gTextViews.get(1).setText(galleryDescription);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener((View v) -> onBackPressed());

        loadBackdrop(getIntent().getIntExtra("img", -1));

        mp.setOnCompletionListener((MediaPlayer mp) -> {
            playButton.setImageResource(android.R.drawable.ic_media_play);
            paused = true;
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mp.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        getEncodedAudio(galleryDescription);
    }

    private void getEncodedAudio(String galleryDescription) {
        String URL = Constants.TTS_URL;

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject body = VolleyUtils.buildGTTSRequestBody(galleryDescription);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                body,
                (JSONObject response) -> {
                    String audioContent;
                    try {
                        audioContent = (String) response.get("audioContent");
                        buildAudio(audioContent);
                        try {
                            FileInputStream fis = new FileInputStream(temp);
                            mp.setDataSource(fis.getFD());
                            mp.prepare();
                            seekBar.setMax(mp.getDuration());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                (VolleyError error) -> {
                    System.out.println(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return VolleyUtils.getGTTSHeaders();
            }
        };

        requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getEncodedAudio(galleryDescription);
    }

    @OnClick(R.id.btn_play_pause_gallery_details)
    public void synthesizeText() {

        if (paused) {
            try {
                mp.start();
                paused = false;
                playButton.setImageResource(R.drawable.round_pause_circle_outline_black_48);
                mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (mp != null && mp.isPlaying()) {
                mp.pause();
                paused = true;
                playButton.setImageResource(R.drawable.round_play_circle_outline_black_48);
                mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
            }
        }
    }

    public void buildAudio(String base64Audio) {
        byte[] soundBytes = Base64.decode(base64Audio, Base64.DEFAULT);

        try {
            temp = File.createTempFile("description", "mp3", getCacheDir());
            //temp.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(temp);
            fos.write(soundBytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.fab_gallery_details)
    public void goToGalleryReviewActivity() {
        Intent i = new Intent(getApplicationContext(), GalleryReviewActivity.class);
        FloatingActionButton fab = findViewById(R.id.fab_gallery_details);
        float x = fab.getX();
        float y = fab.getY();
        i.putExtra("x", x);
        i.putExtra("y", y);
        i.putExtra("rbWidth", ratingBar.getWidth());
        i.putExtra("galleryId", gallery.getId());
        startActivity(i);
    }

    private void loadBackdrop(final int drawable) {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);

        Picasso.with(getApplicationContext())
                .load(drawable)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        GalleryDetailsActivity.this.supportStartPostponedEnterTransition();
                    }

                    @Override
                    public void onError() {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.reset();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.reset();
    }
}
