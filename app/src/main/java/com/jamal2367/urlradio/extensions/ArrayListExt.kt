/*
 * ArrayListExt.kt
 * Implements the ArrayListExt extension methods
 * Useful extension methods for ArrayLists
 * Source: https://raw.githubusercontent.com/googlesamples/android-UniversalMusicPlayer/master/common/src/main/java/com/example/android/uamp/media/extensions/MediaMetadataCompatExt.kt
 *
 * This file is part of
 * URL Radio - Radio App for Android
 *
 * Copyright (c) 2020 - jamal2367.de
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.extensions


/* Creates a "real" copy of an ArrayList<Long> - useful for preventing concurrent modification issues */
fun ArrayList<Long>.copy(): ArrayList<Long> {
    val copy: ArrayList<Long> = ArrayList<Long>()
    this.forEach { copy.add(it) }
    return copy
}