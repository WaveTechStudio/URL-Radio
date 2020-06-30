/*
 * ShortcutHelper.kt
 * Implements the ShortcutHelper object
 * A ShortcutHelper creates and handles station shortcuts on the Home screen
 *
 * This file is part of
 * URL Radio - Radio App for Android
 *
 * Copyright (c) 2020 - jamal2367.de
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.helpers

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.jamal2367.urlradio.Keys
import com.jamal2367.urlradio.PlayerServiceStarterActivity
import com.jamal2367.urlradio.R
import com.jamal2367.urlradio.core.Station


/*
 * ShortcutHelper object
 */
object ShortcutHelper {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(ShortcutHelper::class.java)


    /* Places shortcut on Home screen */
    fun placeShortcut(context: Context, station: Station) {
        // credit: https://medium.com/@BladeCoder/using-support-library-26-0-0-you-can-do-bb75911e01e8
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val shortcut: ShortcutInfoCompat = ShortcutInfoCompat.Builder(context, station.name)
                    .setShortLabel(station.name)
                    .setLongLabel(station.name)
                    .setIcon(createShortcutIcon(context, station.image, station.imageColor))
                    .setIntent(createShortcutIntent(context, station.uuid))
                    .build()
            ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
            Toast.makeText(context, R.string.toastmessage_shortcut_created, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, R.string.toastmessage_shortcut_not_created, Toast.LENGTH_LONG).show()
        }
    }


    /* Removes shortcut for given station from Home screen */
    fun removeShortcut(context: Context, station: Station) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // from API level 26 ("Android O") on shortcuts are handled by ShortcutManager, which cannot remove shortcuts. The user must remove them manually.
        } else {
            // the pre 26 way: create and launch intent put shortcut on Home screen
            val stationImageBitmap: Bitmap = ImageHelper.getStationImage(context, station.image.toUri(),192)
            val removeIntent = Intent()
            removeIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, station.name)
            removeIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, ImageHelper.createSquareImage(context, stationImageBitmap, station.imageColor, 192, false))
            removeIntent.putExtra("duplicate", false)
            removeIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, createShortcutIntent(context, station.uuid))
            removeIntent.action = "com.android.launcher.action.UNINSTALL_SHORTCUT"
            context.applicationContext.sendBroadcast(removeIntent)
        }
    }


    /* Creates Intent for a station shortcut */
    private fun createShortcutIntent(context: Context, stationUuid: String): Intent {
        val shortcutIntent = Intent(context, PlayerServiceStarterActivity::class.java)
        shortcutIntent.action = Keys.ACTION_START_PLAYER_SERVICE
        shortcutIntent.putExtra(Keys.EXTRA_STATION_UUID, stationUuid)
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return shortcutIntent
    }


    /* Create shortcut icon */
    private fun createShortcutIcon(context: Context, stationImage: String, stationImageColor: Int): IconCompat {
        val stationImageBitmap: Bitmap = ImageHelper.getStationImage(context, stationImage.toUri(),192)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IconCompat.createWithAdaptiveBitmap(ImageHelper.createSquareImage(context, stationImageBitmap, stationImageColor, 192, true))
        } else {
            IconCompat.createWithAdaptiveBitmap(ImageHelper.createSquareImage(context, stationImageBitmap, stationImageColor, 192, false))
        }
    }

}