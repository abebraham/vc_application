package com.example.daxing.qualitytest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayerView;



public class PlayVideoActivity extends YouTubeFailureRecoveryActivity implements TextView.OnEditorActionListener, YouTubePlayer.OnInitializedListener, View.OnClickListener {
    private static final String TAG = PlayVideoActivity.class.getSimpleName();

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private StringBuilder logString;
    private static final String KEY_CURRENTLY_SELECTED_ID = "currentlySelectedId";

    String video_id;

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer player;

    private TextView tv_video_status;
    private TextView tv_log_info;

    private Button b_play;
    private Button b_pause;

    private EditText et_skip;

    private MyPlaylistEventListener playlistEventListener;
    private MyPlayerStateChangeListener playerStateChangeListener;
    private MyPlaybackEventListener playbackEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_play_video);
        Intent intent = getIntent();
        video_id = intent.getStringExtra(testControlActivity.VIDEO_ID_MESSAGE);
        Log.i(TAG,"Video ID is " + video_id);
        setUI();
        playlistEventListener = new MyPlaylistEventListener();
        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();
        logString = new StringBuilder();
    }

    protected void setUI() {
        Log.i(TAG, "Set UI");
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubePlayerView.initialize(DeveloperKey.DEVELOPER_KEY, this);

        tv_video_status = (TextView) findViewById(R.id.video_status);

        tv_log_info = (TextView) findViewById(R.id.tv_log_info);

        b_play = (Button) findViewById(R.id.b_play);
        b_play.setOnClickListener(this);

        b_pause = (Button) findViewById(R.id.b_pause);
        b_pause.setOnClickListener(this);

        et_skip = (EditText) findViewById(R.id.skip_to_text);
        et_skip.setOnEditorActionListener(this);
        Log.i(TAG, "UI finished");

    }

    private void setControlsEnabled(boolean enabled) {
        b_play.setEnabled(enabled);
        b_pause.setEnabled(enabled);
        et_skip.setEnabled(enabled);
    }

    protected void updateText() {
        Log.i(TAG, "Update Text");
        tv_video_status.setText(String.format("Current state: %s %s %s",
                playerStateChangeListener.playerState, playbackEventListener.playbackState,
                playbackEventListener.bufferingState));
    }

    private void log(String message) {
        logString.append(message + "\n");
        tv_log_info.setText(logString);
    }



    private static final int parseInt(String intString, int defaultValue) {
        try {
            return intString != null ? Integer.valueOf(intString) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        return (hours == 0 ? "" : hours + ":")
                + String.format("%02d:%02d", minutes % 60, seconds % 60);
    }

    private String getTimesText() {
        int currentTimeMillis = player.getCurrentTimeMillis();
        int durationMillis = player.getDurationMillis();
        return String.format("(%s/%s)", formatTime(currentTimeMillis), formatTime(durationMillis));
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        Log.i(TAG, "Initialize YouTubePlayer");
        System.out.println("Initialize YouTubePlayer");
        this.player = player;
        Log.i(TAG,"after set player");
        player.setPlaylistEventListener(playlistEventListener);
        Log.i(TAG,"after set Listener");
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);

        if (!wasRestored) {
            Log.i(TAG, "play video");
            playVideo();
        }
        setControlsEnabled(true);
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubePlayerView;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.i(TAG, "Editor Action");
        if (v == et_skip) {
            int skipToSecs = parseInt(et_skip.getText().toString(), 0);
            player.seekToMillis(skipToSecs * 1000);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_skip.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private void playVideo() {
        Log.i(TAG, "Play Video with Video ID " + video_id);
        //player.cueVideo(video_id);
        player.loadVideo(video_id);
    }

    @Override
    public void onClick(View view) {
        if (view == b_play) {
            player.play();
        } else if (view == b_pause) {
            player.pause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        //super.onSaveInstanceState(state);
        state.putString(KEY_CURRENTLY_SELECTED_ID, video_id);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        video_id = state.getString(KEY_CURRENTLY_SELECTED_ID);
    }


    private final class MyPlaylistEventListener implements PlaylistEventListener {
        @Override
        public void onNext() {
            log("NEXT VIDEO");
        }

        @Override
        public void onPrevious() {
            log("PREVIOUS VIDEO");
        }

        @Override
        public void onPlaylistEnded() {
            log("PLAYLIST ENDED");
        }
    }

    private final class MyPlaybackEventListener implements PlaybackEventListener {
        String playbackState = "NOT_PLAYING";
        String bufferingState = "";
        @Override
        public void onPlaying() {
            playbackState = "PLAYING";
            updateText();
            log("\tPLAYING " + getTimesText());
        }

        @Override
        public void onBuffering(boolean isBuffering) {
            bufferingState = isBuffering ? "(BUFFERING)" : "";
            updateText();
            log("\t\t" + (isBuffering ? "BUFFERING " : "NOT BUFFERING ") + getTimesText());
        }

        @Override
        public void onStopped() {
//            playbackState = "STOPPED";
//            updateText();
//            log("\tSTOPPED");
        }

        @Override
        public void onPaused() {
            playbackState = "PAUSED";
            updateText();
            log("\tPAUSED " + getTimesText());
        }

        @Override
        public void onSeekTo(int endPositionMillis) {
            log(String.format("\tSEEKTO: (%s/%s)",
                    formatTime(endPositionMillis),
                    formatTime(player.getDurationMillis())));
        }
    }

    private final class MyPlayerStateChangeListener implements PlayerStateChangeListener {
        String playerState = "UNINITIALIZED";

        @Override
        public void onLoading() {
            playerState = "LOADING";
            updateText();
            log(playerState);
        }

        @Override
        public void onLoaded(String videoId) {
            playerState = String.format("LOADED %s", videoId);
            updateText();
            log(playerState);
        }

        @Override
        public void onAdStarted() {
            playerState = "AD_STARTED";
            updateText();
            log(playerState);
        }

        @Override
        public void onVideoStarted() {
            playerState = "VIDEO_STARTED";
            updateText();
            log(playerState);
        }

        @Override
        public void onVideoEnded() {
            playerState = "VIDEO_ENDED";
            updateText();
            log(playerState);
        }

        @Override
        public void onError(ErrorReason reason) {
            playerState = "ERROR (" + reason + ")";
            if (reason == ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // When this error occurs the player is released and can no longer be used.
                player = null;
                setControlsEnabled(false);
            }
            updateText();
            log(playerState);
        }

    }
}