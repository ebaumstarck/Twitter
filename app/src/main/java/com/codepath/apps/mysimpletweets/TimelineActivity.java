package com.codepath.apps.mysimpletweets;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.mysimpletweets.fragments.ComposeTweetFragment;
import com.codepath.apps.mysimpletweets.fragments.HomeTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.MentionsTimelineFragment;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ProgressBarContainer {

    private TwitterClient client;
    private ViewPager viewPager;
    private TweetsPagerAdapater adapter;
    private MenuItem miActionProgressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new TweetsPagerAdapater(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setViewPager(viewPager);

        client = TwitterApplication.getRestClient();
    }

    @Override
    public void setLoading(boolean loading) {
        if (miActionProgressItem != null) {
            miActionProgressItem.setVisible(loading);
        }
    }

    public class TweetsPagerAdapater extends FragmentPagerAdapter implements ProgressBarContainer {
        private String[] tabTitles = {"Home", "Mentions"};

        private ProgressBarContainer progressBarContainer;

        private HomeTimelineFragment homeTimelineFragment;
        private MentionsTimelineFragment mentionsTimelineFragment;

        public TweetsPagerAdapater(ProgressBarContainer progressBarContainer, FragmentManager fragmentManager) {
            super(fragmentManager);
            this.progressBarContainer = progressBarContainer;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (homeTimelineFragment == null) {
                        homeTimelineFragment = new HomeTimelineFragment();
                        homeTimelineFragment.setProgressBarContainer(this);
                    }
                    return homeTimelineFragment;
                case 1:
                    if (mentionsTimelineFragment == null) {
                        mentionsTimelineFragment = new MentionsTimelineFragment();
                        homeTimelineFragment.setProgressBarContainer(this);
                    }
                    return mentionsTimelineFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public void setLoading(boolean loading) {
            this.progressBarContainer.setLoading(loading);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        return true;
    }

    public void onProfileView(MenuItem menuItem) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void onComposeView(MenuItem menuItem) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ComposeTweetFragment fragment = ComposeTweetFragment.newInstance();
        fragment.show(fragmentManager, "compose_tweet_fragment");
    }

    public void performTweet(String body) {
        client.postUpdate(body, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                viewPager.setCurrentItem(0);
                HomeTimelineFragment fragment = (HomeTimelineFragment) adapter.getItem(0);
                fragment.reloadTweets();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("ERROR", errorResponse.toString());
            }
        });
    }
}
