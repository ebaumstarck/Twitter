package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;

import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Created by emma_baumstarck on 8/18/16.
 */
public class UserTimelineFragment extends TweetsListFragment {
    private static final String SCREEN_NAME_KEY = "screen_name";
    private TwitterClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = TwitterApplication.getRestClient();
    }

    public static UserTimelineFragment newInstance(String screenName) {
        UserTimelineFragment fragment = new UserTimelineFragment();
        Bundle args = new Bundle();
        args.putString(SCREEN_NAME_KEY, screenName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void loadTweets(long maxId, JsonHttpResponseHandler handler) {
        client.getUserTimeline(maxId, getArguments().getString(SCREEN_NAME_KEY), handler);
    }
}
