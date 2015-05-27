package com.codepath.apps.twitterclient.tclient;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;

import com.codepath.apps.twitterclient.utils.TwitterUtil;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
	public static final String REST_URL = "https://api.twitter.com/1.1/"; //base API URL
	public static final String REST_CONSUMER_KEY = "pHTIkl55quSqsW2PvPDb0OuJU";
	public static final String REST_CONSUMER_SECRET = "iItkHn0vjyRIB1uGRo4TjeGRDYlhxQOefwzl30HgrBERbTsc00";
	public static final String REST_CALLBACK_URL = "oauth://cptwitterclient"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	// DEFINE METHODS for different API endpoints here
	//each endpoint = method here

    /*
    1. Get home time line
    GET statuses/home_timeline.json
        count=25
        since_id=1 all tweets
    */

    public void getHomeTimeLine(int refreshType, long offset, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        params.put("count", 25);

        if(refreshType == TwitterUtil.SCROLL){
            //get the 25 tweets before current earliest
            params.put("max_id", offset);// offset: earliest -1
        }
        else{
            //if(refreshType == TwitterUtil.NEW_PAGE) ||
            //if(refreshType == TwitterUtil.SWIPE)
            params.put("since_id", 1);// get the most recent 25
        }
        params.put("include_my_retweet", true);

        //Execute the request
        getClient().get(apiUrl,params, handler);
    }

    public void postTweet(String newTweetBody, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        params.put("status", newTweetBody);

        //Execute the request
        getClient().post(apiUrl, params, handler);
    }

    public void replyTweet(String newTweetBody, long originalTweetID, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        params.put("status", newTweetBody);
        params.put("in_reply_to_status_id", originalTweetID);

        //Execute the request
        getClient().post(apiUrl, params, handler);
    }

    public void retweet(long originalTweetID, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/retweet/" + originalTweetID  + ".json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        //params.put("include_my_retweet", true);/////

        //Execute the request
        getClient().post(apiUrl, params, handler);
    }

    public void destroyRetweet(String retweetIDstr, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/destroy/" + retweetIDstr + ".json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();

        //Execute the request
        getClient().post(apiUrl, params, handler);
    }

    public void getCurrentUserInfo(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
       //Execute the request
        getClient().get(apiUrl, handler);
    }

    public void makeFavorite(long tweetID, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/create.json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        params.put("id", tweetID);

        //Execute the request
        getClient().post(apiUrl, params, handler);
    }

    public void removeFavorite(long tweetID, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/destroy.json");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        params.put("id", tweetID);

        //Execute the request
        getClient().post(apiUrl, params, handler);
    }


	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}