package com.castizer;

import com.castizer.util.CastizerConfig;
import com.castizer.util.SystemUiHider;
import com.castizer.Configuration;

import android.app.Activity;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    private static final String TAG = "PPP.com.castizer";
    //private static final String TAG = "PPP com.castizer";
    private boolean isReceiverRegistered = false;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private Button buttonState;

    // Mediaplayer
    private MediaPlayer mPlayer;
    private CastizerPlayer castizerPlayer;
    //SongsManager songManager;

    private IntentFilter intentFilter_noisy;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private AudioManager audioManager;

    private int color_index = 0;
    private boolean isPlaying = false;
    private int currentSongIndex = 0;
    private int playlist = 0;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mmDevice;
    private String address = "90:F1:AA:E5:9A:27";
    private Set<BluetoothDevice> pairedDevices;
    private Set<BluetoothDevice> connectedDevices;

    private Button button_action;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        color_index = 0;

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
        Button button_TEST = (Button) findViewById(R.id.button_TEST);
        button_TEST.setOnClickListener(onClickListener);
        Button button_DEBUG = (Button) findViewById(R.id.button_DEBUG);
        button_DEBUG.setOnClickListener(onClickListener);
        Button button_EXIT = (Button) findViewById(R.id.button_EXIT);
        button_EXIT.setOnClickListener(onClickListener);

        button_action = (Button) findViewById(R.id.button_action);
        button_action.setOnClickListener(onClickListener);

        /*
        Button button04 = (Button) findViewById(R.id.button_04);
        button04.setOnClickListener(onClickListener);
        Button button05 = (Button) findViewById(R.id.button_action);
        button05.setOnClickListener(onClickListener);
        Button button06 = (Button) findViewById(R.id.button_06);
        button06.setOnClickListener(onClickListener);
         */
        Button button_LEFT = (Button) findViewById(R.id.button_LEFT);
        button_LEFT.setOnClickListener(onClickListener);
        Button button_MIDDLE = (Button) findViewById(R.id.button_MIDDLE);
        button_MIDDLE.setOnClickListener(onClickListener);
        Button button_RIGHT = (Button) findViewById(R.id.button_RIGHT);
        button_RIGHT.setOnClickListener(onClickListener);

        if (!CastizerConfig.CASTIZER_DEBUG) {

            LinearLayout linearLayoutButtons;
            linearLayoutButtons = (LinearLayout) findViewById(R.id.IDLinearLayoutButtons);
            linearLayoutButtons.setVisibility(View.GONE);
/*
            button_TEST.setVisibility(View.INVISIBLE);
            button_DEBUG.setVisibility(View.INVISIBLE);
            button_EXIT.setVisibility(View.INVISIBLE);
            button_LEFT.setVisibility(View.INVISIBLE);
            button_MIDDLE.setVisibility(View.INVISIBLE);
            button_RIGHT.setVisibility(View.INVISIBLE);
  */
        }

        if (isExternalStorageReadable())
            Toast.makeText(FullscreenActivity.this, "SD Readable !", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(FullscreenActivity.this, "SD NOT READABLE !", Toast.LENGTH_SHORT).show();
        }

        /*
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
        */

        castizerPlayer = new CastizerPlayer(getApplicationContext());

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    // Pause playback
                    Log.i(TAG, "OnAudioFocusChangeListener - AUDIOFOCUS_LOSS_TRANSIENT");
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Resume playback
                    Log.i(TAG, "OnAudioFocusChangeListener - AUDIOFOCUS_GAIN");
                    if (!isReceiverRegistered) {

                        Log.d(TAG, "registerReceiver");
                        //registerReceiver(myReceiver, new IntentFilter(...));
                        registerReceiver(broadcastReceiver, new IntentFilter("BLUETOOTH_KEYPRESS"));
                        registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
                        registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
                        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));

                        isReceiverRegistered = true;

                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    // Stop playback
                    Log.i(TAG, "OnAudioFocusChangeListener - AUDIOFOCUS_LOSS");
                    Log.d(TAG, "unregisterReceiver");
                    try {
                        unregisterReceiver(broadcastReceiver);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    isReceiverRegistered = false;
                }
            }
        };

        mPlayer =   new MediaPlayer();

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Log.d(TAG, "onResume(): Requesting audio focus... ");
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
        } else {
            Log.d(TAG, "onResume(): OTHER ");
        }

        //TODO: Use MediaSession instead... but only from Lollipop !
        //http://developer.android.com/reference/android/media/session/MediaSession.html#setMediaButtonReceiver%28android.app.PendingIntent%29
        // Start listening for button presses
        ComponentName myEventReceiver = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(myEventReceiver);
        // Stop listening for button presses
        //am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);

        if (!isReceiverRegistered) {

            //registerReceiver(myReceiver, new IntentFilter(...));
            registerReceiver(broadcastReceiver, new IntentFilter("BLUETOOTH_KEYPRESS"));
            registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
            registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));

            Log.d(TAG, "onResume(): events registered!");

            isReceiverRegistered = true;

        }


    }

    private boolean isPlaying(){
        return (isPlaying);
    }

    public void switchOn() {
/*
        Log.d(TAG, "switchOn()");
        if (playlist_number == 0){
            nextPlaylist();
        } else {
            play();
        }
  */
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        /*
        if (isReceiverRegistered) {
            Log.d(TAG, "onPause() - unregisterReceiver");
            try {
                //unregisterReceiver(broadcastReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            isReceiverRegistered = false;
        }
        */
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
            //buttonState.setText("PLAYING");
            Log.d(TAG, "Playing !");
            isPlaying = true;

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

    public void pause() {

        if(mPlayer.isPlaying()){
            isPlaying = false;
            mPlayer.pause();
            Log.i(TAG, "pause(): mPlayer was playing... now paused!");
        } else {
            Log.i(TAG, "pause(): mPlayer was not playing!");
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive()");

            String action = intent.getAction();

            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                Log.i(TAG, "ACTION_AUDIO_BECOMING_NOISY");
                //castizerPlayer.switchOff();
                Toast.makeText(context, "BLUETOOTH OFF !", Toast.LENGTH_LONG).show();
                pause();
            }

            if (AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED.equals(action)) {
                Log.i(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED");
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
                    if (value.equals("KEY_CASTIZER_CLICK")){
                        Log.d(TAG, "Event received : KEY_CASTIZER_CLICK");
                        int pl = castizerPlayer.nextPlaylist();
                        Toast.makeText(context, "playlist: " + pl, Toast.LENGTH_LONG).show();
                        //Toast.makeText(context, "playlist: DEBUGGING !" , Toast.LENGTH_LONG).show();
                    } else if (value.equals("KEY_CASTIZER_DOUBLE_CLICK")){
                        Log.d(TAG, "Event received : KEY_CASTIZER_DOUBLE_CLICK");
                        castizerPlayer.playPause();
                        Toast.makeText(context, "playPause", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "Event received : other value: " + value);
                    }
                    mPlayer = MediaPlayer.create(FullscreenActivity.this, R.raw.sonar);
                    mPlayer.start();
                }
            }

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

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
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

                case R.id.button_action:
                    playlist = castizerPlayer.nextPlaylist();
                    Toast.makeText(FullscreenActivity.this, "playlist: " + playlist, Toast.LENGTH_LONG).show();
                    mPlayer.reset();
                    try {
                        mPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://com.castizer/" + R.raw.sonar));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mPlayer.start();

                    button_action.setBackgroundColor(CastizerConfig.color[playlist]);

                    //File dir = Environment.getExternalStorageDirectory();
                    String path = Environment.getExternalStorageDirectory().toString() + "/castizer/" + playlist;
                    Log.d(TAG, "START");
                    File dirTest = new File(path);
                    songsList = getListFiles(dirTest);

                    if (songsList != null)
                        for (int i=0; i<songsList.size(); ++i)
                        {
                            Log.e("FILE2:", songsList.get(i).getAbsolutePath());
                        }

                    play();

                    break;

                //case R.id.button_02:
                  //  castizerPlayer.playPause();
                    //break;
                /*
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
 */
                case R.id.button_TEST:

                    Toast.makeText(FullscreenActivity.this,
                            "Button !", Toast.LENGTH_SHORT).show();
                    mPlayer = MediaPlayer.create(FullscreenActivity.this, R.raw.sound_1);
                    mPlayer.start();

                    break;

                case R.id.button_DEBUG:
/*
                    playlist = castizerPlayer.nextPlaylist();
                    Toast.makeText(FullscreenActivity.this, "playlist: " + playlist, Toast.LENGTH_LONG).show();
                    mPlayer.reset();
                    try {
                        mPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://com.castizer/" + R.raw.sonar));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mPlayer.start();

                    Button buttonMain = (Button) findViewById(R.id.button02);
                    buttonMain.setBackgroundColor(CastizerConfig.color[playlist]);

                    //File dir = Environment.getExternalStorageDirectory();
                    String path = Environment.getExternalStorageDirectory().toString() + "/castizer/" + playlist;
                    Log.d(TAG, "START");
                    File dirTest = new File(path);
                    songsList = getListFiles(dirTest);

                    if (songsList != null)
                        for (int i=0; i<songsList.size(); ++i)
                        {
                            Log.e("FILE2:", songsList.get(i).getAbsolutePath());
                        }

                    play();

                    break;
  */
                case R.id.button_EXIT:
                    System.exit(0);
                    break;

                case R.id.button_LEFT:


                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if(mBluetoothAdapter == null)
                    {
                        Toast.makeText(getApplicationContext(), "No bluetooth adapter available", Toast.LENGTH_LONG).show();
                        // myLabel.setText("No bluetooth adapter available");
                    }

                    if(!mBluetoothAdapter.isEnabled())
                    {
                        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetooth, 0);
                    }

                    pairedDevices = mBluetoothAdapter.getBondedDevices();
                    mmDevice = mBluetoothAdapter.getRemoteDevice(address);
                    if (pairedDevices.contains(mmDevice))
                    {
                        //statusText.setText("Bluetooth Device Found, address: " + mmDevice.getAddress() );
                        Log.d(TAG, "TEST1: BT is paired");
                    }
                    Toast.makeText(getApplicationContext(), "Bluetooth Device Found", Toast.LENGTH_LONG).show();
                    //myLabel.setText("Bluetooth Device Found");

                    break;

                case R.id.button_MIDDLE:
/*
                    if (bluetooth.isEnabled()) {
                        String toastText = bluetooth.getName() + " : " + bluetooth.getAddress();
                        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
                    }
*/
                break;

                case R.id.button_RIGHT:

                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    pairedDevices = mBluetoothAdapter.getBondedDevices();

                    List<String> s = new ArrayList<String>();
                    for(BluetoothDevice bt : pairedDevices){
                        Log.d(TAG, "BT DEVICE: " + bt.getName().toString());
                        Log.d(TAG, "   " + bt.getAddress().toString());
                        Log.d(TAG, "   " + bt.getBondState());
                    }

                    /*
                    btDevices = mBluetoothHeadset.getConnectedDevices();
                    if ( btDevices.isEmpty() ) {
                        returnNow = true;
                        logMessage = "onHandleIntent/closeProfileProxy/No connected BT devices.";
                    }
                    else if ( mBluetoothHeadset.getConnectionState(btDevices.get(0)) != BluetoothProfile.STATE_CONNECTED)  {
                        returnNow = true;
                        logMessage = "onHandleIntent/closeProfileProxy/No connected headset.";
                    }
                    */
                    break;

                default:
                    Toast.makeText(FullscreenActivity.this,
                            "Button pressed !", Toast.LENGTH_SHORT).show();
                break;

                }

        }
    };

}
