package com.castizer;

import com.castizer.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Browser;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static android.view.KeyEvent.*;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

//    private static final String TAG = MyActivity.class.getSimpleName();
    private static final String TAG = "DDD";


    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private static final String json_command_play_pause = "http://localhost:8080/jsonrpc?request={\"jsonrpc\": \"2.0\", \"method\": \"Player.PlayPause\", \"params\": { \"playerid\": 0 }, \"id\": 1}";
    private static String json_command_castizer_control;
    // = "RunScript("special://skin/scripts/castizer_control.py", "NULL")

    // Mediaplayer
    private MediaPlayer mPlayer;
    SongsManager songManager;

    private Button buttonState;

    final String MEDIA_PATH = "/storage/sdcard1/anna";
    //private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private String mp3Pattern = ".mp3";

    private int currentSongIndex = 0;
    private int playlist_number = 0;
    private String playlist_path;
    private static final String PATHTOPLAYLISTS = "/mnt/sdcard2/castizer/music/";
    private IntentFilter intentFilter_noisy;

    private List<File> songsList;

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                if(file.getName().endsWith(".mp3") || file.getName().endsWith(".wav")){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    private class myNoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // Pause the playback
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        /*
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });
*/
        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    //mSystemUiHider.toggle();
                } else {
                    //mSystemUiHider.show();
                }
            }

        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.button_01).setOnTouchListener(mDelayHideTouchListener);
        Button button01 = (Button) findViewById(R.id.button_01);
        button01.setOnClickListener(onClickListener);
        Button button02 = (Button) findViewById(R.id.button_02);
        button02.setOnClickListener(onClickListener);
        Button button03 = (Button) findViewById(R.id.button_03);
        button03.setOnClickListener(onClickListener);
        Button button04 = (Button) findViewById(R.id.button_04);
        button04.setOnClickListener(onClickListener);
        Button button05 = (Button) findViewById(R.id.button_05);
        button05.setOnClickListener(onClickListener);
        Button button06 = (Button) findViewById(R.id.button_06);
        button06.setOnClickListener(onClickListener);

        buttonState = (Button) findViewById(R.id.buttonState);

        mPlayer =   new MediaPlayer();

        ImageButton imageButtonCastizerLogo = (ImageButton) findViewById(R.id.imageButtonCastizerLogo);
        imageButtonCastizerLogo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Toast.makeText(FullscreenActivity.this,
                        "pausing...", Toast.LENGTH_SHORT).show();
                buttonState.setText("");
                mPlayer.pause();

            }

        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {

                boolean isRepeat = false;
                boolean isShuffle = true;

                if (!isPlaying()) return;

                // check for repeat is ON or OFF
                if(isRepeat){
                    // repeat is on play same song again
                    playSong(currentSongIndex);
                } else if(isShuffle){
                    // shuffle is on - play a random song
                    Random rand = new Random();
                    currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
                    playSong(currentSongIndex);
                } else{
                    // no repeat or shuffle ON - play next song
                    if(currentSongIndex < (songsList.size() - 1)){
                        playSong(currentSongIndex + 1);
                        currentSongIndex = currentSongIndex + 1;
                    }else{
                        // play first song
                        playSong(0);
                        currentSongIndex = 0;
                    }
                }

            }
        });

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //AudioManager am = mContext.getSystemService(Context.AUDIO_SERVICE);
        // Start listening for button presses
        MediaButtonReceiver mbr;

        ComponentName myEventReceiver = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        am.registerMediaButtonEventReceiver(myEventReceiver);
        // Stop listening for button presses
        //am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);

        registerReceiver(broadcastReceiver, new IntentFilter("BLUETOOTH_KEYPRESS"));
        registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        Log.d(TAG, "onCreate(): event registered!");

    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                Log.e("PPP action:", "PGARCIA");
                // Pause the playback
                issueCommand(getApplicationContext(), json_command_play_pause);
            }

            String value = "ERROR";
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if(extras.containsKey("key")){
                    value=extras.get("key").toString();
                }
            }
            Log.e("PPP value:", value);
            if (value.equals("KEY_CASTIZER_CLICK")){
                playlist_number += 1;
                if (playlist_number > 3) {
                    playlist_number = 1;
                }
                //PATHTOPLAYLISTS = xbmc.getInfoLabel( '$INFO[Skin.String(Setting.CastizerPlaylist)]' )
                playlist_path = PATHTOPLAYLISTS + playlist_number + "/";
                //PlayMedia(" + playlist_path + ")";
                json_command_castizer_control = "http://192.168.0.10:8080/jsonrpc?request={\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\": { \"directory\" : \"" + playlist_path + "\" } } }";
                Log.e("PPP command:", json_command_castizer_control);
                Toast.makeText(context, "PPP playlist_path: " + playlist_path, Toast.LENGTH_LONG).show();
                issueCommand(getApplicationContext(), json_command_castizer_control);
            } else if (value.equals("KEY_CASTIZER_DOUBLE_CLICK")){
                issueCommand(getApplicationContext(), json_command_play_pause);
            }
            //Toast.makeText(context, "ONE_CLICK ! - " + value, Toast.LENGTH_LONG).show();
            mPlayer = MediaPlayer.create(FullscreenActivity.this, R.raw.sonar);
            mPlayer.start();

        }
    };

    private boolean isPlaying(){
        return (buttonState.getText().equals("PLAYING"));
    }


    /** Open another app.
     * @param context current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean issueCommand(Context context, String url) {
        String packageName = "com.android.chrome";
        Log.e("PPP URL:", url);
        PackageManager manager = context.getPackageManager();
        try {
            Intent mBrowserIntent = manager.getLaunchIntentForPackage(packageName);
            if (mBrowserIntent == null) {
                return false;
                //throw new PackageManager.NameNotFoundException();
            }
            mBrowserIntent.setData(Uri.parse(url));
            mBrowserIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            //mBrowserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mBrowserIntent.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            mBrowserIntent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
            context.startActivity(mBrowserIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            //mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/";
    }

    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public void  playSong(int songIndex){

        // Play song
        try {
            mPlayer.reset();
            String songToPlayPath = songsList.get(songIndex).getAbsolutePath();
            Log.e("FILE TO PLAY:", songToPlayPath);
            mPlayer.setDataSource(songToPlayPath);
            mPlayer.prepare();
            mPlayer.start();
            // Displaying Song title
            //String songTitle = songsList.get(songIndex).get("songTitle");
            //songTitleLabel.setText(songTitle);

            // Changing Button Image to pause image
            buttonState.setText("PLAYING");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void play() {

        // check if next song is there or not
        if(currentSongIndex < (songsList.size() - 1)){
            playSong(currentSongIndex + 1);
            currentSongIndex = currentSongIndex + 1;
        }else{
            // play first song
            playSong(0);
            currentSongIndex = 0;
        }

    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {

            int listNumber = 1;
            boolean playButton = true;

            switch(v.getId()){
                /*
                case R.id.button_00:
                    Toast.makeText(FullscreenActivity.this,
                            "Playing music !", Toast.LENGTH_SHORT).show();
                    MediaPlayer mPlayer = MediaPlayer.create(FullscreenActivity.this, R.raw.sonar);
                    mPlayer.start();
                    break;
                */
                case R.id.button_01:
                    listNumber = 1;
                    break;
                case R.id.button_02:
                    listNumber = 2;
                    break;
                case R.id.button_03:
                    listNumber = 3;
                    break;
                case R.id.button_04:
                    //openApp(getApplicationContext(), "com.android.chrome");
                    issueCommand(getApplicationContext(), json_command_play_pause);
                    Toast.makeText(FullscreenActivity.this,
                            "Launching App !", Toast.LENGTH_SHORT).show();
                    playButton = false;
                    break;
                case R.id.button_05:
                    //listNumber = 5;
                    Toast.makeText(FullscreenActivity.this,
                            "Playing music !", Toast.LENGTH_SHORT).show();
                    mPlayer = MediaPlayer.create(FullscreenActivity.this, R.raw.sonar);
                    mPlayer.start();
                    playButton = false;

                    break;
                case R.id.button_06:
                    playButton = false;
                    System.exit(0);
                    break;
                default:
                    Toast.makeText(FullscreenActivity.this,
                            "Button pressed !", Toast.LENGTH_SHORT).show();
                    playButton = false;
                    break;
                }

                if (playButton) {
                    //File dirTest = new File("/storage/sdcard1/" + listNumber); // MyAndroidTablet
                    File dirTest = new File("/mnt/external_sd/" + listNumber);
                    Log.d("PABLO_dirTest", dirTest.toString());

                    songsList = getListFiles(dirTest);
                    if (songsList != null)
                        for (int i = 0; i < songsList.size(); ++i) {
                            Log.e("FILE2:", songsList.get(i).getAbsolutePath());
                        }
                    play();
                }

        }
    };

}
