/*
 * DownloadFinishedReceiver.kt
 * Implements the DownloadFinishedReceiver class
 * A DownloadFinishedReceiver listens for finished downloads
 *
 * This file is part of
 * URL Radio - Radio App for Android
 *
 * Copyright (c) 2020 - jamal2367.de
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.helpers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/*
 * DownloadFinishedReceiver class
 */
class DownloadFinishedReceiver : BroadcastReceiver() {


    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(DownloadFinishedReceiver::class.java)


    /* Overrides onReceive */
    override fun onReceive(context: Context, intent: Intent) {
        // process the finished download
        DownloadHelper.processDownload(context, intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L))
    }
}