package com.edu.licenta.activities.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.delta.activities.R;
import com.edu.licenta.activities.model.Gallery;

import java.util.List;

/**
 * Created by catal
 * on 4/11/2018.
 */

public class GalleriesAdapter extends RecyclerView.Adapter<GalleriesAdapter.MyViewHolder> {

    private Context mContext;
    private List<Gallery> galleryList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name, description;
        private ImageView thumbnail, overflow;

        private MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            description = view.findViewById(R.id.description);
            thumbnail = view.findViewById(R.id.thumbnail);
            overflow = view.findViewById(R.id.overflow);
        }
    }


    public GalleriesAdapter(Context mContext, List<Gallery> galleryList) {
        this.mContext = mContext;
        this.galleryList = galleryList;
    }

    @Override
    @NonNull
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Gallery gallery = galleryList.get(position);
        holder.name.setText(gallery.getName());
        holder.description.setText(gallery.getDescription());

        // loading album cover using Glide library
        Glide.with(mContext).load(gallery.getImage()).into(holder.thumbnail);

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow);
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_artifact, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        MyMenuItemClickListener() {}

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_write_review:
                    Toast.makeText(mContext, "Write review", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }

    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

}
