package com.codepath.apps.mysimpletweets.models;

import android.text.format.DateUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by emma_baumstarck on 8/17/16.
 */
@Table(name = "Tweets")
public class Tweet extends Model {
    private static final String TWITTER_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    @Column(name = "Body")
    public String body;
    @Column(name = "Uid", unique = true)
    public long uid;
    @Column(name = "User")
    public User user;
    @Column(name = "CreatedAt")
    public String createdAt;

    @Column(name = "UserName")
    public String userName;
    @Column(name = "UserId")
    public long userId;
    @Column(name = "UserScreenName")
    public String userScreenName;
    @Column(name = "UserProfileImageUrl")
    public String userProfileImageUrl;
    @Column(name = "UserTagline")
    public String userTagline;
    @Column(name = "UserFriendsCount")
    public int userFollowingCount;
    @Column(name = "UserFollowersCount")
    public int userFollowersCount;

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public User getUser() {
        if (user == null) {
            JSONObject object = new JSONObject();
            try {
                object.put("name", userName);
                object.put("id", userId);
                object.put("screen_name", userScreenName);
                object.put("profile_image_url", userProfileImageUrl);
                object.put("tagline", userTagline);
                object.put("friends_count", userFollowingCount);
                object.put("followers_count", userFollowersCount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            user = User.fromJSON(object);
        }
        return user;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = jsonObject.getString("text");
            tweet.uid = jsonObject.getLong("id");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
            tweet.userName = tweet.user.getName();
            tweet.userId = tweet.user.getUid();
            tweet.userScreenName = tweet.user.getScreenName();
            tweet.userProfileImageUrl = tweet.user.getProfileImageUrl();
            tweet.userTagline = tweet.user.getTagline();
            tweet.userFollowingCount = tweet.user.getFollowingCount();
            tweet.userFollowersCount = tweet.user.getFollowersCount();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                tweets.add(fromJSON(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tweets;
    }

    private static String getRelativeTimeAgo(String rawDate) {
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER_FORMAT, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString()
                    .replaceAll("(^in | ago$)", "")
                    .replaceAll(" seconds", "s")
                    .replaceAll(" minutes", "m")
                    .replaceAll(" hours", "h")
                    .replaceAll(" days", "d");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public String getCreatedAtRelative() {
        return getRelativeTimeAgo(createdAt);
    }

    public static List<Tweet> getHomeTimeline(long maxId) {
        // This is how you execute a query
        From from = new Select().from(Tweet.class);
        if (maxId != -1) {
            from = from.where("Uid < ?", maxId);
        }
        return from.orderBy("Uid DESC").execute();
    }
}
