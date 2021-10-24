package com.example.android.popularmovies_project.Models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.popularmovies_project.Data.FavouriteContract.MovieEntry;
import com.example.android.popularmovies_project.Utils.MovieUtils;

/**
 * Model class for Movie objects
 * <p>
 * Implemented {@link Parcelable} interface so that we can the put arraylist of parcelable {@link Movie} objects
 * to the bundle via onSaveInstanceState method in order to persist data between screen orientation
 */
public class Movie implements Parcelable {
    private int mId;
    private String mPosterPath;
    private String mBackdropPath;
    private String mOriginalLanguage;
    private String mTitle;
    private double mVoteAverage;
    private String mOverview;
    private long mReleaseDateMillis;

    public Movie(int id, String posterPath, String backdropPath, String originalLanguage, String title, double voteAverage, String overview, long releaseDateMillis) {
        mId = id;
        mPosterPath = posterPath;
        mBackdropPath = backdropPath;
        mOriginalLanguage = originalLanguage;
        mTitle = title;
        mVoteAverage = voteAverage;
        mOverview = overview;
        mReleaseDateMillis = releaseDateMillis;
    }

    public int getId() {
        return mId;
    }

    /**
     * Use the path string returned from this method in conjunction with the getImageLink(...) method
     * of {@link MovieUtils} to get the link of the image
     *
     * @return the path of the poster image
     */
    public String getPosterPath() {
        return mPosterPath;
    }

    /**
     * Use the path string returned from this method in conjunction with the getImageLink(...) method
     * of {@link MovieUtils} to get the link of the image
     *
     * @return the path of the backdrop image
     */
    public String getBackdropPath() {
        return mBackdropPath;
    }

    public String getOriginalLanguage() {
        return mOriginalLanguage;
    }

    public String getTitle() {
        return mTitle;
    }

    public double getVoteAverage() {
        return mVoteAverage;
    }

    public String getOverview() {
        return mOverview;
    }

    public long getReleaseDateMillis() {
        return mReleaseDateMillis;
    }

    @Override
    public String toString() {
        String strRepresentation;
        strRepresentation = "Id: " + getId() + "\n"
                + "Title: " + getTitle() + "\n"
                + "Release Date Millis: " + getReleaseDateMillis() + "\n"
                + "Poster Image Link: " + MovieUtils.getImageLink(MovieUtils.IMAGE_QUALITY_W342, getPosterPath()) + "\n"
                + "Backdrop Image Link: " + MovieUtils.getImageLink(MovieUtils.IMAGE_QUALITY_ORIGINAL, getBackdropPath()) + "\n"
                + "Average Votes: " + getVoteAverage() + "/10" + "\n"
                + "Overview: " + getOverview() + "\n"
                + "Language: " + getOriginalLanguage();

        return strRepresentation;
    }


    protected Movie(Parcel in) {
        mId = in.readInt();
        mPosterPath = in.readString();
        mBackdropPath = in.readString();
        mOriginalLanguage = in.readString();
        mTitle = in.readString();
        mVoteAverage = in.readDouble();
        mOverview = in.readString();
        mReleaseDateMillis = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mPosterPath);
        dest.writeString(mBackdropPath);
        dest.writeString(mOriginalLanguage);
        dest.writeString(mTitle);
        dest.writeDouble(mVoteAverage);
        dest.writeString(mOverview);
        dest.writeLong(mReleaseDateMillis);
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Movie) {
            Movie other = (Movie) obj;
            return mId == other.mId;
        }
        return false;
    }


    /**
     * ContentValues to be inserted into {@link MovieEntry}'s movies table.
     *
     * @return ContentValues object containing all the information of this {@link Movie} object
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_MOVIE_ID, mId);
        values.put(MovieEntry.COLUMN_POSTER_PATH, mPosterPath);
        values.put(MovieEntry.COLUMN_BACKDROP_PATH, mBackdropPath);
        values.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, mOriginalLanguage);
        values.put(MovieEntry.COLUMN_TITLE, mTitle);
        values.put(MovieEntry.COLUMN_OVERVIEW, mOverview);
        values.put(MovieEntry.COLUMN_VOTE_AVERAGE, mVoteAverage);
        values.put(MovieEntry.COLUMN_RELEASE_DATE_MILLIS, mReleaseDateMillis);
        return values;
    }
}

