package com.codepath.apps.twitterplus.activities;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.twitterplus.R;
import com.codepath.apps.twitterplus.models.Tweet;
import com.codepath.apps.twitterplus.models.User;
import com.codepath.apps.twitterplus.tclient.TwitterApplication;
import com.codepath.apps.twitterplus.tclient.TwitterClient;
import com.codepath.apps.twitterplus.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

public class ComposeActivity extends ActionBarActivity {
    private EditText etNewTweet;
    private TwitterClient client;
    private ImageView ivCurrentProfile;
    private TextView tvCurrentFullName;
    private TextView tvCurrentUsername;
    private User m_user;
    private int m_length = 0;
    private MenuItem miCounter;
    private MenuItem miCompose;
    final int MAX_TWEET_LENGTH = 140;
    MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_ab_twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //extract data, this way or get the static current user from TimeLine
        //m_user = (User) getIntent().getParcelableExtra("user");
        m_user = TwitterUtil.getCurrentUser();

        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

        setUpViews();
        setUpListener();
    }

    private void setUpViews() {

        etNewTweet = (EditText) findViewById(R.id.etNewTweet);
        ivCurrentProfile = (ImageView) findViewById(R.id.ivCurrentProfile);
        tvCurrentFullName = (TextView) findViewById(R.id.tvCurrentFullName);
        tvCurrentUsername = (TextView) findViewById(R.id.tvCurrentUsername);

        if (m_user != null) {
            tvCurrentFullName.setText(m_user.getName());
            tvCurrentUsername.setText("@" + m_user.getScreenName());

            Picasso.with(this).load(m_user.getProfileImageUrl())
                    .into(ivCurrentProfile);
        } else {//just in case something went wrong with getting current user, do not let app crash
            RelativeLayout rluser = (RelativeLayout) findViewById(R.id.rluser);
            rluser.setVisibility(View.INVISIBLE);
            Log.d("DEBUG", "user is null");
        }
    }

    private void setUpListener() {

        Log.d("DEBUG", "m_length is " + m_length);

        etNewTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                m_length = s.toString().length();
                int remaining = MAX_TWEET_LENGTH - m_length;
                miCounter.setTitle(String.valueOf(remaining));
                if (remaining < 0) {
                    miCompose.setEnabled(false);
                } else {
                    miCompose.setEnabled(true);
                }
            }
        });
    }

    public void onSubmitTweet(MenuItem mi) {

        if (!TwitterUtil.isThereNetworkConnection(this)) {
            Log.e("ERROR", "no network");
            Toast.makeText(this, getResources().getString(R.string.no_network), Toast.LENGTH_LONG).show();
        } else if (m_length > MAX_TWEET_LENGTH) {

            Toast.makeText(this, getResources().getString(R.string.tweet_too_long), Toast.LENGTH_LONG).show();
        } else {

            String newTweetBody = etNewTweet.getText().toString();

            showProgressBar();

            client.postTweet(newTweetBody, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    hideProgressBar();
                    Log.d("DEBUG", jsonObject.toString());
                    Toast.makeText(ComposeActivity.this, getResources().getString(R.string.success_on_posting_tweet), Toast.LENGTH_SHORT).show();

                    Tweet newTweet = Tweet.fromJson(jsonObject);
                    Intent data = new Intent();
                    data.putExtra("tweet", newTweet);
                    setResult(RESULT_OK, data); // set result code and bundle data for response
                    finish(); // closes the activity, pass data to parent

                    /*Intent i = new Intent(ComposeActivity.this, TimelineActivity.class);
                    i.putExtra("dummy", 1);
                    startActivity(i);*/
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    hideProgressBar();
                    Log.e("ERROR", errorResponse.toString());
                    Toast.makeText(ComposeActivity.this, getResources().getString(R.string.failed_to_tweet), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose, menu);
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
        miCounter = menu.findItem(R.id.miCounter);
        miCompose = menu.findItem(R.id.miCompose);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);

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
}
