package com.codepath.apps.twitterplus.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.twitterplus.R;
import com.codepath.apps.twitterplus.models.Tweet;
import com.codepath.apps.twitterplus.tclient.TwitterApplication;
import com.codepath.apps.twitterplus.tclient.TwitterClient;
import com.codepath.apps.twitterplus.utils.TwitterUtil;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by ekucukog on 5/24/2015.
 */
public class ReplyDialog extends DialogFragment {

    private Tweet m_oldTweet;
    private Tweet m_newTweet;
    private TwitterClient client;
    private TextView tvOrigBody;
    private TextView tvInReplyTo;
    private EditText etReplyBody;
    private Button btTweet;

    public ReplyDialog() {
        // Empty constructor required for DialogFragment
    }

    public interface ReplyDialogListener {
        void onFinishReplyDialog(Tweet tweet);

        void onStartNetworkCall();

        void onFinishNetworkCall();
    }


    public static ReplyDialog newInstance(Tweet tweet, String title) {
        ReplyDialog frag = new ReplyDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("tweet", tweet);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_reply, container);

        String title = getArguments().getString("title", getResources().getString(R.string.title_dialog_reply));
        getDialog().setTitle(title);
        //Show soft keyboard automatically
        //mEditText.requestFocus();
        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Get the client
        client = (TwitterClient) TwitterApplication.getRestClient();//singleton client

        //extract data
        m_oldTweet = (Tweet) getArguments().getParcelable("tweet");
        Log.d("DEBUG", "got the following tweet from parent: " + m_oldTweet.toString());

        setupViews(view);
        setUpReplyButtonListener();

        return view;
    }

    private void setupViews(View view) {

        tvOrigBody = (TextView) view.findViewById(R.id.tvOrigBody);
        tvInReplyTo = (TextView) view.findViewById(R.id.tvInReplyTo);
        etReplyBody = (EditText) view.findViewById(R.id.etReplyBody);
        btTweet = (Button) view.findViewById(R.id.btTweet);

        tvOrigBody.setText(m_oldTweet.getBody());
        tvInReplyTo.setText(getResources().getString(R.string.in_reply_to_text) + " " + m_oldTweet.getUser().getName());

        String temp = "@" + m_oldTweet.getUser().getScreenName() + " ";
        etReplyBody.setText(temp);
        etReplyBody.requestFocus();

    }

    private void setUpReplyButtonListener() {
        btTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TwitterUtil.isThereNetworkConnection(getActivity().getApplicationContext())) {
                    Log.e("ERROR", "no network");
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_network), Toast.LENGTH_LONG).show();
                } else {

                    String newTweetBody = etReplyBody.getText().toString();
                    long originalTweetID = m_oldTweet.getUid();

                    ReplyDialogListener listener = (ReplyDialogListener) getActivity();
                    listener.onStartNetworkCall();
                    client.replyTweet(newTweetBody, originalTweetID, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                            Log.d("DEBUG", jsonObject.toString());
                            Toast.makeText(getActivity(), getResources().getString(R.string.success_on_posting_reply_tweet), Toast.LENGTH_SHORT).show();

                            ReplyDialogListener listener = (ReplyDialogListener) getActivity();
                            listener.onFinishNetworkCall();
                            listener.onFinishReplyDialog(Tweet.fromJson(jsonObject));
                            dismiss();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e("ERROR", errorResponse.toString());
                            Toast.makeText(getActivity(), getResources().getString(R.string.failed_to_reply_tweet), Toast.LENGTH_LONG).show();
                            ReplyDialogListener listener = (ReplyDialogListener) getActivity();
                            listener.onFinishNetworkCall();
                        }
                    });
                }
            }
        });
    }
}
