package com.codepath.apps.twitterclient.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.activities.DetailActivity;
import com.codepath.apps.twitterclient.activities.TimelineActivity;
import com.codepath.apps.twitterclient.dialogs.ReplyDialog;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.tclient.TwitterApplication;
import com.codepath.apps.twitterclient.tclient.TwitterClient;
import com.codepath.apps.twitterclient.utils.LinkifiedTextView;
import com.codepath.apps.twitterclient.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by ekucukog on 5/20/2015.
 */

//Taking the Tweets objects and turning them into Views displayed in the list
public class TweetsArrayAdapter extends ArrayAdapter<Tweet>{

    private TwitterClient client;

    // View lookup cache
    private static class ViewHolder {
        ImageView ivProfileImage;
        TextView tvUsername;
        LinkifiedTextView tvBody;
        TextView tvFullName;
        TextView tvTimestamp;
        TextView tvRetweetorReplyPreText;
        TextView tvReplyAction;
        TextView tvRetweetAction;
        TextView tvFavsAction;
    }

    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, 0, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1 get the tweet
        Tweet tweet = getItem(position);
        // 2 inflate the template
        ViewHolder viewHolder;
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent, false);
            viewHolder = new ViewHolder();
            // 3 find the subviews to fill with the data in view
            viewHolder.tvUsername = (TextView) convertView.findViewById(R.id.tvUsername);
            viewHolder.ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
            viewHolder.tvBody = (LinkifiedTextView) convertView.findViewById(R.id.tvBody);
            viewHolder.tvFullName = (TextView) convertView.findViewById(R.id.tvFullName);
            viewHolder.tvTimestamp = (TextView) convertView.findViewById(R.id.tvTimestamp);
            viewHolder.tvRetweetorReplyPreText = (TextView) convertView.findViewById(R.id.tvRetweetorReplyPreText);
            viewHolder.tvReplyAction = (TextView) convertView.findViewById(R.id.tvReplyAction);
            viewHolder.tvRetweetAction = (TextView) convertView.findViewById(R.id.tvRetweetAction);
            viewHolder.tvFavsAction = (TextView) convertView.findViewById(R.id.tvFavsAction);

            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 4 populate data into subviews
        viewHolder.tvUsername.setText( "@" + tweet.getUser().getScreenName());
        viewHolder.tvBody.setText(tweet.getBody());
        viewHolder.tvFullName.setText(tweet.getUser().getName());
        viewHolder.tvTimestamp.setText(TwitterUtil.getRelativeTimeAgo(tweet.getCreatedAt()));
        viewHolder.ivProfileImage.setImageResource(android.R.color.transparent);//clear out the old image for a recycled view

        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl())
                //.fit().centerInside()
                .into(viewHolder.ivProfileImage);

        if(tweet.isRetweet() && tweet.getRetweetingUser()!=null){//retweet
            Log.d("DEBUG", "retweeted by user:  " + tweet.getRetweetingUser().toString());
            viewHolder.tvRetweetorReplyPreText.setVisibility(View.VISIBLE);
            viewHolder.tvRetweetorReplyPreText.setText(tweet.getRetweetingUser().getName() + " " + getContext().getResources().getString(R.string.retweeted_text));
        }
        else if(tweet.getInReplyToScreenName()!=null){//reply
            Log.d("DEBUG", "In reply to user:  " + tweet.getInReplyToScreenName());
            viewHolder.tvRetweetorReplyPreText.setVisibility(View.VISIBLE);
            viewHolder.tvRetweetorReplyPreText.setText(getContext().getResources().getString(R.string.in_reply_to_text)
                     + " " + tweet.getInReplyToScreenName());
        }
        else{//regular tweet
            viewHolder.tvRetweetorReplyPreText.setVisibility(View.GONE);
        }

        //viewHolder.tvReplyAction;
        if(tweet.isRetweeted()){//if retweeted by the current user
            viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green, 0, 0, 0);
        }
        else{
            viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_grey, 0, 0, 0);
        }
        viewHolder.tvRetweetAction.setText("" + tweet.getRetweet_count());

        if(tweet.isFavorited()){//if favorited by the current user
            viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow, 0, 0, 0);
        }
        else{
            viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey, 0, 0, 0);
        }
        viewHolder.tvFavsAction.setText("" + tweet.getFavorite_count());

        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

        setUpRetweetButtonListener(viewHolder, position);
        setUpFavoriteButtonListener(viewHolder, position);
        setUpReplyButtonListener(viewHolder, position);

        // 5 return the view to be inserted into list
        return convertView;
    }

    private void setUpRetweetButtonListener(final ViewHolder viewHolder, final int position){

        viewHolder.tvRetweetAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to retweet");

                if( !TwitterUtil.isThereNetworkConnection( getContext() )){
                    Log.e("ERROR", "no network");
                    Toast.makeText( getContext(), R.string.no_network, Toast.LENGTH_LONG).show();
                }
                else {

                    Tweet tweet = (Tweet) getItem(position);
                    long tweetID = tweet.getUid();
                    String retweetIDstr = tweet.getCurrent_user_retweet_id_str();

                    if(!tweet.isRetweeted()) {//not retweeted before, retweet now

                        viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green,0,0,0);
                        int newRC = tweet.getRetweet_count() + 1;
                        viewHolder.tvRetweetAction.setText("" + newRC);

                        client.retweet(tweetID, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                Log.d("DEBUG", jsonObject.toString());

                                viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green, 0, 0, 0);
                                Tweet tweet = (Tweet) getItem(position);
                                int newRC = tweet.getRetweet_count() + 1;
                                viewHolder.tvRetweetAction.setText("" + newRC);
                                tweet.setRetweeted(true);
                                tweet.setRetweet_count(newRC);
                                Tweet updatedTweet = Tweet.fromJson(jsonObject);
                                //tweet.setCurrent_user_retweet_id_str(updatedTweet.getCurrent_user_retweet_id_str());
                                tweet.setCurrent_user_retweet_id_str(updatedTweet.getId_str_x());

                                Toast.makeText( getContext(), R.string.success_on_retweet, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.e("ERROR", errorResponse.toString());
                                viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_grey, 0, 0, 0);
                                Tweet tweet = (Tweet) getItem(position);
                                viewHolder.tvRetweetAction.setText("" + tweet.getRetweet_count());

                                Toast.makeText( getContext(), R.string.failed_to_retweet, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else if (retweetIDstr!=null){//was already retweeted, now destroy retweet

                        Log.d("DEBUG", "retweet id str: " + retweetIDstr);
                        viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_grey,0,0,0);
                        int newRC = tweet.getRetweet_count() - 1;
                        viewHolder.tvRetweetAction.setText("" + newRC);

                        client.destroyRetweet(retweetIDstr, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                Log.d("DEBUG", jsonObject.toString());

                                viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_grey, 0, 0, 0);

                                Tweet tweet = (Tweet) getItem(position);
                                int newRC = tweet.getRetweet_count() - 1;
                                viewHolder.tvRetweetAction.setText("" + newRC);
                                tweet.setRetweeted(false);
                                tweet.setRetweet_count(newRC);
                                Toast.makeText(getContext(), R.string.success_on_unretweet, Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.e("ERROR", errorResponse.toString());
                                viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green, 0, 0, 0);
                                Tweet tweet = (Tweet) getItem(position);
                                viewHolder.tvRetweetAction.setText("" + tweet.getRetweet_count());
                                Toast.makeText( getContext(), R.string.failed_to_unretweet, Toast.LENGTH_LONG).show();
                            }
                        });

                    }else{
                        Log.d("DEBUG", "retweet id was null");
                        Toast.makeText( getContext(), R.string.failed_to_unretweet, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void setUpFavoriteButtonListener(final ViewHolder viewHolder, final int position){

        viewHolder.tvFavsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to star");

                if( !TwitterUtil.isThereNetworkConnection( getContext())){
                    Log.e("ERROR", "no network");
                    Toast.makeText( getContext(), R.string.no_network, Toast.LENGTH_LONG).show();
                }
                else {

                    Tweet tweet = (Tweet) getItem(position);
                    long tweetID = tweet.getUid();

                    if(!tweet.isFavorited()) {//it is not favorited yet, go and favorite

                        viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow,0,0,0);
                        int newFC = tweet.getFavorite_count() + 1;
                        viewHolder.tvFavsAction.setText("" + newFC);

                        client.makeFavorite(tweetID, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                Log.d("DEBUG", jsonObject.toString());
                                viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow, 0, 0, 0);
                                Tweet tweet = (Tweet) getItem(position);
                                int newFC = tweet.getFavorite_count() + 1;
                                viewHolder.tvFavsAction.setText("" + newFC);
                                tweet.setFavorited(true);
                                tweet.setFavorite_count(newFC);
                                Toast.makeText(getContext(), R.string.success_on_favorite, Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.e("ERROR", errorResponse.toString());
                                viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey, 0, 0, 0);
                                Tweet tweet = (Tweet) getItem(position);
                                viewHolder.tvFavsAction.setText("" + tweet.getFavorite_count());
                                Toast.makeText(getContext(), R.string.failed_to_favorite, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{//unfavorite

                        viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey,0,0,0);
                        int newFC = tweet.getFavorite_count() - 1;
                        viewHolder.tvFavsAction.setText("" + newFC);

                        client.removeFavorite(tweetID, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                                Log.d("DEBUG", jsonObject.toString());
                                viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey, 0, 0, 0);
                                Tweet tweet = (Tweet) getItem(position);
                                int newFC = tweet.getFavorite_count() - 1;
                                viewHolder.tvFavsAction.setText("" + newFC);
                                tweet.setFavorited(false);
                                tweet.setFavorite_count(newFC);

                                Toast.makeText(getContext(), R.string.success_on_unfavorite, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.e("ERROR", errorResponse.toString());
                                viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow, 0, 0, 0);
                                Tweet tweet = (Tweet) getItem(position);
                                viewHolder.tvFavsAction.setText("" + tweet.getFavorite_count());
                                Toast.makeText(getContext(), R.string.failed_to_unfavorite, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    private void setUpReplyButtonListener(final ViewHolder viewHolder, final int position){
        viewHolder.tvReplyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to reply");
                Tweet tweet = getItem(position);
                showReplyDialog(tweet, viewHolder);
            }
        });
    }

    public void showReplyDialog(Tweet tweet, ViewHolder viewHolder) {
        FragmentActivity activity = (FragmentActivity)viewHolder.tvReplyAction.getContext();
        FragmentManager fm = activity.getSupportFragmentManager();
        ReplyDialog replyDialog = ReplyDialog.newInstance(tweet, getContext().getResources().getString(R.string.title_dialog_reply));
        replyDialog.show(fm, "fragment_reply");
    }
}
