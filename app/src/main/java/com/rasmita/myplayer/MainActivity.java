package com.rasmita.myplayer;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    ListView lv;
    ListAdapter lAdapter;
    public static final String[] descriptions = new String[]{
            "1 year Ago * 141 views",
            "3 year Ago * 143 views", "10 year Ago * 142 views",
            "1 year Ago * 41 views"};

    public static final Integer[] images = {R.drawable.tmnjr,
            R.drawable.bgbn, R.drawable.mkms, R.drawable.lnkng};
    public static final String[] titles = new String[]{"Tom and Jerry",
            "Big Bunny", "Micky Mouse", "The Lion King"};
    ArrayList<RowItem> rowItems;
    private static final String ACTION_MEDIA_CONTROL = "media_control";
    private static final String EXTRA_CONTROL_TYPE = "control_type";
    private static final int REQUEST_PLAY = 1;
    private static final int REQUEST_PAUSE = 2;
    private static final int REQUEST_INFO = 3;

    /**
     * The intent extra value for play action.
     */
    private static final int CONTROL_TYPE_PLAY = 1;

    /**
     * The intent extra value for pause action.
     */
    private static final int CONTROL_TYPE_PAUSE = 2;

    /**
     * The arguments to be used for Picture-in-Picture mode.
     */
    private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder =
            new PictureInPictureParams.Builder();


    private MovieView mMovieView;
    private BroadcastReceiver mReceiver;

    private String mPlay;
    private String mPause;

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
                    updatePictureInPictureActions(
                            R.mipmap.baseline_pause_white_24, mPause, CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
                }

                @Override
                public void onMovieStopped() {
                    updatePictureInPictureActions(
                            R.mipmap.baseline_play_arrow_white_24, mPlay, CONTROL_TYPE_PLAY, REQUEST_PLAY);
                }

                @Override
                public void onMovieMinimized() {
                    minimize();
                }
            };

    void updatePictureInPictureActions(
            @DrawableRes int iconId, String title, int controlType, int requestCode) {
        final ArrayList<RemoteAction> actions = new ArrayList<>();

        final PendingIntent intent =
                PendingIntent.getBroadcast(
                        MainActivity.this,
                        requestCode,
                        new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
                        0);
        Icon icon = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            icon = Icon.createWithResource(MainActivity.this, iconId);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            actions.add(new RemoteAction(icon, title, title, intent));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            actions.add(
                    new RemoteAction(
                            Icon.createWithResource(MainActivity.this, R.mipmap.baseline_info_white_24),
                            getString(R.string.info),
                            getString(R.string.info_description),
                            PendingIntent.getActivity(
                                    MainActivity.this,
                                    REQUEST_INFO,
                                    new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(getString(R.string.info_uri))),
                                    0)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mPictureInPictureParamsBuilder.setActions(actions);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.lv);
        rowItems = new ArrayList<RowItem>();
        for (int i = 0; i < titles.length; i++) {
            RowItem item = new RowItem(images[i], titles[i], descriptions[i]);
            rowItems.add(item);
        }
        ItemListViewAdapter adapter = new ItemListViewAdapter(this,
                rowItems);
        lv.setAdapter(adapter);

        mPlay = getString(R.string.play);
        mPause = getString(R.string.pause);

        mMovieView = (MovieView) findViewById(R.id.movie);

        // Set up the video; it automatically starts.
        mMovieView.setMovieListener(mMovieListener);
    }

    @Override
    protected void onStop() {

        mMovieView.pause();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode()) {
                // Show the video controls so the video can be easily resumed.
                mMovieView.showControls();
            }
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
        if (isInPictureInPictureMode) {
            // Starts receiving events from action items in PiP mode.
            mReceiver =
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (intent == null
                                    || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                                return;
                            }

                            // This is where we are called back from Picture-in-Picture action
                            // items.
                            final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
                            switch (controlType) {
                                case CONTROL_TYPE_PLAY:
                                    mMovieView.play();
                                    break;
                                case CONTROL_TYPE_PAUSE:
                                    mMovieView.pause();
                                    break;
                            }
                        }
                    };
            registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
        } else {
            unregisterReceiver(mReceiver);
            mReceiver = null;
            if (mMovieView != null && !mMovieView.isPlaying()) {
                mMovieView.showControls();
            }
        }
    }


    void minimize() {
        if (mMovieView == null) {
            return;
        }
        // Hide the controls in picture-in-picture mode.
        mMovieView.hideControls();
        // Calculate the aspect ratio of the PiP screen.
        Rational aspectRatio = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            aspectRatio = new Rational(mMovieView.getWidth(), mMovieView.getHeight());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
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

    private class SwitchActivityOnClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(view.getContext(), MediaSessionPlaybackActivity.class));
            finish();
        }
    }
}
