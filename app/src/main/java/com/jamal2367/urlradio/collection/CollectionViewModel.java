/**
 * CollectionViewModel.java
 * Implements the CollectionViewModel class
 * A CollectionViewModel stores the Collection of stations in a ViewModel
 *
 * This file is part of
 * URL Radio - Radio Application
 *
 * Copyright (c) 2019 - jamal2367.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.jamal2367.urlradio.collection;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.jamal2367.urlradio.core.Station;
import com.jamal2367.urlradio.helpers.LogHelper;
import com.jamal2367.urlradio.helpers.StationListHelper;
import com.jamal2367.urlradio.helpers.URLRadioKeys;

import java.util.ArrayList;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * CollectionViewModel.class
 */
public class CollectionViewModel extends AndroidViewModel implements URLRadioKeys {

    /* Define log tag */
    private static final String LOG_TAG = CollectionViewModel.class.getSimpleName();


    /* Main class variables */
    private final MutableLiveData<ArrayList<Station>> mStationListLiveData;
    private final MutableLiveData<Station> mPlayerServiceStationLiveData;
    private final MutableLiveData<Boolean> mTwoPaneLiveData;

    /* Constructor */
    public CollectionViewModel(Application application) {
        super(application);

        // initialize LiveData
        mStationListLiveData = new MutableLiveData<ArrayList<Station>>();
        mPlayerServiceStationLiveData = new MutableLiveData<Station>();
        mTwoPaneLiveData = new MutableLiveData<Boolean>();

        // load state from shared preferences and set live data values
        loadAppState(application);

        // set station from PlayerService to null
        mPlayerServiceStationLiveData.setValue(null);

        // load station list from storage and set live data
        mStationListLiveData.setValue(StationListHelper.loadStationListFromStorage(application));

        // load station list and set live data in background -> not used because DiffResult.dispatchUpdatesTo is causing problems in Adapter
        // new LoadCollectionAsyncTask().execute(application);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }


    /* Loads app state from preferences and updates live data */
    private void loadAppState(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        mTwoPaneLiveData.setValue(settings.getBoolean(PREF_TWO_PANE, false));
        LogHelper.v(LOG_TAG, "Loading state and updating live data.");
    }


    /* Getter for StationList */
    public MutableLiveData<ArrayList<Station>> getStationList() {
        return mStationListLiveData;
    }


    /* Getter for station from PlayerService */
    public MutableLiveData<Station> getPlayerServiceStation() {
        return mPlayerServiceStationLiveData;
    }


    /**
     * Inner class: AsyncTask that loads list in background
     */
    class LoadCollectionAsyncTask extends AsyncTask<Context, Void, ArrayList<Station>> {
        @Override
        protected ArrayList<Station> doInBackground(Context... contexts) {
//            // stress test for DiffResult todo remove
//            try {
//                Thread.sleep(1500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            // load stations in background
            return StationListHelper.loadStationListFromStorage(contexts[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Station> stationList) {
            // set live data
            mStationListLiveData.setValue(stationList);
        }
    }
    /**
     * End of inner class
     */

}
