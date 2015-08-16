package com.castizer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

/**
 * Created by pgarcia on 16/08/15.
 */
public class CastizerPlayer {

    private static final boolean DEBUGGING = false;

    private static final String TAG = "CastizerPlayer";
    private static final String KODI_PROCESS = "org.xbmc.kodi";

    private static final String KODI_HOST = "localhost";
    private static final String KODI_PORT = "8080";

    private static final String json_command_play_pause = "http://" + KODI_HOST + ":" + KODI_PORT + "/jsonrpc?request={\"jsonrpc\":\"2.0\",\"method\":\"Player.PlayPause\",\"params\":{\"playerid\":0},\"id\":1}";
    private static final String json_command_stop = "http://" + KODI_HOST + ":" + KODI_PORT + "/jsonrpc?request={\"jsonrpc\":\"2.0\",\"method\":\"Player.Stop\",\"params\":{\"playerid\":0},\"id\":1}";
    private static final String json_command_play = "http://" + KODI_HOST + ":" + KODI_PORT + "/jsonrpc?request={\"jsonrpc\":\"2.0\",\"method\":\"Player.PlayPause\",\"params\":{\"playerid\":0,\"play\":true},\"id\":1}";
    private static final String json_command_pause = "http://" + KODI_HOST + ":" + KODI_PORT + "/jsonrpc?request={\"jsonrpc\":\"2.0\",\"method\":\"Player.PlayPause\",\"params\":{\"playerid\":0,\"play\":false},\"id\":1}";

    private static String json_command_castizer_control;

    private static final String PATHTOPLAYLISTS = "/mnt/sdcard2/castizer/music/";

    private int playlist_number = 0;

    private static Context mContext;

    /*
        private int currentSongIndex = 0;
        private String playlist_path;

    */





    public CastizerPlayer (Context context)
    {
        mContext =context;
        playlist_number = 0;
        checkKodiRunning();
    }


    /** Open another app.
     * @param context current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    private static boolean openApp(String packageName) {
        PackageManager manager = mContext.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "openApp() ERROR");
            return false;
        }
    }

    private boolean isProcessRunning(String process) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService( mContext.ACTIVITY_SERVICE );
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++){
            if (procInfos.get(i).processName.equals(process)) {
                return true;
            }
        }
        return false;
    }

    private void kodiCommand(String command) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(mContext);

        // Request a string response from the provided URL.
        Log.d(TAG, "JSON Request: " + command);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, command,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, "JSON Response: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Most likely KODI was not there
                Log.d(TAG, "JSON Response: ERROR !!! is Kodi open ??? ");
                checkKodiRunning();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void launchKodi(){
        Log.e(TAG, "launchKodi()");
        openApp(KODI_PROCESS);
    }

    private void checkKodiRunning() {
        if (isProcessRunning(KODI_PROCESS)) {
            Log.d(TAG, "checkKodiRunning() - Kodi is running !");
        } else {
            Log.w(TAG, "checkKodiRunning() - Kodi NOT running !");
            playlist_number = 0;
            launchKodi();
        }
    }

    private void stop() {
        Log.d(TAG, "stop()");
        kodiCommand(json_command_stop);
    }

    private void play() {
        Log.d(TAG, "play()");
        kodiCommand(json_command_play);
    }

    private void pause() {
        Log.d(TAG, "pause()");
        kodiCommand(json_command_pause);
    }

    //////////////////////////////////////////////////////////////////////
    // public methods
    //////////////////////////////////////////////////////////////////////


    public void testCheckKodiRunning() {
        checkKodiRunning();
    }

    public int nextPlaylist() {
        Log.d(TAG, "nextPlaylist()");
        String playlist_path;
        playlist_number += 1;
        if (playlist_number > 3) {
            playlist_number = 1;
        }
        if (DEBUGGING){
            playlist_path = PATHTOPLAYLISTS + "d" + playlist_number + "/";
        }else{
            playlist_path = PATHTOPLAYLISTS + playlist_number + "/";
        }


        json_command_castizer_control = "http://192.168.0.10:8080/jsonrpc?request={\"jsonrpc\":\"2.0\",\"method\":\"Player.Open\",\"params\":{\"item\":{\"directory\":\"" + playlist_path + "\"}}}";
        Log.d(TAG, "command: " + json_command_castizer_control);
        Log.d(TAG, "playlist_path: " + playlist_path);
        kodiCommand(json_command_castizer_control);
        return playlist_number;
    }

    public void playPause() {
        Log.d(TAG, "playPause()");
        kodiCommand(json_command_play_pause);
    }

    public void switchOff() {
        Log.d(TAG, "switchOff()");
        pause();
    }

    public void switchOn() {
        Log.d(TAG, "switchOn()");
        if (playlist_number == 0){
            nextPlaylist();
        } else {
            play();
        }
    }

}
