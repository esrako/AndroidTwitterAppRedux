package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterclient.listeners.EndlessScrollListener;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.tclient.TwitterApplication;
import com.codepath.apps.twitterclient.tclient.TwitterClient;
import com.codepath.apps.twitterclient.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TimelineActivity extends ActionBarActivity {
    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private ListView lvTweets;
    final int REQUEST_CODE_COMPOSE = 10;
    final int REQUEST_CODE_DETAIL = 20;
    private Tweet m_tweet = null;
    private SwipeRefreshLayout swipeContainer;
    private boolean usingLocalData = false;// means page is populated using local DB instead of Twitter API call

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        lvTweets = (ListView) findViewById(R.id.lvTweets);
        // Create the arraylist (data source)
        tweets = new ArrayList<>();
        // Construct the adapter from data source
        aTweets = new TweetsArrayAdapter(this, tweets);
        // Connect adapter to list view
        lvTweets.setAdapter(aTweets);
        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

        fetchCurrentUserInfo();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_ab_twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //set up endless scroll listener for listview
        setUpListenerForListView();
        setUpSwipeContainer();

        populateTimeline(TwitterUtil.NEW_PAGE, 1);
    }

    private void setUpSwipeContainer(){
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
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

    private void setUpListenerForListView(){
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

                Intent i = new Intent(TimelineActivity.this, DetailActivity.class);
                i.putExtra("tweet", (Tweet)parent.getItemAtPosition(position));
                startActivityForResult(i, REQUEST_CODE_DETAIL);
            }
        });
    }

    // Append more data into the adapter
    public void customLoadMoreDataFromApi() {

        //make sure to put -1, bc twitter sends back those equal or less
        populateTimeline(TwitterUtil.SCROLL, Tweet.getEarliestID()-1);
    }

    //Send an API request to get the timeline json
    //fill the listview by creating tweets objects from json
    private void populateTimeline(int refType, long offset){

        if( !TwitterUtil.isThereNetworkConnection(this)){
            Log.e("ERROR", "no network");
            if (refType == TwitterUtil.SWIPE) {
                swipeContainer.setRefreshing(false);
            }
            if( refType != TwitterUtil.SCROLL){
                Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
            }
            if( refType == TwitterUtil.NEW_PAGE) {

                //first time open the page(blank page) & no network -> load tweets from local db
                usingLocalData = true;
                aTweets.addAll(Tweet.getAllFromDB());
                Log.d("DEBUG", aTweets.toString());
            }
        }
        else {

            Log.d("DEBUG", "Entering populateTimeLine");

            //if we were using local db to populate page before and
            //now we have network and trying to do scroll
            //just refresh the page instead
            if(usingLocalData && refType== TwitterUtil.SCROLL){
                refType = TwitterUtil.REFRESH_PAGE;
            }

            final int refreshType = refType;

            client.getHomeTimeLine(refreshType, offset, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                    Log.d("DEBUG", jsonArray.toString());

                    usingLocalData = false;
                    if (refreshType == TwitterUtil.SWIPE) {
                        // Now we call setRefreshing(false) to signal refresh has finished
                        swipeContainer.setRefreshing(false);
                    }
                    if( refreshType != TwitterUtil.SCROLL) {
                        //clear adapter
                        aTweets.clear();
                        //clear dbs
                        new Delete().from(Tweet.class).execute();
                        new Delete().from(User.class).execute();
                    }
                    aTweets.addAll(Tweet.fromJsonArray(jsonArray, refreshType));
                    Log.d("DEBUG", aTweets.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("ERROR", errorResponse.toString());
                    if (refreshType == TwitterUtil.SWIPE) {
                        swipeContainer.setRefreshing(false);
                    }
                    Toast.makeText(TimelineActivity.this, getResources().getString(R.string.failed_to_fetch_tweets), Toast.LENGTH_LONG).show();
                    if( refreshType == TwitterUtil.NEW_PAGE) {

                        //first time open the page(blank page) & call fails -> load tweets from local db
                        aTweets.addAll(Tweet.getAllFromDB());
                        Log.d("DEBUG", aTweets.toString());
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void onComposeTweet(MenuItem mi){

        /* to-do: instead of this
         * put current user to db when there is network
         * and fetch from db when no network
         */
        if(TwitterUtil.getCurrentUser()==null)
            fetchCurrentUserInfo();

        Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
        startActivityForResult(i, REQUEST_CODE_COMPOSE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode== REQUEST_CODE_COMPOSE && resultCode == RESULT_OK){
            m_tweet = (Tweet) data.getParcelableExtra("tweet");

            if(m_tweet==null){
                Log.e("ERROR", "onActivityResult tweet received is null");
                Toast.makeText(this, "compose activity returned a null tweet", Toast.LENGTH_SHORT).show();
            }
            else{
                Log.i("DEBUG", "onActivityResult got the tweet back " + m_tweet.toString());
                Toast.makeText(this, m_tweet.toString(), Toast.LENGTH_SHORT).show();

                //Before refreshing the page, we put the just composed tweet into the adapter
                //because refreshing the page might take time
                tweets.add(0, m_tweet);
                m_tweet.save();
                aTweets.notifyDataSetChanged();

                //refresh page
                populateTimeline(TwitterUtil.REFRESH_PAGE, 1);
            }
        }
        else if(requestCode== REQUEST_CODE_DETAIL && resultCode == RESULT_OK){
            Tweet tweetChanged = (Tweet) data.getParcelableExtra("updatedtweet");
            Tweet replyTweet = (Tweet) data.getParcelableExtra("replytweet");

            if(tweetChanged==null && replyTweet==null){
                Log.e("ERROR", "onActivityResult updated tweet received is null");
                Toast.makeText(this, "detail activity returned a null changed tweet and reply tweet - OK ", Toast.LENGTH_SHORT).show();
            }
            else{

                if(tweetChanged!=null) {
                    Log.i("DEBUG", "onActivityResult got the updated tweet back " + tweetChanged.toString());
                    //Toast.makeText(this, m_tweet.toString(), Toast.LENGTH_SHORT).show();

                    //tweetChanged.save();

                    //find and update the item
                    Tweet tweetToBeUpdated = Tweet.findTweet(tweets, tweetChanged.getUid());
                    if (tweetToBeUpdated != null) {
                        tweetToBeUpdated.setFavorite_count(tweetChanged.getFavorite_count());
                        tweetToBeUpdated.setFavorited(tweetChanged.isFavorited());
                        tweetToBeUpdated.setRetweet_count(tweetChanged.getRetweet_count());
                        tweetToBeUpdated.setRetweeted(tweetChanged.isRetweeted());

                        aTweets.notifyDataSetChanged();
                    }
                }

                if(replyTweet!=null){
                        Log.i("DEBUG", "onActivityResult got the reply tweet back " + replyTweet.toString());
                        Toast.makeText(this, replyTweet.toString(), Toast.LENGTH_SHORT).show();

                        tweets.add(0, replyTweet);
                        replyTweet.save();
                        aTweets.notifyDataSetChanged();
                }

                //refresh page
                //populateTimeline(TwitterUtil.REFRESH_PAGE, 1);
            }
        }
    }

    private void fetchCurrentUserInfo(){

        if( !TwitterUtil.isThereNetworkConnection(this)){
            Log.e("ERROR", "no network - couldn't fetch current user info");
            //Toast.makeText(TimelineActivity.this, R.string.no_network, Toast.LENGTH_LONG).show();
        }
        else {

            client.getCurrentUserInfo(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.d("DEBUG", jsonObject.toString());
                    TwitterUtil.setCurrentUser(User.fromJson(jsonObject));
                    Log.d("DEBUG", "current user fetched: " + TwitterUtil.getCurrentUser().toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("ERROR", errorResponse.toString());
                    //Toast.makeText(TimelineActivity.this, "Failed to get current user info", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}