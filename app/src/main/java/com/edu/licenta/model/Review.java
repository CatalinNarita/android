package com.edu.licenta.model;

/**
 * Created by naritc
 * on 21-Jun-18.
 */

public class Review {

    private float rating;
    private String comment;
    private String userFullName;

    public Review(float rating, String comment, String userFullName) {
        this.rating = rating;
        this.comment = comment;
        this.userFullName = userFullName;
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

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    @Override
    public String toString() {
        return "Review{" +
                "rating='" + rating + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
