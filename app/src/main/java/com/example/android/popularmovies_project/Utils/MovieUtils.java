package com.example.android.popularmovies_project.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MovieUtils {

    private static final String LOG_TAG = MovieUtils.class.getSimpleName();
    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private static final String BASE_YOUTUBE_TRAILER_URL = "http://www.youtube.com/watch?v=";
    private static final String BASE_YOUTUBE_TRAILER_THUMBNAIL_URL = "http://img.youtube.com/vi/";
    private static final String POST_YOUTUBE_TRAILER_THUMBNAIL_PATH = "/0.jpg";


    /*
     * Qualities in which the link of the image should come in.
     */
    public static final String IMAGE_QUALITY_W92 = "w92";
    public static final String IMAGE_QUALITY_W154 = "w154";
    public static final String IMAGE_QUALITY_W185 = "w185";
    public static final String IMAGE_QUALITY_W342 = "w342";
    public static final String IMAGE_QUALITY_W500 = "w500";
    public static final String IMAGE_QUALITY_W780 = "w780";
    public static final String IMAGE_QUALITY_ORIGINAL = "original";

    private MovieUtils() {

    }

    /**
     * Converts the date coming from themoviedb api(from "yyyy-MM-dd") to milliseconds
     *
     * @param dateString is the date which needs to be converted
     * @return the date in milliseconds
     */
    public static long toMillis(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        long timeInMillis = 0;
        try {
            Date date = dateFormat.parse(dateString);

            timeInMillis = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return -1;
        }

        return timeInMillis;
    }


    /**
     * @param timeInMillis which needs to be converted into date format.
     * @return the date in string format as example, 19 Nov, 2019.
     */
    public static String toDateString(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("d LLL, yyyy.");
        return formatter.format(new Date(timeInMillis));
    }


    /**
     * Converts the posterPath or backdropPath to Image link (Url actually) to display it wherever needed.
     * It is required that only the quality constants that are defined above should be used in order for this method to work.
     *
     * @param imageQuality   is one of the quality constant defined in {@link MovieUtils} class.
     * @param movieImagePath is either the posterPath or backdropPath which should come from Movie
     * @return the url of the image link
     */
    public static String getImageLink(String imageQuality, String movieImagePath) {
        return BASE_IMAGE_URL + imageQuality + movieImagePath;
    }


    /**
     * Supports youtube only.
     *
     * @param ytKey, the key of {@link com.example.android.popularmovies_project.Models.Trailer}
     * @return the video link of trailer.
     */
    public static String getTrailerLink(String ytKey) {
        return BASE_YOUTUBE_TRAILER_URL + ytKey;
    }


    /**
     * Supports youtube only.
     *
     * @param ytKey, the key of {@link com.example.android.popularmovies_project.Models.Trailer}
     * @return the thumbnail link of trailer.
     */
    public static String getTrailerThumbnailLink(String ytKey) {
        return BASE_YOUTUBE_TRAILER_THUMBNAIL_URL + ytKey + POST_YOUTUBE_TRAILER_THUMBNAIL_PATH;
    }
}
