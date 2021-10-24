package com.example.android.popularmovies_project.Utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.android.popularmovies_project.Models.Movie;
import com.example.android.popularmovies_project.Models.Review;
import com.example.android.popularmovies_project.Models.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static int totalPages;
    private static int currentPage;

    private QueryUtils() {

    }

    /**
     * Gets the movies from themoviedb api.
     *
     * @param requestUrl is the url for the api call to themoviedb server
     * @return the list of {@link Movie}
     */
    public static List<Movie> fetchMovieList(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the Http request: fetching movies");
        }

        List<Movie> movies = extractMoviesFromJson(jsonResponse);
        return movies;
    }

    /**
     * Gets the trailers from themoviedb api.
     *
     * @param requestUrl is the url for the api call to themoviedb server
     * @return the list of {@link Trailer}
     */
    public static List<Trailer> fetchTrailerList(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the Http request: fetching trailers");
        }

        List<Trailer> trailers = extractTrailersFromJson(jsonResponse);
        return trailers;
    }

    /**
     * Gets the reviews from themoviedb api.
     *
     * @param requestUrl is the url for the api call to themoviedb server
     * @return the list of {@link Review}
     */
    public static List<Review> fetchReviewList(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the Http request: fetching reviews");
        }

        List<Review> reviews = extractReviewsFromJson(jsonResponse);
        return reviews;
    }

    private static URL createUrl(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the movies JSON results");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (inputStream != null) {
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);

                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Movie> extractMoviesFromJson(String moviesJson) {
        if (TextUtils.isEmpty(moviesJson)) {
            return null;
        }

        List<Movie> movieList = new ArrayList<Movie>();

        try {
            JSONObject rootJson = new JSONObject(moviesJson);
            totalPages = rootJson.getInt("total_pages");
            currentPage = rootJson.getInt("page");

            JSONArray results = rootJson.getJSONArray("results");
            for (int i=0; i < results.length(); i++) {
                JSONObject movieObj = results.getJSONObject(i);

                int id = movieObj.getInt("id");
                String posterPath = movieObj.getString("poster_path");
                String backdropPath = movieObj.getString("backdrop_path");
                String originalLanguage = movieObj.getString("original_language");
                double voteAverage = movieObj.getDouble("vote_average");
                String overview = movieObj.getString("overview");
                String title = movieObj.getString("title");

                String releaseDate = movieObj.optString("release_date", null);
                long releaseDateMillis = MovieUtils.toMillis(releaseDate);

                Movie movie = new Movie(id, posterPath, backdropPath, originalLanguage, title, voteAverage, overview, releaseDateMillis);
                movieList.add(movie);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }

        Log.d("MovieApiSize", "Current Page: " + getCurrentPage() + " Length: " + movieList.size());
        return movieList;
    }

    private static List<Trailer> extractTrailersFromJson(String trailersJson) {
        if (TextUtils.isEmpty(trailersJson)) {
            return null;
        }

        List<Trailer> trailerList = new ArrayList<Trailer>();
        try {
            JSONObject rootJson = new JSONObject(trailersJson);

            JSONArray results = rootJson.getJSONArray("results");
            for (int i=0; i < results.length(); i++) {
                JSONObject trailerObj = results.getJSONObject(i);

                String id = trailerObj.getString("id");
                String key = trailerObj.getString("key");
                String name = trailerObj.getString("name");
                String site = trailerObj.getString("site");
                int size = trailerObj.getInt("size");
                String type = trailerObj.getString("type");

                Trailer trailer = new Trailer(id, key, name, site, size, type);
                trailerList.add(trailer);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }

        return trailerList;
    }

    private static List<Review> extractReviewsFromJson(String reviewsJson) {
        if (TextUtils.isEmpty(reviewsJson)) {
            return null;
        }

        List<Review> reviewList = new ArrayList<Review>();
        try {
            JSONObject rootJson = new JSONObject(reviewsJson);

            JSONArray results = rootJson.getJSONArray("results");
            for (int i=0; i < results.length(); i++) {
                JSONObject reviewObj = results.getJSONObject(i);

                String author = reviewObj.getString("author");
                String content = reviewObj.getString("content");
                String id = reviewObj.getString("id");
                String url = reviewObj.getString("url");

                Review review = new Review(author, content, id, url);
                reviewList.add(review);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }

        return reviewList;
    }


    public static int getTotalPages() {
        return totalPages;
    }

    public static int getCurrentPage() {
        return currentPage;
    }
}