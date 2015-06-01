package com.codepath.apps.twitterplus.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.twitterplus.R;
import com.codepath.apps.twitterplus.activities.DetailActivity;
import com.codepath.apps.twitterplus.activities.TimelineActivity;
import com.codepath.apps.twitterplus.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterplus.listeners.EndlessScrollListener;
import com.codepath.apps.twitterplus.models.Tweet;
import com.codepath.apps.twitterplus.tclient.TwitterApplication;
import com.codepath.apps.twitterplus.tclient.TwitterClient;
import com.codepath.apps.twitterplus.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ekucukog on 5/29/2015.
 */
public abstract class TweetsListFragment extends Fragment implements TweetsArrayAdapter.MyFavListener {

    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private ListView lvTweets;
    protected SwipeRefreshLayout swipeContainer;
    protected boolean usingLocalData = false;// means page is populated using local DB instead of Twitter API call
    private TwitterClient client;

    final int REQUEST_CODE_COMPOSE = 10;
    final int REQUEST_CODE_DETAIL = 20;

    protected static long earliestID = -1;


    //inflation logic
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweets_list, container, false);

        lvTweets = (ListView) v.findViewById(R.id.lvTweets);
        // Connect adapter to list view
        lvTweets.setAdapter(aTweets);
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);


        //set up endless scroll listener for listview
        setUpListenerForListView();
        setUpSwipeContainer();

        return v;
    }


    //creation lifecycle event
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the arraylist (data source)
        tweets = new ArrayList<>();
        // Construct the adapter from data source
        aTweets = new TweetsArrayAdapter(getActivity(), tweets);

        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

    }

    public interface TweetsListFragmentListener {
        void onStartNetworkCall();
        void onFinishNetworkCall();
    }

/*
    public void addAll(List<Tweet> tweetss){
        aTweets.addAll(tweetss);
    }
*/

    public TweetsArrayAdapter getAdapter(){
        return aTweets;
    }

    public void updateItem(Tweet tweetChanged){
        //find and update the item
        Tweet tweetToBeUpdated = Tweet.findTweet(tweets, tweetChanged.getUid());
        if (tweetToBeUpdated != null) {
            tweetToBeUpdated.setFavorite_count(tweetChanged.getFavorite_count());
            tweetToBeUpdated.setFavorited(tweetChanged.isFavorited());
            tweetToBeUpdated.setRetweet_count(tweetChanged.getRetweet_count());
            tweetToBeUpdated.setRetweeted(tweetChanged.isRetweeted());
            tweetToBeUpdated.setCurrent_user_retweet_id_str(tweetChanged.getCurrent_user_retweet_id_str());
            aTweets.notifyDataSetChanged();
        }

    }

    private void setUpSwipeContainer() {
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                populateTimeline(TwitterUtil.SWIPE, 1);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    private void setUpListenerForListView() {
        // Attach the listener to the AdapterView onCreate
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                customLoadMoreDataFromApi();
            }
        });

        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(getActivity(), DetailActivity.class);
                i.putExtra("tweet", (Tweet) parent.getItemAtPosition(position));
                getActivity().startActivityForResult(i, REQUEST_CODE_DETAIL);
            }
        });
    }

    // Append more data into the adapter
    public void customLoadMoreDataFromApi() {

        //make sure to put -1, bc twitter sends back those equal or less
        populateTimeline(TwitterUtil.SCROLL, earliestID - 1);
    }

    public abstract void populateTimeline(int refType, long offset);

    public void onFavCall(final Tweet tweet) {


        long tweetID = tweet.getUid();
        /*if (!tweet.isFavorited()) {//it is not favorited yet, go and favorite

            int newFC = tweet.getFavorite_count() + 1;

            client.makeFavorite(tweetID, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.d("DEBUG", jsonObject.toString());
                    int newFC = tweet.getFavorite_count() + 1;
                    tweet.setFavorited(true);
                    tweet.setFavorite_count(newFC);
                    Toast.makeText(getActivity(), R.string.success_on_favorite, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("ERROR", errorResponse.toString());
                    //viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey, 0, 0, 0);
                    //viewHolder.tvFavsAction.setText("" + tweet.getFavorite_count());
                    Toast.makeText(getActivity(), R.string.failed_to_favorite, Toast.LENGTH_LONG).show();
                }
            });
        }*/
    }
}
