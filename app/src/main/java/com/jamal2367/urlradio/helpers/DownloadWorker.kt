/*
 * DownloadWorker.kt
 * Implements the DownloadWorker class
 * A DownloadWorker is a worker that triggers download actions when the app is not in use
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
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jamal2367.urlradio.Keys


/*
 * DownloadWorker class
 */
class DownloadWorker(context : Context, params : WorkerParameters): Worker(context, params) {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(DownloadWorker::class.java)


    /* Overrides doWork */
    override fun doWork(): Result {
        // determine what type of download is requested
        when(inputData.getInt(Keys.KEY_DOWNLOAD_WORK_REQUEST,0)) {
            // CASE: update collection
            Keys.REQUEST_UPDATE_COLLECTION -> {
                doOneTimeHousekeeping()
                updateCollection()
            }
        }
        return Result.success()
        // (Returning Result.retry() tells WorkManager to try this task again later; Result.failure() says not to try again.)
    }


    /* Updates collection of stations */
    private fun updateCollection() {
        // todo implement / or delete
    }


    /* Execute one-time housekeeping */
    private fun doOneTimeHousekeeping() {
        if (PreferencesHelper.isHouseKeepingNecessary(applicationContext)) {
            /* add whatever housekeeping is necessary here */

            // housekeeping finished - save state
            // PreferencesHelper.saveHouseKeepingNecessaryState(applicationContext) // TODO uncomment if you need to do housekeeping here
        }
    }

}