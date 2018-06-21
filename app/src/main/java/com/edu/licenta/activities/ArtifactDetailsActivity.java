package com.edu.licenta.activities;

import android.app.Activity;
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
import com.edu.licenta.model.Artifact;
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
 * on 20-Jun-18.
 */

public class ArtifactDetailsActivity extends AppCompatActivity {

    @BindView(R.id.btn_play_pause_artifact_details)
    ImageButton playButton;

    @BindView(R.id.seek_bar_artifact_details)
    SeekBar seekBar;

    @BindViews({R.id.artifact_name_artifact_details, R.id.artifact_description_artifact_details})
    List<TextView> gTextViews;

    @BindView(R.id.dummy_rating_bar_artifact)
    RatingBar ratingBar;

    Toolbar toolbar;
    MediaPlayer mp = new MediaPlayer();
    boolean paused = true;
    String textBasic;
    String textAdvanced;
    boolean isBasicDescription = true;
    File tempBasic = null;
    File tempAdvanced = null;
    FileInputStream fisBasic;
    FileInputStream fisAdvanced;
    Handler mSeekBarUpdateHandler = new Handler();
    Artifact artifact;
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
        setContentView(R.layout.activity_artifact_details);
        ButterKnife.bind(this);

        artifact = (Artifact) getIntent().getSerializableExtra("artifact");
        String artifactName = artifact.getName();
        textBasic = artifact.getTextBasic();
        textAdvanced = artifact.getTextAdvanced();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener((View v) -> onBackPressed());

        loadBackdrop(getIntent().getIntExtra("img", -1));

        System.out.println(artifact);

        gTextViews.get(0).setText(artifactName);
        gTextViews.get(1).setText(textBasic);

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
        getEncodedAudio(textBasic, textAdvanced);
    }

    private void getEncodedAudio(String textBasic, String textAdvanced) {
        String URL = Constants.TTS_URL;

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject bodyBasic = VolleyUtils.buildGTTSRequestBody(textBasic);
        JSONObject bodyAdvanced = VolleyUtils.buildGTTSRequestBody(textAdvanced);

        JsonObjectRequest requestBasic = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                bodyBasic,
                (JSONObject response) -> {
                    String audioContent;
                    try {
                        audioContent = (String) response.get("audioContent");
                        tempBasic = buildAudio(audioContent,"basic", "mp3");
                        try {
                            fisBasic = new FileInputStream(tempBasic);
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

        JsonObjectRequest requestAdvanced = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                bodyAdvanced,
                (JSONObject response) -> {
                    String audioContent;
                    try {
                        audioContent = (String) response.get("audioContent");
                        tempAdvanced = buildAudio(audioContent,"advanced", "mp3");
                        try {
                            fisAdvanced = new FileInputStream(tempAdvanced);
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

        requestQueue.add(requestBasic);
        requestQueue.add(requestAdvanced);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.btn_play_pause_artifact_details)
    public void synthesizeText() {
        try {
            if (isBasicDescription) {
                mp.setDataSource(fisBasic.getFD());
                mp.prepare();
                seekBar.setMax(mp.getDuration());
            } else {
                mp.setDataSource(fisAdvanced.getFD());
                mp.prepare();
                seekBar.setMax(mp.getDuration());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (paused) {
            try {
                mp.start();
                paused = false;
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                mSeekBarUpdateHandler.postDelayed(mUpdateSeekBar, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (mp != null && mp.isPlaying()) {
                mp.pause();
                paused = true;
                playButton.setImageResource(android.R.drawable.ic_media_play);
                mSeekBarUpdateHandler.removeCallbacks(mUpdateSeekBar);
            }
        }
    }

    public File buildAudio(String base64Audio, String fileName, String suffix) {
        byte[] soundBytes = Base64.decode(base64Audio, Base64.DEFAULT);
        File file = null;

        try {
            file = File.createTempFile(fileName, suffix, getCacheDir());
            file.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(soundBytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    @OnClick(R.id.fab_artifact_details)
    public void goToArtifactReviewActivity() {
        Intent i = new Intent(getApplicationContext(), ArtifactReviewActivity.class);
        FloatingActionButton fab = findViewById(R.id.fab_artifact_details);
        float x = fab.getX();
        float y = fab.getY();
        i.putExtra("x", x);
        i.putExtra("y", y);
        i.putExtra("rbWidth", ratingBar.getWidth());
        i.putExtra("artifactId", artifact.getId());
        startActivity(i);
    }

    @OnClick(R.id.btn_change_description)
    public void changeDescription() {
        mp.reset();
        isBasicDescription = !isBasicDescription;
        if (isBasicDescription) {
            gTextViews.get(1).setText(textBasic);
        } else {
            gTextViews.get(1).setText(textAdvanced);
        }
    }

    private void loadBackdrop(final int drawable) {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);

        Picasso.with(getApplicationContext())
                .load(drawable)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        ArtifactDetailsActivity.this.supportStartPostponedEnterTransition();
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
