/**
 * CollectionAdapterDiffUtilCallback.java
 * Implements a DiffUtil Callback
 * A CollectionAdapterDiffUtilCallback is a DiffUtil.Callback that compares two lists of stations
 *
 * This file is part of
 * URL Radio - Radio Application
 *
 * Copyright (c) 2020 - jamal2367.de
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.jamal2367.urlradio.collection;

import com.jamal2367.urlradio.core.Station;
import com.jamal2367.urlradio.helpers.URLRadioKeys;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;


/**
 * CollectionAdapterDiffUtilCallback class
 */
public class CollectionAdapterDiffUtilCallback extends DiffUtil.Callback implements URLRadioKeys {

    /* Define log tag */
    private static final String LOG_TAG = CollectionAdapterDiffUtilCallback.class.getSimpleName();


    /* Main class variables */
    private final ArrayList<Station> mOldStations;
    private final ArrayList<Station> mNewStations;

    /* Constructor */
    public CollectionAdapterDiffUtilCallback(ArrayList<Station> oldStations, ArrayList<Station> newStations) {
        mOldStations = oldStations;
        mNewStations = newStations;
    }


    @Override
    public int getOldListSize() {
        return mOldStations.size();
    }


    @Override
    public int getNewListSize() {
        return mNewStations.size();
    }


    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Called by the DiffUtil to decide whether two objects represent the same Item.
        Station oldStation = mOldStations.get(oldItemPosition);
        Station newStation = mNewStations.get(newItemPosition);
        return oldStation.getStreamUri().equals(newStation.getStreamUri());
    }


    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Called by the DiffUtil when it wants to check whether two items have the same data. DiffUtil uses this information to detect if the contents of an item has changed.
        Station oldStation = mOldStations.get(oldItemPosition);
        Station newStation = mNewStations.get(newItemPosition);
        if (oldStation.getStationName().equals(newStation.getStationName()) &&
                oldStation.getPlaybackState() == newStation.getPlaybackState() &&
                oldStation.getStationImageSize() == newStation.getStationImageSize()) {
            return true;
        } else {
            return false;
        }
    }


    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Station oldStation = mOldStations.get(oldItemPosition);
        Station newStation = mNewStations.get(newItemPosition);
        if (!(oldStation.getStationName().equals(newStation.getStationName()))) {
            return HOLDER_UPDATE_NAME;
        } else if (oldStation.getPlaybackState() != newStation.getPlaybackState()) {
            return HOLDER_UPDATE_PLAYBACK_STATE;
        } else if (oldStation.getStationImageSize() != newStation.getStationImageSize()) {
            return HOLDER_UPDATE_IMAGE;
        } else if (oldStation.getSelectionState() != newStation.getSelectionState()) {
            return HOLDER_UPDATE_SELECTION_STATE;
        } else {
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }

}