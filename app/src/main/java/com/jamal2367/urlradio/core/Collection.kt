/*
 * Collection.kt
 * Implements the Collection class
 * A Collection object holds a list of radio stations
 *
 * This file is part of
 * URL Radio - Radio App for Android
 *
 * Copyright (c) 2020 - jamal2367.de
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.jamal2367.urlradio.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize
import com.jamal2367.urlradio.Keys
import java.util.*


/*
 * Collection class
 */
@Keep
@Parcelize
data class Collection(@Expose val version: Int = Keys.CURRENT_COLLECTION_CLASS_VERSION_NUMBER,
                      @Expose var stations: MutableList<Station> = mutableListOf<Station>(),
                      @Expose var modificationDate: Date = Date()) : Parcelable {

    /* overrides toString method */
    override fun toString(): String {
        val stringBuilder: StringBuilder = StringBuilder()
        stringBuilder.append("Format version: $version\n")
        stringBuilder.append("Number of stations in collection: ${stations.size}\n\n")
        stations.forEach {
            stringBuilder.append("$it\n")
        }
        return stringBuilder.toString()
    }


    /* Creates a deep copy of a Collection */
    fun deepCopy(): Collection {
        val stationsCopy: MutableList<Station> = mutableListOf<Station>()
        stations.forEach { stationsCopy.add(it.deepCopy()) }
        return Collection(version = version,
                          stations = stationsCopy,
                          modificationDate = modificationDate)
    }

}