package com.castizer;

import com.castizer.util.SystemUiHider;

import android.app.Activity;
import android.app.SearchManager;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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

    private static final String TAG = "com.castizer";

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private Button buttonState;

    // Mediaplayer
    private MediaPlayer mPlayer;
    private CastizerPlayer castizerPlayer;

    private IntentFilter intentFilter_noisy;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

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

        castizerPlayer = new CastizerPlayer(getApplicationContext());

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    // Pause playback
                    Log.i(TAG, "OnAudioFocusChangeListener - AUDIOFOCUS_LOSS_TRANSIENT");
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Resume playback
                    Log.i(TAG, "OnAudioFocusChangeListener - AUDIOFOCUS_GAIN");
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    // Stop playback
                    Log.i(TAG, "OnAudioFocusChangeListener - AUDIOFOCUS_LOSS");
                }
            }
        };

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                // Hint: the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.i(TAG, "requestAudioFocus - AUDIOFOCUS_REQUEST_GRANTED");
        }
        else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            Log.e(TAG, "requestAudioFocus - AUDIOFOCUS_REQUEST_FAILED !!!");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");

        //TODO: Use MediaSession instead... but only from Lollipop !
        //http://developer.android.com/reference/android/media/session/MediaSession.html#setMediaButtonReceiver%28android.app.PendingIntent%29
        // Start listening for button presses
        ComponentName myEventReceiver = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(myEventReceiver);
        // Stop listening for button presses
        //am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);


        //registerReceiver(myReceiver, new IntentFilter(...));
        registerReceiver(broadcastReceiver, new IntentFilter("BLUETOOTH_KEYPRESS"));
        registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));

        Log.d(TAG, "onCreate(): event registered!");

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                Log.i(TAG, "ACTION_AUDIO_BECOMING_NOISY");
                castizerPlayer.switchOff();
            }

            if (AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED.equals(action)) {
                Log.i(TAG, "IMPORTANT !!! " + "ACTION_SCO_AUDIO_STATE_UPDATED");
                // Pause the playback ???
            }

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
                Log.i(TAG, "BT connected !!!");
                Toast.makeText(getApplicationContext(), "BT Connected !", Toast.LENGTH_SHORT).show();
                try {
                    // Give time to the audio to switch from the phone speaker to the BT speaker
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                castizerPlayer.switchOn();
            }

            String value = "ERROR";
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey("key")) {
                    value = extras.get("key").toString();
                    Log.d(TAG, "value: " + value);
                    if (value.equals("KEY_CASTIZER_CLICK")){
                        Log.d(TAG, "Event received : KEY_CASTIZER_CLICK");
                        int pl = castizerPlayer.nextPlaylist();
                        Toast.makeText(context, "playlist: " + pl, Toast.LENGTH_LONG).show();
                    } else if (value.equals("KEY_CASTIZER_DOUBLE_CLICK")){
                        Log.d(TAG, "Event received : KEY_CASTIZER_DOUBLE_CLICK");
                        castizerPlayer.playPause();
                        Toast.makeText(context, "playPause", Toast.LENGTH_LONG).show();
                    }
                }
            }

            mPlayer = MediaPlayer.create(FullscreenActivity.this, R.raw.sonar);
            mPlayer.start();

        }
    };

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

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {

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
                    castizerPlayer.nextPlaylist();
                    break;
                case R.id.button_02:
                    castizerPlayer.playPause();
                    break;
                case R.id.button_03:
                    //castizerPlayer.testCheckKodiRunning();

                    Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
                    i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                    sendOrderedBroadcast(i, null);

                    i = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
                    i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
                    sendOrderedBroadcast(i, null);

                    break;
                case R.id.button_04:

                    /*
                    // right click on a track in Spotify to get the URI, or use the Web API.
                    String spotify_uri = "spotify:artist:5lsC3H1vh9YSRQckyGv0Up";
                    //String uri = "spotify:track:<spotify uri>";
                    Intent launcher = new Intent( Intent.ACTION_VIEW, Uri.parse(spotify_uri) );
                    startActivity(launcher);
                    */


                    final Intent intent1 = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                    intent1.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.MainActivity"));
                    intent1.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*");
                    //intent1.putExtra(SearchManager.QUERY, "michael jackson smooth criminal");
                    //intent1.putExtra(SearchManager.QUERY, "celine dion all the way");
                    //intent1.putExtra(SearchManager.QUERY, "heroes del silencio tumbas de sal");
                    intent1.putExtra(SearchManager.QUERY, "vicente fernandez");
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (intent1.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent1);
                    }
                                        /*
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                    intent.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.MainActivity"));
                    intent.putExtra(SearchManager.QUERY, "michael jackson smooth criminal");

                    try {
                        startActivity(intent);
                    }catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "You must first install Spotify", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.spotify.mobile.android.ui"));
                        startActivity(i);
                    }
                     */

                    //castizerPlayer.switchOff();
                    break;
                case R.id.button_05:
                    castizerPlayer.switchOn();
                    Toast.makeText(FullscreenActivity.this,
                            "Playing music !", Toast.LENGTH_SHORT).show();
                    mPlayer = MediaPlayer.create(FullscreenActivity.this, R.raw.sonar);
                    mPlayer.start();

                    break;
                case R.id.button_06:
                    System.exit(0);
                    break;
                default:
                    Toast.makeText(FullscreenActivity.this,
                            "Button pressed !", Toast.LENGTH_SHORT).show();
                    break;
                }

        }
    };

}
