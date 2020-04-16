package com.rasmita.myplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Rational;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;


public class MediaSessionPlaybackActivity extends AppCompatActivity {

private static final String TAG = "MediaSessionPlaybackActivity";

public static final long MEDIA_ACTIONS_PLAY_PAUSE =
        PlaybackStateCompat.ACTION_PLAY
        | PlaybackStateCompat.ACTION_PAUSE
        | PlaybackStateCompat.ACTION_PLAY_PAUSE;

public static final long MEDIA_ACTIONS_ALL =
        MEDIA_ACTIONS_PLAY_PAUSE
        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;

private MediaSessionCompat mSession;

private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder =
        new PictureInPictureParams.Builder();

private MovieView mMovieView;

private final View.OnClickListener mOnClickListener =
        new View.OnClickListener() {
@Override
public void onClick(View view) {
        switch (view.getId()) {

        }
        }
        };

private MovieView.MovieListener mMovieListener =
        new MovieView.MovieListener() {

@Override
public void onMovieStarted() {

        updatePlaybackState(
        PlaybackStateCompat.STATE_PLAYING,
        mMovieView.getCurrentPosition(),
        mMovieView.getVideoResourceId());
        }

@Override
public void onMovieStopped() {

        updatePlaybackState(
        PlaybackStateCompat.STATE_PAUSED,
        mMovieView.getCurrentPosition(),
        mMovieView.getVideoResourceId());
        }

@Override
public void onMovieMinimized() {
        minimize();
        }
        };

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View references
        mMovieView = findViewById(R.id.movie);


        // Set up the video; it automatically starts.
        mMovieView.setMovieListener(mMovieListener);
        }

@Override
protected void onStart() {
        super.onStart();
        initializeMediaSession();
        }

private void initializeMediaSession() {
        mSession = new MediaSessionCompat(this, TAG);
        mSession.setFlags(
        MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);
        MediaControllerCompat.setMediaController(this, mSession.getController());

        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, mMovieView.getTitle())
        .build();
        mSession.setMetadata(metadata);

        MediaSessionCallback mMediaSessionCallback = new MediaSessionCallback(mMovieView);
        mSession.setCallback(mMediaSessionCallback);

        int state =
        mMovieView.isPlaying()
        ? PlaybackStateCompat.STATE_PLAYING
        : PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState(
        state,
        MEDIA_ACTIONS_ALL,
        mMovieView.getCurrentPosition(),
        mMovieView.getVideoResourceId());
        }

@Override
protected void onStop() {
        super.onStop();

        mMovieView.pause();
        mSession.release();
        mSession = null;
        }

@Override
protected void onRestart() {
        super.onRestart();
        if (!isInPictureInPictureMode()) {
        mMovieView.showControls();
        }
        }

@Override
public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustFullScreen(newConfig);
        }

@Override
public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
        adjustFullScreen(getResources().getConfiguration());
        }
        }

@Override
public void onPictureInPictureModeChanged(
        boolean isInPictureInPictureMode, Configuration configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, configuration);
        if (!isInPictureInPictureMode) {
        // Show the video controls if the video is not playing
        if (mMovieView != null && !mMovieView.isPlaying()) {
        mMovieView.showControls();
        }
        }
        }

        void minimize() {
        if (mMovieView == null) {
        return;
        }
        mMovieView.hideControls();
        Rational aspectRatio = new Rational(mMovieView.getWidth(), mMovieView.getHeight());
        mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
        enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }

private void adjustFullScreen(Configuration config) {
final View decorView = getWindow().getDecorView();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        decorView.setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        mMovieView.setAdjustViewBounds(false);
        } else {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        mMovieView.setAdjustViewBounds(true);
        }
        }

private void updatePlaybackState(
@PlaybackStateCompat.State int state, int position, int mediaId) {
        long actions = mSession.getController().getPlaybackState().getActions();
        updatePlaybackState(state, actions, position, mediaId);
        }

private void updatePlaybackState(
@PlaybackStateCompat.State int state, long playbackActions, int position, int mediaId) {
        PlaybackStateCompat.Builder builder =
        new PlaybackStateCompat.Builder()
        .setActions(playbackActions)
        .setActiveQueueItemId(mediaId)
        .setState(state, position, 1.0f);
        mSession.setPlaybackState(builder.build());
        }

private class MediaSessionCallback extends MediaSessionCompat.Callback {

    private static final int PLAYLIST_SIZE = 2;

    private MovieView movieView;
    private int indexInPlaylist;

    public MediaSessionCallback(MovieView movieView) {
        this.movieView = movieView;
        indexInPlaylist = 1;
    }

    @Override
    public void onPlay() {
        super.onPlay();
        movieView.play();
    }

    @Override
    public void onPause() {
        super.onPause();
        movieView.pause();
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        movieView.startVideo();
        if (indexInPlaylist < PLAYLIST_SIZE) {
            indexInPlaylist++;
            if (indexInPlaylist >= PLAYLIST_SIZE) {
                updatePlaybackState(
                        PlaybackStateCompat.STATE_PLAYING,
                        MEDIA_ACTIONS_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS,
                        movieView.getCurrentPosition(),
                        movieView.getVideoResourceId());
            } else {
                updatePlaybackState(
                        PlaybackStateCompat.STATE_PLAYING,
                        MEDIA_ACTIONS_ALL,
                        movieView.getCurrentPosition(),
                        movieView.getVideoResourceId());
            }
        }
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        movieView.startVideo();
        if (indexInPlaylist > 0) {
            indexInPlaylist--;
            if (indexInPlaylist <= 0) {
                updatePlaybackState(
                        PlaybackStateCompat.STATE_PLAYING,
                        MEDIA_ACTIONS_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
                        movieView.getCurrentPosition(),
                        movieView.getVideoResourceId());
            } else {
                updatePlaybackState(
                        PlaybackStateCompat.STATE_PLAYING,
                        MEDIA_ACTIONS_ALL,
                        movieView.getCurrentPosition(),
                        movieView.getVideoResourceId());
            }
        }
    }
}

private class SwitchActivityOnClick implements View.OnClickListener {
    @Override
    public void onClick(View view) {
        startActivity(new Intent(view.getContext(), MainActivity.class));
        finish();
    }
}
}
