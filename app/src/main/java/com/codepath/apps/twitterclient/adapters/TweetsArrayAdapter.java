package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.utils.LinkifiedTextView;
import com.codepath.apps.twitterclient.utils.TwitterUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ekucukog on 5/20/2015.
 */

//Taking the Tweets objects and turning them into Views displayed in the list
public class TweetsArrayAdapter extends ArrayAdapter<Tweet>{

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
            viewHolder.tvRetweetorReplyPreText.setText(tweet.getRetweetingUser().getName() + " " + getContext().getResources().getString(R.string.retweeted_text) );
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


        // 5 return the view to be inserted into list
        return convertView;
    }
}
