package com.example.android.popularmovies_project.Models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.popularmovies_project.Data.FavouriteContract.ReviewEntry;

public class Review implements Parcelable {
    private String mAuthor;
    private String mContent;
    private String mId;
    private String mUrl;

    public Review(String author, String content, String id, String url) {
        mAuthor = author;
        mContent = content;
        mId = id;
        mUrl = url;
    }

    public String getAuthor() { return mAuthor; }
    public String getContent() {return mContent; }
    public String getId() { return mId; }
    public String getUrl() { return mUrl; }


    @Override
    public String toString() {
        String strRepresentation;
        strRepresentation = "Author: " + getAuthor() + "\n"
                + "Content: " + getContent() + "\n"
                + "Id: " + getId() + "\n"
                + "Url: " + getUrl();
        return strRepresentation;
    }

    protected Review(Parcel in) {
        mAuthor = in.readString();
        mContent = in.readString();
        mId = in.readString();
        mUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthor);
        dest.writeString(mContent);
        dest.writeString(mId);
        dest.writeString(mUrl);
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Review) {
            Review other = (Review) obj;
            return mId.equals(other.mId);
        }
        return false;
    }


    /**
     * ContentValues to be inserted into {@link ReviewEntry}'s reviews table.
     * Note that this doesn't contain any info/value of column(reviews.movie_id_key) having foreign key in 'reviews' table!
     *
     * @return ContentValues object containing all the information of this {@link Review} object
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(ReviewEntry.COLUMN_REVIEW_ID, mId);
        values.put(ReviewEntry.COLUMN_AUTHOR, mAuthor);
        values.put(ReviewEntry.COLUMN_CONTENT, mContent);
        values.put(ReviewEntry.COLUMN_URL, mUrl);
        return values;
    }
}
