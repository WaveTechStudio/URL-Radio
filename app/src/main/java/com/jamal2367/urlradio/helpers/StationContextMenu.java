/**
 * StationContextMenu.java
 * Implements the StationContextMenu class
 * The StationContextMenu allows manipulation of station objects, eg. rename or delete
 *
 * This file is part of
 * URL Radio - Radio Application
 *
 * Copyright (c) 2019 - jamal2367.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.helpers;

import android.app.Activity;
import android.app.DialogFragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.jamal2367.urlradio.MainActivity;
import com.jamal2367.urlradio.R;
import com.jamal2367.urlradio.core.Station;


/**
 * StationContextMenu class
 */
public final class StationContextMenu extends DialogFragment implements URLRadioKeys {

    /* Define log tag */
    private static final String LOG_TAG = StationContextMenu.class.getSimpleName();


    /* Displays context menu */
    public static void show(final Activity activity, final View view, final Station station) {

        PopupMenu popup = new PopupMenu(activity, view);
        popup.inflate(R.menu.menu_list_list_item);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    // CASE ICON
                    case R.id.menu_icon:
                        // get system picker for images
                        ((MainActivity)activity).pickImage(station);
                        return true;

                    // CASE RENAME
                    case R.id.menu_rename:
                        // construct and run rename dialog
                        DialogRename.show(activity, station);
                        return true;

                    // CASE DELETE
                    case R.id.menu_delete:
                        // construct and run delete dialog
                        DialogDelete.show(activity, station);
                        return true;

                    // CASE SHORTCUT
                    case R.id.menu_shortcut: {
                        // create shortcut
                        ShortcutHelper.placeShortcut(activity.getApplication().getApplicationContext(), station);
                        return true;
                    }

                    // CASE DEFAULT
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

}
