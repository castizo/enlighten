package com.castizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;

public class MediaButtonReceiver extends BroadcastReceiver {
    @Override

    public void onReceive(Context context, Intent intent) {
        String TAG = "PPP";
        Log.d(TAG, "onReceive");
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.d(TAG, "onReceive - ACTION_MEDIA_BUTTON - event.getKeyCode() " + event.getKeyCode());
            //Toast.makeText(context, "sample", Toast.LENGTH_LONG).show();
            if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
                Log.d(TAG, "onReceive - ACTION_MEDIA_BUTTON - KEYCODE_MEDIA_PLAY_PAUSE - ACTION_UP = " + event.getAction());
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    // Handle key press.
                    Intent myIntent = new Intent("BLUETOOTH_KEYPRESS");
                    myIntent.putExtra("key","KEY_CASTIZER_ACTION");
                    context.sendBroadcast(myIntent);
                }
            } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                // Handle key press.
                Log.d(TAG, "onReceive - ACTION_MEDIA_BUTTON - KEYCODE_MEDIA_NEXT");
            }
        }
    }
}
