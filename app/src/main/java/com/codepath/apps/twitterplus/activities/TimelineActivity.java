package com.codepath.apps.twitterplus.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.twitterplus.R;
import com.codepath.apps.twitterplus.adapters.SmartFragmentStatePagerAdapter;
import com.codepath.apps.twitterplus.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterplus.dialogs.ComposeDialog;
import com.codepath.apps.twitterplus.dialogs.ReplyDialog;
import com.codepath.apps.twitterplus.fragments.HomeTimelineFragment;
import com.codepath.apps.twitterplus.fragments.MentionsTimelineFragment;
import com.codepath.apps.twitterplus.fragments.TweetsListFragment;
import com.codepath.apps.twitterplus.listeners.EndlessScrollListener;
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

public class TimelineActivity extends ActionBarActivity implements TweetsListFragment.TweetsListFragmentListener, ReplyDialog.ReplyDialogListener, ComposeDialog.ComposeDialogListener {


    private TweetsListFragment fragmentTweetsList;
    private TwitterClient client;
    private SwipeRefreshLayout swipeContainer;
    private SmartFragmentStatePagerAdapter adapterViewPager;
    private ViewPager viewPager;
    MenuItem miActionProgressItem;

    final int REQUEST_CODE_COMPOSE = 10;
    final int REQUEST_CODE_DETAIL = 20;
    private Tweet m_tweet = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

        fetchCurrentUserInfo();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_ab_twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        // Get the view pager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        // Set the view pager adapter for the pager
        adapterViewPager = new TweetsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        // Find the sliding tabstrip
        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the pager tabs to the view
        tabStrip.setViewPager(viewPager);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    public void onComposeTweet(MenuItem mi) {

        /* to-do: instead of this
         * put current user to db when there is network
         * and fetch from db when no network
         */
        if (TwitterUtil.getCurrentUser() == null)
            fetchCurrentUserInfo();

        Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
        startActivityForResult(i, REQUEST_CODE_COMPOSE);

        //showComposeDialog();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_COMPOSE && resultCode == RESULT_OK) {
            m_tweet = (Tweet) data.getParcelableExtra("tweet");

            if (m_tweet == null) {
                Log.e("ERROR", "onActivityResult tweet received is null");
                //Toast.makeText(this, "compose activity returned a null tweet", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("DEBUG", "onActivityResult got the tweet back " + m_tweet.toString());
                //Toast.makeText(this, m_tweet.toString(), Toast.LENGTH_SHORT).show();

                //Before refreshing the page, we put the just composed tweet into the adapter
                //because refreshing the page might take time
                m_tweet.save();

                TweetsListFragment fragment = (TweetsListFragment)adapterViewPager.getRegisteredFragment(0/*viewPager.getCurrentItem()*/);
                if(fragment!=null){
                    TweetsArrayAdapter taa = fragment.getAdapter();
                    if(taa!=null){
                        taa.insert(m_tweet, 0);
                    }else{
                        Log.d("DEBUG", "adapter is null");
                    }
                }
                else{
                    Log.d("DEBUG", "fragment is null");
                }

                //refresh page
                //populateTimeline(TwitterUtil.REFRESH_PAGE, 1);
            }
        } else if (requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            Tweet tweetChanged = (Tweet) data.getParcelableExtra("updatedtweet");
            Tweet replyTweet = (Tweet) data.getParcelableExtra("replytweet");

            if (tweetChanged == null && replyTweet == null) {
                Log.e("ERROR", "onActivityResult updated tweet received is null");
                //Toast.makeText(this, "detail activity returned a null changed tweet and reply tweet - OK ", Toast.LENGTH_SHORT).show();
            } else {

                if (tweetChanged != null) {
                    Log.i("DEBUG", "onActivityResult got the updated tweet back " + tweetChanged.toString());
                    //Toast.makeText(this, m_tweet.toString(), Toast.LENGTH_SHORT).show();

                    //tweetChanged.save();

                    TweetsListFragment fragmentHome = (TweetsListFragment)adapterViewPager.getRegisteredFragment(0/*viewPager.getCurrentItem()*/);
                    fragmentHome.updateItem(tweetChanged);

                    TweetsListFragment fragmentMentions = (TweetsListFragment)adapterViewPager.getRegisteredFragment(1/*viewPager.getCurrentItem()*/);
                    fragmentMentions.updateItem(tweetChanged);

                }

                if (replyTweet != null) {
                    Log.i("DEBUG", "onActivityResult got the reply tweet back " + replyTweet.toString());
                    //Toast.makeText(this, replyTweet.toString(), Toast.LENGTH_SHORT).show();

                    replyTweet.save();

                    TweetsListFragment fragment = (TweetsListFragment)adapterViewPager.getRegisteredFragment(0/*viewPager.getCurrentItem()*/);
                    if(fragment!=null){
                        TweetsArrayAdapter taa = fragment.getAdapter();
                        if(taa!=null){
                            taa.insert(replyTweet, 0);
                        }else{
                            Log.d("DEBUG", "adapter is null");
                        }
                        //refresh page
                        //fragment.populateTimeline(TwitterUtil.REFRESH_PAGE, 1);
                    }
                    else{
                        Log.d("DEBUG", "fragment is null");
                    }
                }

                //refresh page
                //populateTimeline(TwitterUtil.REFRESH_PAGE, 1);
            }
        }
    }

    @Override
    public void onStartNetworkCall(){
        showProgressBar();
    }

    @Override
    public void onFinishNetworkCall(){
        hideProgressBar();
    }

    @Override
    public void onFinishReplyDialog(Tweet replyTweet) {
        Log.d("DEBUG", "onFinishReplyDialog: reply is done replyTweet is " + replyTweet.toString());
        //Toast.makeText(this, replyTweet.toString(), Toast.LENGTH_SHORT).show();

        replyTweet.save();

        TweetsListFragment fragment = (TweetsListFragment)adapterViewPager.getRegisteredFragment(0/*viewPager.getCurrentItem()*/);
        if(fragment!=null){
            TweetsArrayAdapter taa = fragment.getAdapter();
            if(taa!=null){
                taa.insert(replyTweet, 0);
            }else{
                Log.d("DEBUG", "adapter is null");
            }
            //refresh page
            fragment.populateTimeline(TwitterUtil.REFRESH_PAGE, 1);
        }
        else{
            Log.d("DEBUG", "fragment is null");
        }
    }

    @Override
    public void onFinishComposeDialog(Tweet tweet) {

        if (tweet == null) {
            Log.d("DEBUG", "User closed the dialog without submitting tweet - tweet is null - OK");
            //Toast.makeText(this, "Dialog closed without submitting a tweet", Toast.LENGTH_SHORT).show();

        } else {
            Log.d("DEBUG", "onFinishComposeDialog: new tweet is " + tweet.toString());
            //Toast.makeText(this, tweet.toString(), Toast.LENGTH_SHORT).show();

            tweet.save();

            TweetsListFragment fragment = (TweetsListFragment)adapterViewPager.getRegisteredFragment(0/*viewPager.getCurrentItem()*/);
            if(fragment!=null){
                TweetsArrayAdapter taa = fragment.getAdapter();
                if(taa!=null){
                    taa.insert(tweet, 0);
                }else{
                    Log.d("DEBUG", "adapter is null");
                }
                //refresh page
                fragment.populateTimeline(TwitterUtil.REFRESH_PAGE, 1);
            }
            else{
                Log.d("DEBUG", "fragment is null");
            }
        }
    }

    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialog composeDialog = ComposeDialog.newInstance(getResources().getString(R.string.title_dialog_compose));
        composeDialog.show(fm, "fragment_reply");
    }

    //return the order of the fragments in the view pager
    public class TweetsPagerAdapter extends SmartFragmentStatePagerAdapter{
        private String tabTitles[] = {"Home", "Mentions"};

        //adapter gets the manager to insert or remove fragment from activity
        public TweetsPagerAdapter(FragmentManager fm){
            super(fm);
        }

        //the order and creation of the fragment within the pager
        @Override
        public Fragment getItem(int position) {
            if(position==0) {
                return new HomeTimelineFragment();
            }
            else if(position ==1){
                return new MentionsTimelineFragment();
            }
            else{
                return null;
            }
        }

        //return the tab title
        public CharSequence getPageTitle(int position){
            return tabTitles[position];
        }

        //how many frags?
        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }

    public void onShowProfile(MenuItem menuItem){

        if (TwitterUtil.getCurrentUser() == null)
            fetchCurrentUserInfo();

        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

    private void fetchCurrentUserInfo() {

        if (!TwitterUtil.isThereNetworkConnection(this)) {
            Log.e("ERROR", "no network - couldn't fetch current user info");
            //Toast.makeText(TimelineActivity.this, R.string.no_network, Toast.LENGTH_LONG).show();
        } else {

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