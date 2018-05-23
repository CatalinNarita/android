package com.edu.licenta.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.delta.activities.R;
import com.edu.licenta.activities.GalleryDetailsActivity;
import com.edu.licenta.model.Gallery;

import java.util.List;

/**
 * Created by naritc
 * on 19-Apr-18.
 */

public class GalleriesAdapter extends ArrayAdapter<Gallery> {

    private Context context;
    private int layoutResourceId;
    private List<Gallery> galleries;

    public GalleriesAdapter(@NonNull Context context, int resource, @NonNull List<Gallery> objects) {
        super(context, resource, objects);

        this.context =  context;
        this.layoutResourceId = resource;
        this.galleries = objects;
    }

    @Nullable
    @Override
    public Gallery getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        GalleriesHolder holder;

        if(row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new GalleriesHolder();
            holder.galleryName = row.findViewById(R.id.galleryName);
            holder.galleryDescription = row.findViewById(R.id.galleryDescription);
            //holder.detailsButton = row.findViewById(R.id.gallery_details_btn);

//            holder.detailsButton.setOnClickListener((View v) -> {
//                    Intent i = new Intent(context, GalleryDetailsActivity.class);
//                    i.putExtra("galleryName", "asd");
//                    context.startActivity(i);
//                }
//            );

            row.setTag(holder);
        } else {
            holder = (GalleriesHolder) row.getTag();
        }

        Gallery gallery = galleries.get(position);

        holder.galleryName.setText(gallery.getName());
        holder.galleryDescription.setText(gallery.getDescription());

        return row;
    }

    private static class GalleriesHolder {
        TextView galleryName;
        TextView galleryDescription;
        //Button detailsButton;
    }
}
