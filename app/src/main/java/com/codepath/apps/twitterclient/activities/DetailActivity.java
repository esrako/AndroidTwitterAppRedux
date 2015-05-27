package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.dialogs.ReplyDialog;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.tclient.TwitterApplication;
import com.codepath.apps.twitterclient.tclient.TwitterClient;
import com.codepath.apps.twitterclient.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

public class DetailActivity extends ActionBarActivity implements ReplyDialog.ReplyDialogListener {

    private TwitterClient client;
    private Tweet m_tweet;
    private Tweet m_newTweet = null;

    private ImageView ivProfileImage;
    private TextView tvFullName;
    private TextView tvUsername;
    private TextView tvBody;
    private ImageView ivExtraImage;
    private VideoView vvExtravideo;
    private TextView tvTimestamp;
    private TextView tvRetweetCount;
    private TextView tvFavCount;
    private TextView tvReplyAction;
    private TextView tvRetweetAction;
    private TextView tvFavsAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_ab_twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

        m_tweet = (Tweet) getIntent().getParcelableExtra("tweet");
        Log.d("DEBUG", "got the following tweet from parent: " + m_tweet.toString());

        setUpViews();
        setUpReplyButtonListener();
        setUpRetweetButtonListener();
        setUpFavoriteButtonListener();
    }

    private void setUpViews(){
        ivProfileImage =(ImageView) findViewById(R.id.ivProfileImage);
        tvFullName = (TextView) findViewById(R.id.tvFullName);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvBody = (TextView) findViewById(R.id.tvBody);
        ivExtraImage = (ImageView) findViewById(R.id.ivExtraImage);
        //vvExtravideo = (VideoView) findViewById(R.id.vvExtraVideo);
        tvTimestamp = (TextView) findViewById(R.id.tvTimestamp);
        tvRetweetCount = (TextView) findViewById(R.id.tvRetweetCount);
        tvFavCount = (TextView) findViewById(R.id.tvFavCount);
        tvReplyAction = (TextView) findViewById(R.id.tvReplyAction);
        tvRetweetAction = (TextView) findViewById(R.id.tvRetweetAction);
        tvFavsAction = (TextView) findViewById(R.id.tvFavsAction);

        ivProfileImage.setImageResource(0);
        Picasso.with(this).
                load(m_tweet.getUser().getProfileImageUrl())
                .into(ivProfileImage);

        tvFullName.setText(m_tweet.getUser().getName());
        tvUsername.setText("@" + m_tweet.getUser().getScreenName());
        tvBody.setText(m_tweet.getBody());

        if(m_tweet.getMediatype().equals("photo")) {


            ivExtraImage.setImageResource(0);
            ivExtraImage.setVisibility(View.VISIBLE);
            Picasso.with(this).
                    load(m_tweet.getMediaurl())
                    .fit().centerInside()
                    .placeholder(R.drawable.placeholder)
                    .into(ivExtraImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.i("DEBUG", getResources().getString(R.string.image_load_succeded));
                            //Toast.makeText(DetailActivity.this, "Loaded", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            Log.i("DEBUG", getResources().getString(R.string.image_load_failed));
                            Toast.makeText(DetailActivity.this, getResources().getString(R.string.image_load_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        tvTimestamp.setText(TwitterUtil.getNicerTime(m_tweet.getCreatedAt()));
        tvRetweetCount.setText(m_tweet.getRetweet_count() + " " + getResources().getString(R.string.retweets_label));
        tvFavCount.setText(m_tweet.getFavorite_count()+ "  " + getResources().getString(R.string.favorites_label));

        if(m_tweet.isFavorited()){
            Log.d("DEBUG", "this tweet is a favorite");
            tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow, 0, 0, 0);
        }

        if(m_tweet.isRetweeted()){
            Log.d("DEBUG", "this tweet is a retweeted");
            tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green, 0, 0, 0);
        }
    }

    private void setUpReplyButtonListener(){
        tvReplyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to reply");
                showReplyDialog();
            }
        });
    }

    private void setUpRetweetButtonListener(){
        tvRetweetAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to retweet");

                if( !TwitterUtil.isThereNetworkConnection(DetailActivity.this)){
                    Log.e("ERROR", "no network");
                    Toast.makeText(DetailActivity.this, getResources().getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
                else {

                    long tweetID = m_tweet.getUid();
                    String retweetIDstr = m_tweet.getCurrent_user_retweet_id_str();

                    if(!m_tweet.isRetweeted()) {//not ret
                    // weeted before, retweet now

                        tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green,0,0,0);
                        int newRC = m_tweet.getRetweet_count() + 1;
                        tvRetweetCount.setText(newRC+ "  " + getResources().getString(R.string.retweets_label));

                        client.retweet(tweetID, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                Log.d("DEBUG", jsonObject.toString());

                                Tweet updatedTweet = Tweet.fromJson(jsonObject);
                                m_tweet.setCurrent_user_retweet_id_str(updatedTweet.getCurrent_user_retweet_id_str());

                                tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green, 0, 0, 0);
                                int newRC = m_tweet.getRetweet_count() + 1;
                                tvRetweetCount.setText(newRC+ "  " + getResources().getString(R.string.retweets_label));
                                m_tweet.setRetweeted(true);
                                m_tweet.setRetweet_count(newRC);

                                Toast.makeText(DetailActivity.this, getResources().getString(R.string.success_on_retweet), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.e("ERROR", errorResponse.toString());
                                tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_grey, 0, 0, 0);
                                tvRetweetCount.setText(m_tweet.getRetweet_count() + "  " + getResources().getString(R.string.retweets_label));

                                Toast.makeText(DetailActivity.this, getResources().getString(R.string.failed_to_retweet), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else if (retweetIDstr!=null){//was already retweeted, now destroy retweet

                        Log.d("DEBUG", "retweet id str: " + retweetIDstr);
                        tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_grey,0,0,0);
                        int newRC = m_tweet.getRetweet_count() - 1;
                        tvRetweetCount.setText(newRC+ "  " + getResources().getString(R.string.retweets_label));

                        client.destroyRetweet(retweetIDstr, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                Log.d("DEBUG", jsonObject.toString());
                                tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_grey, 0, 0, 0);

                                int newRC = m_tweet.getRetweet_count() - 1;
                                tvRetweetCount.setText(newRC+ "  " + getResources().getString(R.string.retweets_label));
                                m_tweet.setRetweeted(false);
                                m_tweet.setRetweet_count(newRC);
                                Toast.makeText(DetailActivity.this, getResources().getString(R.string.success_on_unretweet), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.e("ERROR", errorResponse.toString());
                                tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green, 0, 0, 0);
                                tvRetweetCount.setText(m_tweet.getRetweet_count() + "  " + getResources().getString(R.string.retweets_label));
                                Toast.makeText(DetailActivity.this, getResources().getString(R.string.failed_to_unretweet), Toast.LENGTH_LONG).show();
                            }
                        });

                    }else{
                        Log.d("DEBUG", "retweet id was null");
                        Toast.makeText(DetailActivity.this, getResources().getString(R.string.failed_to_unretweet), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void setUpFavoriteButtonListener(){
        tvFavsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to star");

                if( !TwitterUtil.isThereNetworkConnection(DetailActivity.this)){
                    Log.e("ERROR", "no network");
                    Toast.makeText(DetailActivity.this, getResources().getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
                else {

                    long tweetID = m_tweet.getUid();

                    if(!m_tweet.isFavorited()) {//favorite

                        tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow,0,0,0);
                        int newFC = m_tweet.getFavorite_count() + 1;
                        tvFavCount.setText(newFC+ "  " + getResources().getString(R.string.favorites_label));


                        client.makeFavorite(tweetID, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                Log.d("DEBUG", jsonObject.toString());
                                tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow, 0, 0, 0);
                                int newFC = m_tweet.getFavorite_count() + 1;
                                tvFavCount.setText(newFC+ "  " + getResources().getString(R.string.favorites_label));
                                m_tweet.setFavorited(true);
                                m_tweet.setFavorite_count(newFC);
                                Toast.makeText(DetailActivity.this, getResources().getString(R.string.success_on_favorite), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.e("ERROR", errorResponse.toString());
                                tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey, 0, 0, 0);
                                tvFavCount.setText(m_tweet.getFavorite_count()+ "  " + getResources().getString(R.string.favorites_label));
                                Toast.makeText(DetailActivity.this, getResources().getString(R.string.failed_to_favorite), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{//unfavorite

                        tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey,0,0,0);
                        int newFC = m_tweet.getFavorite_count() - 1;
                        tvFavCount.setText(newFC+ "  " + getResources().getString(R.string.favorites_label));

                        client.removeFavorite(tweetID, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                Log.d("DEBUG", jsonObject.toString());
                                tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey, 0, 0, 0);
                                int newFC = m_tweet.getFavorite_count() - 1;
                                tvFavCount.setText(newFC+ "  " + getResources().getString(R.string.favorites_label));
                                m_tweet.setFavorited(false);
                                m_tweet.setFavorite_count(newFC);

                                Toast.makeText(DetailActivity.this, getResources().getString(R.string.success_on_unfavorite), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.e("ERROR", errorResponse.toString());
                                tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow, 0, 0, 0);
                                tvFavCount.setText(m_tweet.getFavorite_count() + "  " + getResources().getString(R.string.favorites_label));
                                Toast.makeText(DetailActivity.this, getResources().getString(R.string.failed_to_unfavorite), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
            }
        });
    }

    private void showReplyDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ReplyDialog replyDialog = ReplyDialog.newInstance(m_tweet, getResources().getString(R.string.title_dialog_reply));
        replyDialog.show(fm, "fragment_reply");
    }

    @Override
    public void onFinishReplyDialog(Tweet newTweet) {
        Log.d("DEBUG", "onFinishReplyDialog: reply is done replyTweet is " + newTweet.toString());
        m_newTweet = newTweet;
        //Toast.makeText(this, "Dialog returning reply tweet: " + m_newTweet.toString(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){

            Intent data = new Intent();
            data.putExtra("updatedtweet", m_tweet);
            data.putExtra("replytweet", m_newTweet);
            setResult(RESULT_OK, data); // set result code and bundle data for response
            finish(); // closes the activity, pass data to parent
        }
        return super.onOptionsItemSelected(item);
    }
}
