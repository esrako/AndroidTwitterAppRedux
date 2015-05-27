package com.codepath.apps.twitterclient.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.activities.ComposeActivity;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.tclient.TwitterApplication;
import com.codepath.apps.twitterclient.tclient.TwitterClient;
import com.codepath.apps.twitterclient.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by ekucukog on 5/25/2015.
 */
public class ComposeDialog extends DialogFragment {

    private EditText etNewTweet;
    private TwitterClient client;
    private ImageView ivCurrentProfile;
    private TextView tvCurrentFullName;
    private TextView tvCurrentUsername;
    private TextView tvChCount;
    private Button btTweet;
    private TextView tvCross;

    private User m_user;
    private int m_length=0;
    final int MAX_TWEET_LENGTH=140;


    public ComposeDialog() {
        // Empty constructor required for DialogFragment
    }

    public interface ComposeDialogListener {
        void onFinishComposeDialog(Tweet tweet);
    }

    public static ComposeDialog newInstance(String title) {
        ComposeDialog frag = new ComposeDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_compose, container);

        String title = getArguments().getString("title", getResources().getString(R.string.title_dialog_compose));
        getDialog().setTitle(title);

        //Show soft keyboard automatically
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //full screen
        //getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client
        m_user = TwitterUtil.getCurrentUser();

        setUpViews(view);
        setUpEditTextListener();
        setUpTweetButtonListener();
        setUpCloseButtonListener();

        return view;
    }

    private void setUpViews(View view){

        etNewTweet = (EditText) view.findViewById(R.id.etNewTweet);
        ivCurrentProfile = (ImageView) view.findViewById(R.id.ivCurrentProfile);
        tvCurrentFullName =(TextView) view.findViewById(R.id.tvCurrentFullName);
        tvCurrentUsername = (TextView) view.findViewById(R.id.tvCurrentUsername);
        tvChCount = (TextView) view.findViewById(R.id.tvChCount);
        btTweet = (Button) view.findViewById(R.id.btTweet);
        tvCross = (TextView) view.findViewById(R.id.tvCross);

        if(m_user!=null) {
            tvCurrentFullName.setText(m_user.getName());
            tvCurrentUsername.setText("@" + m_user.getScreenName());

            Picasso.with( getActivity()).load(m_user.getProfileImageUrl())
                    .into(ivCurrentProfile);
        }else{//just in case something went wrong with getting current user, do not let app crash
            RelativeLayout rluser = (RelativeLayout) view.findViewById(R.id.rluser);
            rluser.setVisibility(View.INVISIBLE);
            Log.d("DEBUG", "user is null");
        }
        etNewTweet.requestFocus();
    }

    private void setUpEditTextListener(){

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
                Log.d("DEBUG", "m_length is " + m_length);
                int remaining = MAX_TWEET_LENGTH-m_length;
                tvChCount.setText(String.valueOf(remaining));
                if(remaining < 0){
                    btTweet.setAlpha(0.5f);
                    //btTweet.setEnabled(false);
                }else{
                    btTweet.setAlpha(1f);
                    //btTweet.setEnabled(true);
                }
            }
        });
    }

    private void setUpTweetButtonListener(){

        if( !TwitterUtil.isThereNetworkConnection(getActivity())){
            Log.e("ERROR", "no network");
            Toast.makeText(getActivity(), getResources().getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
        else if(m_length > MAX_TWEET_LENGTH){

            Toast.makeText(getActivity(), getResources().getString(R.string.tweet_too_long), Toast.LENGTH_LONG).show();
        }
        else{

            String newTweetBody = etNewTweet.getText().toString();

            client.postTweet(newTweetBody, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.d("DEBUG", jsonObject.toString());
                    Toast.makeText( getActivity() , getResources().getString(R.string.success_on_posting_tweet), Toast.LENGTH_SHORT).show();

                    Tweet tweet = Tweet.fromJson(jsonObject);
                    Log.d("DEBUG", tweet.toString());
                    Toast.makeText( getActivity() , "tweet", Toast.LENGTH_SHORT).show();

                    ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                    listener.onFinishComposeDialog(tweet);
                    dismiss();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    //Toast.makeText(  , "failed on posting new tweet", Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", errorResponse.toString());
                    Toast.makeText(getActivity(), getResources().getString(R.string.failed_to_tweet), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setUpCloseButtonListener(){

        tvCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                listener.onFinishComposeDialog(null);
                dismiss();
            }
        });
    }
}
