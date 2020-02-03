/**
 * DialogRename.java
 * Implements the DialogRename class
 * A DialogRename renames a station after asking the user for a new name
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jamal2367.urlradio.MainActivity;
import com.jamal2367.urlradio.R;
import com.jamal2367.urlradio.core.Station;


/**
 * DialogRename class
 */
public final class DialogRename implements URLRadioKeys {

    /* Define log tag */
    private static final String LOG_TAG = DialogRename.class.getSimpleName();


    /* Construct and show dialog */
    public static void show(final Activity activity, final Station station) {
        // prepare dialog builder
        LayoutInflater inflater = LayoutInflater.from(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // get input field
        View view = inflater.inflate(R.layout.dialog_rename_station, null);
        final EditText inputField = (EditText) view.findViewById(R.id.dialog_rename_station_input);
        inputField.setText(station.getStationName());

        // set dialog view
        builder.setView(view);

        // add rename button
        builder.setPositiveButton(R.string.dialog_button_rename, new DialogInterface.OnClickListener() {
            // listen for click on delete button
            public void onClick(DialogInterface arg0, int arg1) {

                // rename station shortcut
                // TODO implement

                // rename station
                String newStationName = inputField.getText().toString();

                // hand updated station over to main activity
                ((MainActivity)activity).handleStationRename(station, newStationName);

            }
        });

        // add cancel button
        builder.setNegativeButton(R.string.dialog_generic_button_cancel, new DialogInterface.OnClickListener() {
            // listen for click on cancel button
            public void onClick(DialogInterface arg0, int arg1) {
                // do nothing
            }
        });

        // display rename dialog
        builder.show();
    }

}