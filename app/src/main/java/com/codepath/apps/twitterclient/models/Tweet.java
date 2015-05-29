package com.codepath.apps.twitterclient.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.twitterclient.utils.TwitterUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ekucukog on 5/19/2015.
 */


//Parse the JSON and store the data, encapsulate state logic or display logic

@Table(name = "Tweets")
public class Tweet extends Model implements Parcelable {

    @Column(name = "body")
    private String body;

    // This is the unique id given by the server
    // @Column(name = "remote_id")
    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;// unique db id for the tweet

    @Column(name = "User", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private User user;

    @Column(name = "timestamp")
    private String createdAt;

    @Column(name = "favorite_count")
    private int favorite_count=0;

    @Column(name = "retweet_count")
    private int retweet_count=0;

    @Column(name = "favorited")
    private boolean favorited = false;

    @Column(name = "retweeted")
    private boolean retweeted = false;

    @Column(name = "mediatype")
    private String mediatype = "nomedia";//image or video

    @Column(name = "mediaurl")
    private String mediaurl = null;

    @Column(name = "replystatusID")
    private long inReplyToStatusID = -1;

    @Column(name = "replyuserID")
    private long inReplyToUserID = -1;

    @Column(name = "replyscreenname")
    private String inReplyToScreenName = null;

    @Column(name = "retweetingUser", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private User retweetingUser = null;

    @Column(name = "isretweet")
    private boolean isRetweet = false;

    @Column(name = "currentuserretweetIDstr")
    private String current_user_retweet_id_str = null;

    @Column(name = "idstr")
    private String id_str_x = null;

    public Tweet() {
        super();
    }

    private static long mostRecentID = -1;
    private static long earliestID = -1 ;

    //deserialize the json and build tweet object
    // Tweet.fromJson("{ ... }") -> <Tweet>
    public static Tweet fromJson(JSONObject jsonObject){
        Tweet tweet = new Tweet();
        //Extract the values from json, store them
        try {

            if( jsonObject.optJSONObject("current_user_retweet") !=null ){
                tweet.current_user_retweet_id_str = jsonObject.getJSONObject("current_user_retweet").getString("id_str");
                Log.d("DEBUG", "retweet is found: " + tweet.current_user_retweet_id_str);
            }
            tweet.id_str_x = jsonObject.getString("id_str");

            if(jsonObject.optJSONObject("retweeted_status")!=null){//retweet
                tweet.retweetingUser = User.findOrCreateFromJson(jsonObject.getJSONObject("user"));
                jsonObject = jsonObject.getJSONObject("retweeted_status");
                tweet.isRetweet = true;
            }

            tweet.body = jsonObject.getString("text");
            tweet.uid = jsonObject.getLong("id");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.user = User.findOrCreateFromJson(jsonObject.getJSONObject("user"));

            tweet.favorite_count = jsonObject.getInt("favorite_count");
            tweet.retweet_count = jsonObject.getInt("retweet_count");
            tweet.favorited = jsonObject.getBoolean("favorited");
            tweet.retweeted = jsonObject.getBoolean("retweeted");

            //    jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url")
            if( jsonObject.optJSONObject("extended_entities")!=null &&
                jsonObject.getJSONObject("extended_entities").optJSONArray("media")!=null &&
                jsonObject.getJSONObject("extended_entities").getJSONArray("media").optJSONObject(0)!=null){

                if( jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).optString("media_url") !=null )
                    tweet.mediaurl = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("media_url");

                if( jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).optString("type") !=null )
                    tweet.mediatype = jsonObject.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0).getString("type");
            }

            if(!tweet.isRetweet()) {

                if (jsonObject.optString("in_reply_to_status_id") != null) {
                    tweet.inReplyToStatusID = jsonObject.getLong("in_reply_to_status_id");
                }
                if (jsonObject.optString("in_reply_to_user_id") != null) {
                    tweet.inReplyToUserID = jsonObject.getLong("in_reply_to_user_id");
                }
                if (jsonObject.optString("in_reply_to_screen_name") != null && !jsonObject.optString("in_reply_to_screen_name").equals("null")) {
                    tweet.inReplyToScreenName = jsonObject.getString("in_reply_to_screen_name");
                }
            }

            tweet.save();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //return tweet object
        return tweet;
    }

    //deserialize the tweet json array and build tweet arraylist
    // Tweet.fromJsonArray("[ { ... } { ... } { ... } ]") -> List<Tweet>
    public static ArrayList<Tweet> fromJsonArray(JSONArray jsonArray, int refreshType){

        ArrayList<Tweet> tweets = new ArrayList<>();

        for(int i=0; i<jsonArray.length(); i++){
            try {
                JSONObject tweetJson = jsonArray.getJSONObject(i);
                Tweet tweet = Tweet.fromJson(tweetJson);
                if(tweet!=null){
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        if(refreshType != TwitterUtil.SCROLL){
            Tweet.mostRecentID = tweets.get(0).getUid();
        }
        Tweet.earliestID = tweets.get(tweets.size()-1).getUid();

        //return tweet object
        return tweets;
    }

    public static List<Tweet> getAllFromDB() {
        // This is how you execute a query
        return new Select()
                .from(Tweet.class)
                .orderBy("remote_id DESC")
                .execute();
    }

    public int getFavorite_count() {
        return favorite_count;
    }

    public int getRetweet_count() {
        return retweet_count;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public String getMediatype() {
        return mediatype;
    }

    public String getMediaurl() {
        return mediaurl;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public static long getMostRecentID() {
        return mostRecentID;
    }

    public static long getEarliestID() {
        return earliestID;
    }

    public User getUser() {
        return user;
    }


    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public long getInReplyToStatusID() {
        return inReplyToStatusID;
    }

    public boolean isRetweet() {
        return isRetweet;
    }

    public User getRetweetingUser() {
        return retweetingUser;
    }

    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    public long getInReplyToUserID() {
        return inReplyToUserID;
    }

    public void setFavorite_count(int favorite_count) {
        this.favorite_count = favorite_count;
    }

    public void setRetweet_count(int retweet_count) {
        this.retweet_count = retweet_count;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public String getCurrent_user_retweet_id_str() {
        return current_user_retweet_id_str;
    }

    public void setCurrent_user_retweet_id_str(String current_user_retweet_id_str) {
        this.current_user_retweet_id_str = current_user_retweet_id_str;
    }

    public void setId_str_x(String id_str_x) {
        this.id_str_x = id_str_x;
    }

    public String getId_str_x() {
        return id_str_x;
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "body='" + body + '\'' +
                ", uid=" + uid +
                ", user=" + user +
                ", createdAt='" + createdAt + '\'' +
                ", favorite_count=" + favorite_count +
                ", retweet_count=" + retweet_count +
                ", favorited=" + favorited +
                ", retweeted=" + retweeted +
                ", mediatype='" + mediatype + '\'' +
                ", mediaurl='" + mediaurl + '\'' +
                ", inReplyToStatusID=" + inReplyToStatusID +
                ", inReplyToUserID=" + inReplyToUserID +
                ", inReplyToScreenName='" + inReplyToScreenName + '\'' +
                ", retweetingUser=" + retweetingUser +
                ", isRetweet=" + isRetweet +
                ", current_user_retweet_id_str='" + current_user_retweet_id_str + '\'' +
                ", id_str_x='" + id_str_x + '\'' +
                '}';
    }

    public static Tweet findTweet(ArrayList<Tweet> tweets, long uid){
        for(int i=0; i< tweets.size(); i++){
            if(tweets.get(i).getUid()==uid){
                return tweets.get(i);
            }
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.body);
        dest.writeLong(this.uid);
        dest.writeParcelable(this.user, 0);
        dest.writeString(this.createdAt);
        dest.writeInt(this.favorite_count);
        dest.writeInt(this.retweet_count);
        dest.writeByte(favorited ? (byte) 1 : (byte) 0);
        dest.writeByte(retweeted ? (byte) 1 : (byte) 0);
        dest.writeString(this.mediatype);
        dest.writeString(this.mediaurl);
        dest.writeLong(this.inReplyToStatusID);
        dest.writeLong(this.inReplyToUserID);
        dest.writeString(this.inReplyToScreenName);
        dest.writeParcelable(this.retweetingUser, 0);
        dest.writeByte(isRetweet ? (byte) 1 : (byte) 0);
        dest.writeString(this.current_user_retweet_id_str);
        dest.writeString(this.id_str_x);
    }

    private Tweet(Parcel in) {
        this.body = in.readString();
        this.uid = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
        this.favorite_count = in.readInt();
        this.retweet_count = in.readInt();
        this.favorited = in.readByte() != 0;
        this.retweeted = in.readByte() != 0;
        this.mediatype = in.readString();
        this.mediaurl = in.readString();
        this.inReplyToStatusID = in.readLong();
        this.inReplyToUserID = in.readLong();
        this.inReplyToScreenName = in.readString();
        this.retweetingUser = in.readParcelable(User.class.getClassLoader());
        this.isRetweet = in.readByte() != 0;
        this.current_user_retweet_id_str = in.readString();
        this.id_str_x = in.readString();
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        public Tweet createFromParcel(Parcel source) {
            return new Tweet(source);
        }

        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
