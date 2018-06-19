package com.edu.licenta.activities;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.delta.activities.R;
import com.edu.licenta.model.Gallery;
import com.edu.licenta.utils.VolleyUtils;

import org.json.JSONObject;
import org.w3c.dom.Text;

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

public class GalleryDetailsActivity extends Activity {
    @BindView(R.id.btn_play_pause)
    ImageButton playButton;

    @BindView(R.id.seek_bar)
    SeekBar seekBar;

    @BindViews({R.id.galleryName, R.id.galleryDescription})
    List<TextView> gTextViews;

    MediaPlayer mp = new MediaPlayer();
    boolean paused = true;
    File temp = null;
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

        Gallery gallery = (Gallery) getIntent().getSerializableExtra("gallery");
        String galleryName = gallery.getName();
        String galleryDescription = gallery.getDescription();

        gTextViews.get(0).setText(galleryName);
        gTextViews.get(1).setText(galleryDescription);

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
        String URL = "https://texttospeech.googleapis.com/v1beta1/text:synthesize";

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

    @OnClick(R.id.close)
    public void dismissModal() {
        GalleryDetailsActivity.this.finish();
    }

    @OnClick(R.id.btn_play_pause)
    public void synthesizeText() {

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

    public void buildAudio(String base64Audio) {
        byte[] soundBytes = Base64.decode(base64Audio, Base64.DEFAULT);

        try {
            temp = File.createTempFile("description", "mp3", getCacheDir());
            temp.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(temp);
            fos.write(soundBytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.reset();
    }
}
