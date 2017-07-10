package com.castizer;

import android.content.Context;

public class MySingleton {
    private static MySingleton mInstance;
    //private RequestQueue mRequestQueue;
    //private ImageLoader mImageLoader;
    private static Context mCtx;

    private MySingleton(Context context) {
        mCtx = context;
        //mRequestQueue = getRequestQueue();
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }
/*
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }
*/
/*
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
*/
}
