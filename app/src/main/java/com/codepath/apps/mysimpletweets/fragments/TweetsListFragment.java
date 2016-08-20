package com.codepath.apps.mysimpletweets.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.codepath.apps.mysimpletweets.ProfileActivity;
import com.codepath.apps.mysimpletweets.ProgressBarContainer;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by emma_baumstarck on 8/18/16.
 */
public abstract class TweetsListFragment extends Fragment {

    private SwipeRefreshLayout swipeContainer;

    private ListView lvTweets;
    private List<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private JsonHttpResponseHandler tweetsLoader;
    private boolean fetching;
    private boolean doneScrolling;

    private MenuItem miActionProgressItem;
    private ProgressBarContainer progressBarContainer;

    protected abstract void loadTweets(long maxId, JsonHttpResponseHandler handler);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweets_list, parent, false);

        lvTweets = (ListView) view.findViewById(R.id.lvTweets);
        lvTweets.setAdapter(aTweets);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadTweets();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        fetching = false;
        doneScrolling = false;
        tweetsLoader = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG", response.toString());
                acceptTweets(Tweet.fromJSONArray(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("ERROR", errorResponse == null ? "null" : errorResponse.toString());
                finishFetch();
            }
        };

        lvTweets.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d("DEBUG", "done scrolling: "  + doneScrolling);
                if (doneScrolling) {
                    return;
                }

                int numItems = firstVisibleItem + visibleItemCount;
                if (numItems > 10 && numItems >= totalItemCount) {
                    Log.d("SCROLLING", firstVisibleItem + "/" + visibleItemCount + "/" + totalItemCount);
                    loadMoreTweets();
                }
            }
        });

        loadMoreTweets();

        lvTweets.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        intent.putExtra(ProfileActivity.USER_ID_KEY, tweets.get(i).getUser().getUid());
                        startActivity(intent);
                    }
                }
        );

        return view;
    }

    private void finishFetch() {
        setLoading(false);
        doneScrolling = true;
        fetching = false;
        swipeContainer.setRefreshing(false);
    }

    protected void acceptTweets(List<Tweet> tweets) {
        finishFetch();
        if (tweets.size() == 0) {
            doneScrolling = true;
        } else {
            doneScrolling = false;
            addAll(tweets);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(getActivity(), tweets);
    }

    protected void addAll(List<Tweet> tweets) {
        aTweets.addAll(tweets);
    }

    private void clearTweets() {
        aTweets.clear();
        doneScrolling = false;
    }

    public void reloadTweets() {
        clearTweets();
        loadMoreTweets();
    }

    private void loadMoreTweets() {
        if (fetching) {
            return;
        }

        setLoading(true);
        if (tweets.size() == 0) {
            fetching = true;
            loadTweets(-1, tweetsLoader);
        } else {
            // load from last tweet
            fetching = true;
            loadTweets(tweets.get(tweets.size() - 1).getUid(), tweetsLoader);
        }
    }

    public void setProgressBarContainer(ProgressBarContainer progressBarContainer) {
        this.progressBarContainer = progressBarContainer;
    }

    private void setLoading(boolean loading) {
        if (progressBarContainer != null) {
            progressBarContainer.setLoading(loading);
        }
    }
}
