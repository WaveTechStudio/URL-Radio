/*
 * WorkerHelper.kt
 * Implements the WorkerHelper object
 * A WorkerHelper provides helper methods for starting work jobs
 *
 * This file is part of
 * URL Radio - Radio App for Android
 *
 * Copyright (c) 2020 - jamal2367.de
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.helpers

import androidx.work.*
import com.jamal2367.urlradio.Keys
import java.util.*
import java.util.concurrent.TimeUnit


/*
 * WorkerHelper object
 */
object WorkerHelper {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(WorkerHelper::class.java)


    /* Schedules a DownloadWorker that triggers background updates of the collection periodically */
    fun schedulePeriodicUpdateWorker(): UUID {
        LogHelper.v(TAG, "Starting / Updating periodic work: update collection")
        val requestData: Data = Data.Builder()
                .putInt(Keys.KEY_DOWNLOAD_WORK_REQUEST, Keys.REQUEST_UPDATE_COLLECTION)
                .build()
        val unmeteredConstraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
        val updateCollectionPeriodicWork = PeriodicWorkRequestBuilder<DownloadWorker>(Keys.UPDATE_REPEAT_INTERVAL, TimeUnit.HOURS, 30, TimeUnit.MINUTES)
                //.setConstraints(unmeteredConstraint)
                .setInputData(requestData)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(Keys.NAME_PERIODIC_COLLECTION_UPDATE_WORK,  ExistingPeriodicWorkPolicy.REPLACE, updateCollectionPeriodicWork)
        return updateCollectionPeriodicWork.id
    }

}