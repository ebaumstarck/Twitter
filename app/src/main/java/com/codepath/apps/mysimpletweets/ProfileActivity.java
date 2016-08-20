package com.codepath.apps.mysimpletweets;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.fragments.UserTimelineFragment;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ProfileActivity extends AppCompatActivity implements ProgressBarContainer {
    public static final String USER_ID_KEY = "userId";

    TwitterClient client;
    User user;
    Bundle savedInstanceState;

    private MenuItem miActionProgressItem;

    @Override
    public void setLoading(boolean loading) {
        if (miActionProgressItem != null) {
            miActionProgressItem.setVisible(loading);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        client = TwitterApplication.getRestClient();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.savedInstanceState = savedInstanceState;

        long userId = getIntent().getLongExtra(USER_ID_KEY, -1);
        if (userId == -1) {
            setLoading(true);
            client.getUserInfo(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    setLoading(false);
                    user = User.fromJSON(response);
                    getSupportActionBar().setTitle("@" + user.getScreenName());
                    populateProfileHeader(user);
                    populateTimeline(user);
                }
            });
        } else {
            setLoading(true);
            client.getUserInfo(userId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                    setLoading(false);
                    if (array.length() == 1) {
                        try {
                            user = User.fromJSON(array.getJSONObject(0));
                            getSupportActionBar().setTitle("@" + user.getScreenName());
                            populateProfileHeader(user);
                            populateTimeline(user);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

    }

    private void populateTimeline(User user) {
        if (savedInstanceState == null) {
            UserTimelineFragment fragmentUserTimeline = UserTimelineFragment.newInstance(user.getScreenName());
            fragmentUserTimeline.setProgressBarContainer(this);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.flContainer, fragmentUserTimeline);
            transaction.commit();
        }
    }

    private void populateProfileHeader(User user) {
        ImageView ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);

        TextView fullName = (TextView) findViewById(R.id.tvFullName);
        TextView tagline = (TextView) findViewById(R.id.tvTagline);
        TextView followers = (TextView) findViewById(R.id.tvFollowers);
        TextView following = (TextView) findViewById(R.id.tvFollowing);

        fullName.setText(user.getName());
        tagline.setText(user.getTagline());
        followers.setText(user.getFollowersCount() + " followers");
        following.setText(user.getFollowingCount() + " following");
        Picasso.with(this).load(user.getProfileImageUrl()).into(ivProfileImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        return true;
    }
}
