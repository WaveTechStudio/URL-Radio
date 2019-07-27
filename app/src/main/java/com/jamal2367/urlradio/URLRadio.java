/**
 * URLRadio.java
 * Implements the URLRadio class
 * URLRadio starts up the app and sets up the basic theme (Day / Night)
 *
 * This file is part of
 * URL Radio - Radio App for Android
 *
 * Copyright (c) 2019 - jamal2367.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.jamal2367.urlradio;

import android.app.Application;

import com.jamal2367.urlradio.helpers.LogHelper;
import com.jamal2367.urlradio.helpers.NightModeHelper;


/**
 * URLRadio.class
 */
public class URLRadio extends Application {

    /* Define log tag */
    private static final String LOG_TAG = URLRadio.class.getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();

        // set Day / Night theme state
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        NightModeHelper.restoreSavedState(this);

// todo remove
//        if (Build.VERSION.SDK_INT >= 28) {
//            // Android P might introduce a system wide theme option - in that case: follow system (28 = Build.VERSION_CODES.P)
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//        } else {
//            // try to get last state the user chose
//            NightModeHelper.restoreSavedState(this);
//        }

    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        LogHelper.v(LOG_TAG, "URLRadio application terminated.");
    }

}
