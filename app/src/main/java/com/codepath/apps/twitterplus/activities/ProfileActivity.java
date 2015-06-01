package com.codepath.apps.twitterplus.activities;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.apps.twitterplus.R;
import com.codepath.apps.twitterplus.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterplus.dialogs.ReplyDialog;
import com.codepath.apps.twitterplus.fragments.TweetsListFragment;
import com.codepath.apps.twitterplus.fragments.UserTimelineFragment;
import com.codepath.apps.twitterplus.models.Tweet;
import com.codepath.apps.twitterplus.models.User;
import com.codepath.apps.twitterplus.utils.TwitterUtil;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends ActionBarActivity implements ReplyDialog.ReplyDialogListener {
    User m_user;
    MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //another user
        m_user = getIntent().getParcelableExtra("user");
        if(m_user==null){//current user
            m_user = TwitterUtil.getCurrentUser();
        }
        if(m_user==null){
            //fetch current user
            Log.d("USER NULL", "current user null");
            return;
        }

        //to-do- check if m_user is null
        getSupportActionBar().setTitle("@" + m_user.getScreenName());
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_ab_twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        populateProfileHeader();

        if(savedInstanceState==null) {
            UserTimelineFragment fragmentUserTimeline = UserTimelineFragment.newInstance(m_user.getScreenName());

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContainer, fragmentUserTimeline);
            ft.commit();
        }
    }

    private void populateProfileHeader(){

        TextView tvFullName = (TextView) findViewById(R.id.tvFullName);
        TextView tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        TextView tvTag = (TextView) findViewById(R.id.tvTag);
        TextView tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        TextView tvFollowing = (TextView) findViewById(R.id.tvFollowing);
        ImageView ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);

        tvFullName.setText(m_user.getName());
        tvScreenName.setText("@" + m_user.getScreenName());
        tvTag.setText(m_user.getTag());
        tvFollowers.setText(m_user.getFollowersCount() + " " + getResources().getString(R.string.followers));
        tvFollowing.setText(m_user.getFollowingCount() + " " + getResources().getString(R.string.following));
        Picasso.with(this).load(m_user.getProfileImageUrl())
                //.fit().centerInside()
                .into(ivProfileImage);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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

    @Override
    public void onFinishReplyDialog(Tweet replyTweet) {
        Log.d("DEBUG", "onFinishReplyDialog: reply is done replyTweet is " + replyTweet.toString());
        //Toast.makeText(this, replyTweet.toString(), Toast.LENGTH_SHORT).show();

        replyTweet.save();
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    @Override
    public void onStartNetworkCall(){
        showProgressBar();
    }

    @Override
    public void onFinishNetworkCall(){
        hideProgressBar();
    }
}
