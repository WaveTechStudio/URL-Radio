/**
 * DialogDelete.java
 * Implements the DialogDelete class
 * A DialogDelete deletes a station after asking the user for permission
 *
 * This file is part of
 * URL Radio - Radio Application
 *
 * Copyright (c) 2020 - jamal2367.de
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.jamal2367.urlradio.MainActivity;
import com.jamal2367.urlradio.PlayerService;
import com.jamal2367.urlradio.R;
import com.jamal2367.urlradio.core.Station;


/**
 * DialogDelete class
 */
public final class DialogDelete implements URLRadioKeys {

    /* Define log tag */
    private static final String LOG_TAG = DialogDelete.class.getSimpleName();


    /* Construct and show dialog */
    public static void show(final Activity activity, final Station station) {
        // stop player service using intent
        Intent intent = new Intent(activity, PlayerService.class);
        intent.setAction(ACTION_DISMISS);
        activity.startService(intent);
        LogHelper.v(LOG_TAG, "Opening delete dialog. Stopping player service.");

        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(activity);

        // add message to dialog
        deleteDialog.setMessage(R.string.dialog_delete_station_message);

        // add delete button
        deleteDialog.setPositiveButton(R.string.dialog_button_delete, new DialogInterface.OnClickListener() {
            // listen for click on delete button
            public void onClick(DialogInterface arg0, int arg1) {
                // hand station over to main activity
                ((MainActivity)activity).handleStationDelete(station);
            }
        });

        // add cancel button
        deleteDialog.setNegativeButton(R.string.dialog_generic_button_cancel, new DialogInterface.OnClickListener() {
            // listen for click on cancel button
            public void onClick(DialogInterface arg0, int arg1) {
                // do nothing
            }
        });

        // display delete dialog
        deleteDialog.show();
    }

}
