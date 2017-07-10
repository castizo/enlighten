package com.castizer.util;

import android.graphics.Color;

/**
 * Created by pgarcia on 19/08/15.
 */
public class CastizerConfig {

    public static final Boolean CASTIZER_DEBUG = true;

    //public static final String HOST = "192.168.1.12";
    public static final String HOST = "localhost";

    // Chineese Tablet
    //public static final String PATHTOSDCARD = "/mnt/sdcard2/castizer/";
    // Wiko
    public static final String PATHTOSDCARD = "/mnt/sdcard/castizer/";

    public static final String PATHTOPLAYLISTS = PATHTOSDCARD + "music/";
    public static final String PATH_TO_DOWNLOADS = PATHTOSDCARD + "downloads/";

    public static final int[] color = {Color.BLACK, Color.WHITE, Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};

}
