package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;

import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by emma_baumstarck on 8/18/16.
 */
public class HomeTimelineFragment extends TweetsListFragment {

    private TwitterClient client;
    private boolean internetEnabled = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = TwitterApplication.getRestClient();
    }

    @Override
    protected void loadTweets(final long maxId, final JsonHttpResponseHandler handler) {
        JsonHttpResponseHandler wrappedHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                internetEnabled = true;
                handler.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                handler.onFailure(statusCode, headers, throwable, errorResponse);
                if (errorResponse == null) {
                    // load from cache
                    internetEnabled = false;
                    acceptTweets(Tweet.getHomeTimeline(maxId));
                }
            }
        };
        client.getHomeTimeline(maxId, wrappedHandler);
    }

    @Override
    protected void addAll(List<Tweet> tweets) {
        super.addAll(tweets);
        if (internetEnabled) {
            // cache tweets
            for (Tweet tweet : tweets) {
                tweet.save();
            }
        }
    }
}
