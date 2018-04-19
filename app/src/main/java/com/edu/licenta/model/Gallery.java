package com.edu.licenta.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by catal
 * on 4/11/2018.
 */

public class Gallery implements Parcelable{

    private String name;
    private String description;
    private int image;

    public Gallery(String name, String description, int image) {
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(image);
    }

    public static final Parcelable.Creator<Gallery> CREATOR =
            new Parcelable.Creator<Gallery>() {
                public Gallery createFromParcel(Parcel in) {
                   return new Gallery(in);
                }

                @Override
                public Gallery[] newArray(int size) {
                    return new Gallery[size];
                }
            };

    private Gallery(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.image = in.readInt();
    }

    @Override
    public String toString() {
        return "Gallery{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image=" + image +
                '}';
    }
}
