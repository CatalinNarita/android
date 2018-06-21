package com.edu.licenta.model;

/**
 * Created by naritc
 * on 21-Jun-18.
 */

public class Review {

    private float rating;
    private String comment;

    public Review(float rating, String reviewText) {
        this.rating = rating;
        this.comment = reviewText;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Review{" +
                "rating='" + rating + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
