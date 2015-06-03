package com.codepath.apps.twitterplus.adapters;

import android.content.Context;
import android.content.Intent;
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

import com.codepath.apps.twitterplus.R;
import com.codepath.apps.twitterplus.activities.ProfileActivity;
import com.codepath.apps.twitterplus.dialogs.ReplyDialog;
import com.codepath.apps.twitterplus.models.Tweet;
import com.codepath.apps.twitterplus.models.User;
import com.codepath.apps.twitterplus.tclient.TwitterApplication;
import com.codepath.apps.twitterplus.tclient.TwitterClient;
import com.codepath.apps.twitterplus.utils.LinkifiedTextView;
import com.codepath.apps.twitterplus.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by ekucukog on 5/20/2015.
 */

//Taking the Tweets objects and turning them into Views displayed in the list
public class TweetsArrayAdapter extends ArrayAdapter<Tweet> {

    private TwitterClient client;
    private TAAFragmentListener fragmentListener;

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
        this.fragmentListener = null;
    }

    // Assign the listener implementing events interface that will receive the events
    public void setTAAFragmentListener(TAAFragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

    public interface TAAFragmentListener{
        public void onClickToFavorite(int position);
        public void onClickToRetweet(int position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1 get the tweet
        Tweet tweet = getItem(position);
        // 2 inflate the template
        ViewHolder viewHolder;
        if (convertView == null) {
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
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 4 populate data into subviews
        viewHolder.tvUsername.setText("@" + tweet.getUser().getScreenName());
        viewHolder.tvBody.setText(tweet.getBody());
        viewHolder.tvFullName.setText(tweet.getUser().getName());
        viewHolder.tvTimestamp.setText(TwitterUtil.getRelativeTimeAgo(tweet.getCreatedAt()));
        viewHolder.ivProfileImage.setImageResource(android.R.color.transparent);//clear out the old image for a recycled view

        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl())
                //.fit().centerInside()
                .into(viewHolder.ivProfileImage);

        if (tweet.isRetweet() && tweet.getRetweetingUser() != null) {//retweet
            Log.d("DEBUG", "retweeted by user:  " + tweet.getRetweetingUser().toString());
            viewHolder.tvRetweetorReplyPreText.setVisibility(View.VISIBLE);
            viewHolder.tvRetweetorReplyPreText.setText(tweet.getRetweetingUser().getName() + " " + getContext().getResources().getString(R.string.retweeted_text));
        } else if (tweet.getInReplyToScreenName() != null) {//reply
            Log.d("DEBUG", "In reply to user:  " + tweet.getInReplyToScreenName());
            viewHolder.tvRetweetorReplyPreText.setVisibility(View.VISIBLE);
            viewHolder.tvRetweetorReplyPreText.setText(getContext().getResources().getString(R.string.in_reply_to_text)
                    + " " + tweet.getInReplyToScreenName());
        } else {//regular tweet
            viewHolder.tvRetweetorReplyPreText.setVisibility(View.GONE);
        }

        //viewHolder.tvReplyAction;
        if (tweet.isRetweeted()) {//if retweeted by the current user
            viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_green, 0, 0, 0);
        } else {
            viewHolder.tvRetweetAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_retweet_grey, 0, 0, 0);
        }
        viewHolder.tvRetweetAction.setText("" + tweet.getRetweet_count());

        if (tweet.isFavorited()) {//if favorited by the current user
            viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow, 0, 0, 0);
        } else {
            viewHolder.tvFavsAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_grey, 0, 0, 0);
        }
        viewHolder.tvFavsAction.setText("" + tweet.getFavorite_count());

        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

        setUpRetweetButtonListener(viewHolder, position);
        setUpFavoriteButtonListener(viewHolder, position);
        setUpReplyButtonListener(viewHolder, position);
        setUpProfileImageListener(viewHolder, position);

        // 5 return the view to be inserted into list
        return convertView;
    }

    private void setUpRetweetButtonListener(final ViewHolder viewHolder, final int position) {

        viewHolder.tvRetweetAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to retweet");
                if(fragmentListener!=null){
                    fragmentListener.onClickToRetweet(position);
                }
            }
        });
    }

    private void setUpFavoriteButtonListener(final ViewHolder viewHolder, final int position) {

        viewHolder.tvFavsAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to star icon");
                if(fragmentListener!=null){
                    fragmentListener.onClickToFavorite(position);
                }
            }
        });
    }

    private void setUpReplyButtonListener(final ViewHolder viewHolder, final int position) {
        viewHolder.tvReplyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to reply");
                Tweet tweet = getItem(position);
                showReplyDialog(tweet, viewHolder);
            }
        });
    }

    private void setUpProfileImageListener(final ViewHolder viewHolder, final int position) {
        viewHolder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Clicked to image");
                Tweet tweet = getItem(position);
                Intent i = new Intent(getContext(), ProfileActivity.class);
                if(tweet!=null){
                    i.putExtra("user", tweet.getUser());
                }
                getContext().startActivity(i);
            }
        });
    }

    public void showReplyDialog(Tweet tweet, ViewHolder viewHolder) {
        FragmentActivity activity = (FragmentActivity) viewHolder.tvReplyAction.getContext();
        FragmentManager fm = activity.getSupportFragmentManager();
        ReplyDialog replyDialog = ReplyDialog.newInstance(tweet, getContext().getResources().getString(R.string.title_dialog_reply));
        replyDialog.show(fm, "fragment_reply");
    }
}
