package com.codepath.apps.twitterplus.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ekucukog on 5/19/2015.
 */

@Table(name = "Users")
public class User extends Model implements Parcelable {

    @Column(name = "name")
    private String name;

    // This is the unique id given by the server
    //@Column(name = "remote_id")
    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;

    @Column(name = "screen")
    private String screenName;

    @Column(name = "url")
    private String profileImageUrl;

    @Column(name = "tag")
    private String tag;

    @Column(name = "followerscount")
    private int followersCount;

    @Column(name = "followingcount")
    private int followingCount;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", uid=" + uid +
                ", screenName='" + screenName + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", tag='" + tag + '\'' +
                ", followersCount=" + followersCount +
                ", followingCount=" + followingCount +
                '}';
    }

    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getTag() {
        return tag;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    //deserialize the user json and build user object
    public static User fromJson(JSONObject jsonObject){
        User user = new User();
        //Extract the values from json, store them
        try {
            user.name = jsonObject.getString("name");
            user.uid = jsonObject.getLong("id");
            user.screenName = jsonObject.getString("screen_name");
            user.profileImageUrl = jsonObject.getString("profile_image_url");
            user.tag = jsonObject.getString("description");
            user.followersCount = jsonObject.getInt("followers_count");
            user.followingCount = jsonObject.getInt("friends_count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //return user object
        return user;
    }

    // Finds existing user based on remoteId or creates new user and returns
    public static User findOrCreateFromJson(JSONObject json) {

        try {
            long uid = json.getLong("id"); // get just the remote id
            User existingUser = new Select().from(User.class).where("remote_id = ?", uid).executeSingle();
            if (existingUser != null) {
                // found and return existing
                return existingUser;
            }
            else{
                // create and return new user
                User user = User.fromJson(json);
                user.save();
                return user;
            }
        }
        catch (JSONException e) {
            Log.e("ERROR", "user json object not valid");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeLong(this.uid);
        dest.writeString(this.screenName);
        dest.writeString(this.profileImageUrl);
        dest.writeString(this.tag);
        dest.writeInt(this.followersCount);
        dest.writeInt(this.followingCount);
    }

    public User() {
    }

    private User(Parcel in) {
        this.name = in.readString();
        this.uid = in.readLong();
        this.screenName = in.readString();
        this.profileImageUrl = in.readString();
        this.tag = in.readString();
        this.followersCount = in.readInt();
        this.followingCount = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}