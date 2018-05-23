package com.edu.licenta.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.delta.activities.R;

/**
 * Created by naritc
 * on 23-May-18.
 */

public class GalleryDetailsActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gallery_details);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String galleryName = getIntent().getStringExtra("galleryName");

        showPopup(galleryName);
    }

    public void showPopup(String galleryName) {
        LayoutInflater inflater = (LayoutInflater) GalleryDetailsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_gallery_details, null);

        ((TextView) layout.findViewById(R.id.goldName)).setText(galleryName);

        final PopupWindow pw = new PopupWindow(layout, 240, 285, true);

        (layout.findViewById(R.id.close)).setOnClickListener((View v) -> pw.dismiss());

        pw.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        pw.setTouchInterceptor(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    pw.dismiss();
                    return true;
                }
                return false;
            }
        });
        pw.setOutsideTouchable(true);

        layout.post(() -> pw.showAtLocation(layout, Gravity.CENTER, 0, 0));
    }
}
