package com.example.android.popularmovies_project.Utils;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;

public class LayoutUtils {
    /**
     * Use screen width to calculate columns count.
     *
     * @param context
     * @param columnWidthDp
     * @return
     */
    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }

    /**
     * Use parent layout's measured width(available width) to calculate columns count.
     *
     * @param context
     * @param columnWidthDp
     * @param availableWidth
     * @return
     */
    public static int calculateNoOfColumns(Context context, float columnWidthDp, int availableWidth) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float widthDp = availableWidth / displayMetrics.density;
        return (int) (widthDp / columnWidthDp + 0.5);
    }

    /**
     * This will return the darker color than the color provided
     *
     * @param color   of which you want the darker version
     * @param factor, depends how much darker you will get. provide below 1.0f to get darker.(e.g. 0.8)
     * @return the darker version of color of the given color
     */
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);

        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }
}