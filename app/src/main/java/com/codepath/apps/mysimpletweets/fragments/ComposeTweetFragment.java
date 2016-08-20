package com.codepath.apps.mysimpletweets.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TimelineActivity;

/**
 * Created by emma_baumstarck on 8/18/16.
 */
public class ComposeTweetFragment extends DialogFragment {

    private TextView composeLabel;
    private EditText tweetBody;
    private Button cancelButton;
    private Button tweetButton;

    public static ComposeTweetFragment newInstance() {
        return new ComposeTweetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Search Options");
        View view = inflater.inflate(R.layout.compose_fragment, container);

        composeLabel = (TextView) view.findViewById(R.id.composeLabel);
        tweetBody = (EditText) view.findViewById(R.id.tweetBody);
        cancelButton = (Button) view.findViewById(R.id.cancelButton);
        tweetButton = (Button) view.findViewById(R.id.tweetButton);

        tweetBody.setOnKeyListener(
                new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        updateComposeLabel();
                        return false;
                    }
                }
        );
        updateComposeLabel();

        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });

        tweetButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getNumCharactersLeft() < 0) {
                            return;
                        }
                        TimelineActivity timelineActivity = (TimelineActivity) getActivity();
                        timelineActivity.performTweet(tweetBody.getText().toString());
                        dismiss();
                    }
                });

        return view;
    }

    private int getNumCharactersLeft() {
        return 140 - tweetBody.getText().toString().length();
    }

    private void updateComposeLabel() {
        int charactersLeft = getNumCharactersLeft();
        composeLabel.setText("Compose tweet (" + charactersLeft + "):");
        if (charactersLeft < 0) {
            composeLabel.setTextColor(Color.RED);
            tweetButton.setEnabled(false);
        } else if (charactersLeft < 10) {
            composeLabel.setTextColor(Color.argb(255, 255, 165, 0));
            tweetButton.setEnabled(true);
        } else {
            composeLabel.setTextColor(Color.BLACK);
            tweetButton.setEnabled(true);
        }
    }
}
