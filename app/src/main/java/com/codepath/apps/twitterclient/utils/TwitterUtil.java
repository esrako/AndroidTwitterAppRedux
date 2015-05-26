package com.codepath.apps.twitterclient.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateUtils;
import android.util.Log;

import com.codepath.apps.twitterclient.models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ekucukog on 5/21/2015.
 */
public class TwitterUtil {

    //types of refreshes on a page
    public static final int NEW_PAGE = 1; // blank page needs to be populated
    public static final int SWIPE = 2; // not blank, needs to refreshed, swipecontainer
    public static final int SCROLL = 3; // not blank, need more older items at the bottom
    public static final int REFRESH_PAGE = 4; //not blank, needs to be refreshed

    private static User currentUser = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        TwitterUtil.currentUser = currentUser;
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL /*DateUtils.FORMAT_ABBREV_RELATIVE*/).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return relativeDate;
    }

    // getNicerTime("Mon Apr 01 21:16:23 +0000 2014");
    public static String getNicerTime(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        String myFormat = "hh:mm aa   dd MMM yy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String nicerDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            sf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
            nicerDate = sf.format(new Date(dateMillis));
            Log.d("DEBUG", "nicerDate: " + nicerDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return nicerDate;
    }

    public static boolean isThereNetworkConnection(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
