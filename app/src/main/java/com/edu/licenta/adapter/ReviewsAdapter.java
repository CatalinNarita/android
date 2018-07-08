package com.edu.licenta.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.delta.activities.R;
import com.edu.licenta.model.Review;
import com.edu.licenta.utils.UserSessionManager;

import java.util.List;

/**
 * Created by naritc
 * on 22-Jun-18.
 */

public class ReviewsAdapter extends ArrayAdapter<Review> {

    private Context context;
    private int layoutResourceId;
    private List<Review> reviews;
    UserSessionManager sessionManager;

    public ReviewsAdapter(@NonNull Context context, int resource, @NonNull List<Review> objects) {
        super(context, resource, objects);

        this.context = context;
        this.layoutResourceId = resource;
        this.reviews = objects;
    }

    @Nullable
    @Override
    public Review getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ReviewsAdapter.ReviewsHolder holder;

        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ReviewsAdapter.ReviewsHolder();
            holder.userName = row.findViewById(R.id.user_name);
            holder.rating = row.findViewById(R.id.user_rating);
            holder.comment = row.findViewById(R.id.review_text);

            row.setTag(holder);
        } else {
            holder = (ReviewsAdapter.ReviewsHolder) row.getTag();
        }

        Review review = reviews.get(position);

        holder.userName.setText(review.getUserFullName());
        int rating = Math.round(review.getRating());
        String holderRating = Integer.toString(rating);
        holder.rating.setText(holderRating + "/5");
        holder.comment.setText(review.getComment());


        return row;
    }

    private static class ReviewsHolder {
        TextView userName;
        TextView rating;
        TextView comment;
    }

}
