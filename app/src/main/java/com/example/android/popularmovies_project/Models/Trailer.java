package com.example.android.popularmovies_project.Models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.popularmovies_project.Data.FavouriteContract.TrailerEntry;
import com.example.android.popularmovies_project.Utils.MovieUtils;

public class Trailer implements Parcelable {
    private String mId;
    private String mKey;
    private String mName;
    private String mSite;
    private int mSize;
    private String mType;

    public Trailer(String id, String key, String name, String site, int size, String type) {
        mId = id;
        mKey = key;
        mName = name;
        mSite = site;
        mSize = size;
        mType = type;
    }

    public String getId() { return mId; }
    public String getKey() { return mKey; }
    public String getName() { return mName; }
    public String getSite() { return mSite; }
    public int getSize() { return mSize; }
    public String getType() { return mType; }

    @Override
    public String toString() {
        String strRepresentation;
        strRepresentation = "Id: " + getId() + "\n"
                + "Key: " + getKey() + "\n"
                + "Trailer video: " + MovieUtils.getTrailerLink(getKey()) + "\n"
                + "Trailer thumbnail: " + MovieUtils.getTrailerThumbnailLink(getKey()) + "\n"
                + "Name: " + getName() + "\n"
                + "Site: " + getSite() + "\n"
                + "Size: " + getSize() + "p" + "\n"
                + "Type: " + getType();

        return strRepresentation;
    }

    protected Trailer(Parcel in) {
        mId = in.readString();
        mKey = in.readString();
        mName = in.readString();
        mSite = in.readString();
        mSize = in.readInt();
        mType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mKey);
        dest.writeString(mName);
        dest.writeString(mSite);
        dest.writeInt(mSize);
        dest.writeString(mType);
    }

    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel source) {
            return new Trailer(source);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Trailer) {
            Trailer other = (Trailer) obj;
            return mId.equals(other.mId);
        }
        return false;
    }


    /**
     * ContentValues to be inserted into {@link TrailerEntry}'s trailers table.
     * Note that this doesn't contain any info/value of column(trailers.movie_id_key) having foreign key in 'trailers' table!
     *
     * @return ContentValues object containing all the information of this {@link Trailer} object
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(TrailerEntry.COLUMN_TRAILER_ID, mId);
        values.put(TrailerEntry.COLUMN_KEY, mKey);
        values.put(TrailerEntry.COLUMN_NAME, mName);
        values.put(TrailerEntry.COLUMN_SITE, mSite);
        values.put(TrailerEntry.COLUMN_TYPE, mType);
        values.put(TrailerEntry.COLUMN_SIZE, mSize);
        return values;
    }
}
