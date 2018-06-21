package com.edu.licenta.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.delta.activities.R;
import com.edu.licenta.activities.GalleriesActivity;
import com.edu.licenta.activities.GalleryDetailsActivity;
import com.edu.licenta.model.Gallery;
import com.squareup.picasso.Picasso;

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
            holder.galleryImage = row.findViewById(R.id.galleryImage);

            row.setTag(holder);
        } else {
            holder = (GalleriesHolder) row.getTag();
        }

        Gallery gallery = galleries.get(position);
        holder.galleryName.setText(gallery.getName());
        holder.galleryImage.setImageResource(gallery.getImage());

        Picasso.with(holder.galleryImage.getContext())
                .load(gallery.getImage())
                .fit()
                .into(holder.galleryImage);

        return row;
    }

    private class GalleriesHolder {
        TextView galleryName;
        ImageView galleryImage;
    }
}
