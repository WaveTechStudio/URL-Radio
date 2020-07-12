/*
 * DownloadHelper.kt
 * Implements the DownloadHelper object
 * A DownloadHelper provides helper methods for downloading files
 *
 * This file is part of
 * URL Radio - Radio App for Android
 *
 * Copyright (c) 2015-20 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.helpers

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import com.jamal2367.urlradio.Keys
import com.jamal2367.urlradio.R
import com.jamal2367.urlradio.core.Collection
import com.jamal2367.urlradio.core.Station
import com.jamal2367.urlradio.extensions.copy
import java.util.*


/*
 * DownloadHelper object
 */
object DownloadHelper {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(DownloadHelper::class.java)


    /* Main class variables */
    private lateinit var collection: Collection
    private lateinit var downloadManager: DownloadManager
    private lateinit var activeDownloads: ArrayList<Long>
    private lateinit var modificationDate: Date


    /* Download station playlists */
    fun downloadPlaylists(context: Context, playlistUrlStrings: Array<String>) {
        // initialize main class variables, if necessary
        initialize(context)
        // convert array
        val uris: Array<Uri> = Array<Uri>(playlistUrlStrings.size) { index -> Uri.parse(playlistUrlStrings[index]) }
        // enqueue playlists
        enqueueDownload(context, uris, Keys.FILE_TYPE_PLAYLIST)
    }


    /* Refresh image of given station */
    fun updateStationImage(context: Context, station: Station) {
        // initialize main class variables, if necessary
        initialize(context)
        // check if station has an image reference
        if (station.remoteImageLocation.isNotEmpty()) {
            CollectionHelper.clearImagesFolder(context, station)
            val uris: Array<Uri> = Array(1) { station.remoteImageLocation.toUri() }
            enqueueDownload(context, uris, Keys.FILE_TYPE_IMAGE)
        }
    }


    /* Updates all station images */
    fun updateStationImages(context: Context) {
        // initialize main class variables, if necessary
        initialize(context)
        // re-download all station images
        PreferencesHelper.saveLastUpdateCollection(context)
        val uris: MutableList<Uri> = mutableListOf()
        collection.stations.forEach { station ->
            station.radioBrowserStationUuid
            if (!station.imageManuallySet) {
                uris.add(station.remoteImageLocation.toUri())
            }
        }
        enqueueDownload(context, uris.toTypedArray(), Keys.FILE_TYPE_IMAGE)
        LogHelper.i(TAG, "Updating all station images.")
    }


    /* Processes a given download ID */
    fun processDownload(context: Context, downloadId: Long) {
        // initialize main class variables, if necessary
        initialize(context)
        // get local Uri in content://downloads/all_downloads/ for download ID
        val downloadResult: Uri? = downloadManager.getUriForDownloadedFile(downloadId)
        if (downloadResult == null) {
            val downloadErrorCode: Int = getDownloadError(downloadId)
            val downloadErrorFileName: String = getDownloadFileName(downloadManager, downloadId)
            Toast.makeText(context, "${context.getString(R.string.toastmessage_error_download_error)}: $downloadErrorFileName ($downloadErrorCode)", Toast.LENGTH_LONG).show()
            LogHelper.w(TAG, "Download not successful: File name = $downloadErrorFileName Error code = $downloadErrorCode")
            removeFromActiveDownloads(context, arrayOf(downloadId), deleteDownload = true)
            return
        } else {
            val localFileUri: Uri = downloadResult
            // get remote URL for download ID
            val remoteFileLocation: String = getRemoteFileLocation(downloadManager, downloadId)
            // determine what to do
            val fileType = FileHelper.getContentType(context, localFileUri)
            if ((fileType in Keys.MIME_TYPES_M3U || fileType in Keys.MIME_TYPES_PLS) && CollectionHelper.isNewStation(collection, remoteFileLocation)) {
                addStation(context, localFileUri, remoteFileLocation)
            } else if ((fileType in Keys.MIME_TYPES_M3U || fileType in Keys.MIME_TYPES_PLS) && !CollectionHelper.isNewStation(collection, remoteFileLocation)) {
                updateStation(context, localFileUri, remoteFileLocation)
            } else if (fileType in Keys.MIME_TYPES_IMAGE) {
                collection = CollectionHelper.setStationImageWithRemoteLocation(context, collection, localFileUri, remoteFileLocation, false)
            } else if (fileType in Keys.MIME_TYPES_FAVICON) {
                collection = CollectionHelper.setStationImageWithRemoteLocation(context, collection, localFileUri, remoteFileLocation, false)
            }
            // remove ID from active downloads
            removeFromActiveDownloads(context, arrayOf(downloadId))
        }
    }


    /* Initializes main class variables of DownloadHelper, if necessary */
    private fun initialize(context: Context) {
        if (!this::modificationDate.isInitialized) {
            modificationDate = PreferencesHelper.loadCollectionModificationDate(context)
        }
        if (!this::collection.isInitialized || CollectionHelper.isNewerCollectionAvailable(context, modificationDate)) {
            collection = FileHelper.readCollection(context) // todo make async
            modificationDate = PreferencesHelper.loadCollectionModificationDate(context)
        }
        if (!this::downloadManager.isInitialized) {
            FileHelper.clearFolder(context.getExternalFilesDir(Keys.FOLDER_TEMP), 0)
            downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        }
        if (!this::activeDownloads.isInitialized) {
            activeDownloads = getActiveDownloads(context)
        }
    }


    /* Enqueues an Array of files in DownloadManager */
    private fun enqueueDownload(context: Context, uris: Array<Uri>, type: Int, ignoreWifiRestriction: Boolean = false) {
        // determine allowed network types
        val allowedNetworkTypes: Int = determineAllowedNetworkTypes(context, type, ignoreWifiRestriction)
        // enqueue downloads
        val newIds = LongArray(uris.size)
        for (i in uris.indices) {
            LogHelper.v(TAG, "DownloadManager enqueue: ${uris[i]}")
            // check if valid url and prevent double download
            val scheme: String = uris[i].scheme ?: String()
            if (scheme.startsWith("http") && isNotInDownloadQueue(uris[i].toString())) {
                val fileName: String = uris[i].pathSegments.last() ?: String()
                val request: DownloadManager.Request = DownloadManager.Request(uris[i])
                        .setAllowedNetworkTypes(allowedNetworkTypes)
                        .setTitle(fileName)
                        .setDestinationInExternalFilesDir(context, Keys.FOLDER_TEMP, fileName)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                newIds[i] = downloadManager.enqueue(request)
                activeDownloads.add(newIds[i])
            }
        }
        setActiveDownloads(context, activeDownloads)
    }


    /* Checks if a file is not yet in download queue */
    private fun isNotInDownloadQueue(remoteFileLocation: String): Boolean {
        val activeDownloadsCopy = activeDownloads.copy()
        activeDownloadsCopy.forEach { downloadId ->
            if (getRemoteFileLocation(downloadManager, downloadId) == remoteFileLocation) {
                LogHelper.w(TAG, "File is already in download queue: $remoteFileLocation")
                return false
            }
        }
        LogHelper.v(TAG, "File is not in download queue.")
        return true
    }


    /* Safely remove given download IDs from activeDownloads and delete download if requested */
    private fun removeFromActiveDownloads(context: Context, downloadIds: Array<Long>, deleteDownload: Boolean = false): Boolean {
        // remove download ids from activeDownloads
        val success: Boolean = activeDownloads.removeAll { downloadId -> downloadIds.contains(downloadId) }
        if (success) {
            setActiveDownloads(context, activeDownloads)
        }
        // optionally: delete download
        if (deleteDownload) {
            downloadIds.forEach { downloadId -> downloadManager.remove(downloadId) }
        }
        return success
    }


    /* Saves collection of radio station to storage */
    private fun saveCollection(context: Context, opmlExport: Boolean = false) {
        // save collection (not async) - and store modification date
        modificationDate = CollectionHelper.saveCollection(context, collection, async = false)
    }


    /* Reads station playlist file and adds it to collection */
    private fun addStation(context: Context, localFileUri: Uri, remoteFileLocation: String) {
        // read station playlist
        val station: Station = CollectionHelper.createStationFromPlaylistFile(context, localFileUri, remoteFileLocation)
        // detect content type on background thread
        GlobalScope.launch {
            val deferred: Deferred<NetworkHelper.ContentType> = async(Dispatchers.Default) { NetworkHelper.detectContentTypeSuspended(station.getStreamUri()) }
            // wait for result
            val contentType: NetworkHelper.ContentType = deferred.await()
            LogHelper.e(TAG, "DING $localFileUri $remoteFileLocation ${contentType.type}") // todo remove
            // set content type
            station.streamContent = contentType.type
            // add station and save collection
            collection = CollectionHelper.addStation(context, collection, station)
        }
    }


    /* Reads station playlist file and updates it in collection */
    private fun updateStation(context: Context, localFileUri: Uri, remoteFileLocation: String) {
        // read station playlist
        val station: Station = CollectionHelper.createStationFromPlaylistFile(context, localFileUri, remoteFileLocation)
        // detect content type on background thread
        GlobalScope.launch {
            val deferred: Deferred<NetworkHelper.ContentType> = async(Dispatchers.Default) { NetworkHelper.detectContentTypeSuspended(station.getStreamUri()) }
            // wait for result
            val contentType: NetworkHelper.ContentType = deferred.await()
            // update content type
            station.streamContent = contentType.type
            // update station and save collection
            collection = CollectionHelper.updateStation(context, collection, station)
        }
    }


    /* Saves active downloads (IntArray) to shared preferences */
    private fun setActiveDownloads(context: Context, activeDownloads: ArrayList<Long>) {
        val builder = StringBuilder()
        for (i in activeDownloads.indices) {
            builder.append(activeDownloads[i]).append(",")
        }
        var activeDownloadsString: String = builder.toString()
        if (activeDownloadsString.isEmpty()) {
            activeDownloadsString = Keys.ACTIVE_DOWNLOADS_EMPTY
        }
        PreferencesHelper.saveActiveDownloads(context, activeDownloadsString)
    }


    /* Loads active downloads (IntArray) from shared preferences */
    private fun getActiveDownloads(context: Context): ArrayList<Long> {
        var inactiveDownloadsFound: Boolean = false
        val activeDownloadsList: ArrayList<Long> = arrayListOf<Long>()
        val activeDownloadsString: String = PreferencesHelper.loadActiveDownloads(context)
        val count = activeDownloadsString.split(",").size - 1
        val tokenizer = StringTokenizer(activeDownloadsString, ",")
        repeat(count) {
            val token = tokenizer.nextToken().toLong()
            when (isDownloadActive(token)) {
                true -> activeDownloadsList.add(token)
                false -> inactiveDownloadsFound = true
            }
        }
        if (inactiveDownloadsFound) setActiveDownloads(context, activeDownloadsList)
        return activeDownloadsList
    }


    /* Determines the remote file location (the original URL) */
    private fun getRemoteFileLocation(downloadManager: DownloadManager, downloadId: Long): String {
        var remoteFileLocation: String = ""
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.count > 0) {
            cursor.moveToFirst()
            remoteFileLocation = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
        }
        return remoteFileLocation
    }


    /* Determines the file name for given download id (the original URL) */
    private fun getDownloadFileName(downloadManager: DownloadManager, downloadId: Long): String {
        var remoteFileLocation: String = ""
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.count > 0) {
            cursor.moveToFirst()
            remoteFileLocation = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
        }
        return remoteFileLocation
    }


    /* Checks if a given download ID represents a finished download */
    private fun isDownloadFinished(downloadId: Long): Boolean {
        var downloadStatus: Int = -1
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.count > 0) {
            cursor.moveToFirst()
            downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        }
        return (downloadStatus == DownloadManager.STATUS_SUCCESSFUL)
    }


    /* Checks if a given download ID represents a finished download */
    private fun isDownloadActive(downloadId: Long): Boolean {
        var downloadStatus: Int = -1
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.count > 0) {
            cursor.moveToFirst()
            downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        }
        return downloadStatus == DownloadManager.STATUS_RUNNING
    }


    /* Retrieves reason of download error - returns http error codes plus error codes found here check: https://developer.android.com/reference/android/app/DownloadManager */
    private fun getDownloadError(downloadId: Long): Int {
        var reason: Int = -1
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.count > 0) {
            cursor.moveToFirst()
            val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            if (downloadStatus == DownloadManager.STATUS_FAILED) {
                reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
            }
        }
        return reason
    }


    /* Determine allowed network type */
    private fun determineAllowedNetworkTypes(context: Context, type: Int, ignoreWifiRestriction: Boolean): Int {
        var allowedNetworkTypes: Int =  (DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        // restrict download of audio files to WiFi if necessary
        if (type == Keys.FILE_TYPE_AUDIO) {
            val downloadOverMobile = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Keys.PREF_DOWNLOAD_OVER_MOBILE, Keys.DEFAULT_DOWNLOAD_OVER_MOBILE)
            if (!ignoreWifiRestriction && !downloadOverMobile) {
                allowedNetworkTypes = DownloadManager.Request.NETWORK_WIFI
            }
        }
        return allowedNetworkTypes
    }

}