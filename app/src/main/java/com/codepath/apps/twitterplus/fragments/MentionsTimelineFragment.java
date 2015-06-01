package com.codepath.apps.twitterplus.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.codepath.apps.twitterplus.R;
import com.codepath.apps.twitterplus.models.Tweet;
import com.codepath.apps.twitterplus.models.User;
import com.codepath.apps.twitterplus.tclient.TwitterApplication;
import com.codepath.apps.twitterplus.tclient.TwitterClient;
import com.codepath.apps.twitterplus.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ekucukog on 5/29/2015.
 */
public class MentionsTimelineFragment extends TweetsListFragment {

    private TwitterClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

        populateTimeline(TwitterUtil.NEW_PAGE, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    //Send an API request to get the timeline json
    //fill the listview by creating tweets objects from json
    public void populateTimeline(int refType, long offset) {

        if (!TwitterUtil.isThereNetworkConnection(getActivity())) {
            Log.e("ERROR", "no network");
            if (refType == TwitterUtil.SWIPE) {
                Log.d("DEBUG", "swipe");
                swipeContainer.setRefreshing(false);
            }
            if (refType != TwitterUtil.SCROLL) {
                Log.d("DEBUG", "not a scroll");
                Toast.makeText(getActivity(), R.string.no_network, Toast.LENGTH_LONG).show();
            }
            if (refType == TwitterUtil.NEW_PAGE) {

                Log.d("DEBUG", "new page");
                //first time open the page(blank page) & no network -> load tweets from local db
                usingLocalData = true;
                //addAll(Tweet.getAllFromDB());
                getAdapter().addAll(Tweet.getAllFromDB());
                Log.d("DEBUG adapter", getAdapter().toString());
            }
        } else {

            Log.d("DEBUG", "Entering populateTimeLine");

            //if we were using local db to populate page before and
            //now we have network and trying to do scroll
            //just refresh the page instead
            if (usingLocalData && refType == TwitterUtil.SCROLL) {
                refType = TwitterUtil.REFRESH_PAGE;
            }

            final int refreshType = refType;

            //TweetsListFragmentListener listener = (TweetsListFragmentListener) getActivity();
            //listener.onStartNetworkCall();
            client.getMentionsTimeLine(refreshType, offset, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                    //TweetsListFragmentListener listener = (TweetsListFragmentListener) getActivity();
                    //listener.onFinishNetworkCall();
                    Log.d("DEBUG", jsonArray.toString());

                    usingLocalData = false;
                    if (refreshType == TwitterUtil.SWIPE) {
                        // Now we call setRefreshing(false) to signal refresh has finished
                        swipeContainer.setRefreshing(false);
                    }
                    if (refreshType != TwitterUtil.SCROLL) {
                        //clear adapter
                        getAdapter().clear();
                        //clear dbs
                        new Delete().from(Tweet.class).execute();
                        new Delete().from(User.class).execute();
                    }
                    ArrayList<Tweet> temps = Tweet.fromJsonArray(jsonArray);
                    if(temps.size()>0) {
                        earliestID = temps.get(temps.size() - 1).getUid();
                        getAdapter().addAll(temps);
                    }
                    Log.d("DEBUG adapter", getAdapter().toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    //TweetsListFragmentListener listener = (TweetsListFragmentListener) getActivity();
                    //listener.onFinishNetworkCall();
                    Log.e("ERROR", errorResponse.toString());
                    if (refreshType == TwitterUtil.SWIPE) {
                        swipeContainer.setRefreshing(false);
                    }
                    Toast.makeText(getActivity(), getResources().getString(R.string.failed_to_fetch_tweets), Toast.LENGTH_LONG).show();
                    if (refreshType == TwitterUtil.NEW_PAGE) {

                        //first time open the page(blank page) & call fails -> load tweets from local db
                        getAdapter().addAll(Tweet.getAllFromDB());
                        Log.d("DEBUG", getAdapter().toString());
                    }
                }
            });
        }
    }
}
