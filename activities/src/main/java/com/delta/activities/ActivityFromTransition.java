package com.delta.activities;

import android.app.Activity;
import android.os.Bundle;
import android.transition.ChangeBounds;

/**
 * Created by naritc on 11-Apr-18.
 */

public class ActivityFromTransition extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        ChangeBounds bounds = new ChangeBounds();
        bounds.setDuration(300);
        getWindow().setSharedElementEnterTransition(bounds);

    }

}
