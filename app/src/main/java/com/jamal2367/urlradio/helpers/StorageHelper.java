/**
 * StorageHelper.java
 * Implements the StorageHelper class
 * A StorageHelper provides reliable access to Androids external storage
 *
 * This file is part of
 * URL Radio - Radio Application
 *
 * Copyright (c) 2020 - jamal2367.de
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.helpers;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import androidx.core.os.EnvironmentCompat;


/**
 * StorageHelper class
 */
public final class StorageHelper {

    /* Define log tag */
    private static final String LOG_TAG = StorageHelper.class.getSimpleName();


    /* Getter for collection directory */
    public static File getCollectionDirectory(Context context) {
        return findCollectionDirectory(context);
    }


    /* Checks if given folder holds any m3u files */
    public static boolean storageHasStationPlaylistFiles(Context context) {
        File collectionDirectory = findCollectionDirectory(context);
        if (!collectionDirectory.isDirectory()) {
            LogHelper.i(LOG_TAG, "Given file object is not a directory.");
            return false;
        }
        File[] listOfFiles = collectionDirectory.listFiles();
        for (File file : listOfFiles) {
            if (file.getPath().endsWith(".m3u")) {
                return true;
            }
        }
        LogHelper.i(LOG_TAG, "External storage does not contain any station playlist files.");
        return false;
    }


    /* Return a write-able sub-directory from external storage  */
    private static File findCollectionDirectory(Context context) {
        String subDirectory = "Collection";
        File[] storage = context.getExternalFilesDirs(subDirectory);
        for (File file : storage) {
            if (file != null) {
                String state = EnvironmentCompat.getStorageState(file);
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    LogHelper.v(LOG_TAG, "External storage: " + file.toString());
                    return file;
                }
            }
        }
        return null;
    }

}
