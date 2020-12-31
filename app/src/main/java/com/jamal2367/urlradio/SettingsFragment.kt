/*
 * SettingsFragment.kt
 * Implements the SettingsFragment fragment
 * A SettingsFragment displays the user accessible settings of the app
 *
 * This file is part of
 * URL Radio - Radio App for Android
 *
 * Copyright (c) 2015-21 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.jamal2367.urlradio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.jamal2367.urlradio.dialogs.ErrorDialog
import com.jamal2367.urlradio.dialogs.YesNoDialog
import com.jamal2367.urlradio.helpers.AppThemeHelper
import com.jamal2367.urlradio.helpers.LogHelper
import com.jamal2367.urlradio.helpers.NetworkHelper


/*
 * SettingsFragment class
 */
class SettingsFragment: PreferenceFragmentCompat(), YesNoDialog.YesNoDialogListener {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(SettingsFragment::class.java)


    /* Overrides onViewCreated from PreferenceFragmentCompat */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set the background color
        view.setBackgroundColor(resources.getColor(R.color.app_window_background, null))
        // show action bar
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.fragment_settings_title)
        // set the navigation bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        activity?.window!!.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.app_window_background)
        }
    }


    /* Overrides onCreatePreferences from PreferenceFragmentCompat */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)


        // set up "App Theme" preference
        val preferenceThemeSelection: ListPreference = ListPreference(activity as Context)
        preferenceThemeSelection.title = getString(R.string.pref_theme_selection_title)
        preferenceThemeSelection.setIcon(R.drawable.ic_brush_24dp)
        preferenceThemeSelection.key = Keys.PREF_THEME_SELECTION
        preferenceThemeSelection.summary = "${getString(R.string.pref_theme_selection_summary)} ${AppThemeHelper.getCurrentTheme(activity as Context)}"
        preferenceThemeSelection.entries = arrayOf(getString(R.string.pref_theme_selection_mode_device_default), getString(R.string.pref_theme_selection_mode_light), getString(R.string.pref_theme_selection_mode_dark))
        preferenceThemeSelection.entryValues = arrayOf(Keys.STATE_THEME_FOLLOW_SYSTEM, Keys.STATE_THEME_LIGHT_MODE, Keys.STATE_THEME_DARK_MODE)
        preferenceThemeSelection.setDefaultValue(Keys.STATE_THEME_FOLLOW_SYSTEM)
        preferenceThemeSelection.setOnPreferenceChangeListener { preference, newValue ->
            if (preference is ListPreference) {
                val index: Int = preference.entryValues.indexOf(newValue)
                preferenceThemeSelection.summary = "${getString(R.string.pref_theme_selection_summary)} ${preference.entries[index]}"
                return@setOnPreferenceChangeListener true
            } else {
                return@setOnPreferenceChangeListener false
            }
        }

        // set up "Update Stations" preference
        val preferenceUpdateCollection: Preference = Preference(activity as Context)
        preferenceUpdateCollection.title = getString(R.string.pref_update_collection_title)
        preferenceUpdateCollection.setIcon(R.drawable.ic_cloud_download_24dp)
        preferenceUpdateCollection.summary = getString(R.string.pref_update_collection_summary)
        preferenceUpdateCollection.setOnPreferenceClickListener {
            // show dialog
            YesNoDialog(this).show(context = activity as Context, type = Keys.DIALOG_UPDATE_COLLECTION, message = R.string.dialog_yes_no_message_update_collection, yesButton = R.string.dialog_yes_no_positive_button_update_collection)
            return@setOnPreferenceClickListener true
        }

        // set up "Update Station Images" preference
        val preferenceUpdateStationImages: Preference = Preference(activity as Context)
        preferenceUpdateStationImages.title = getString(R.string.pref_update_station_images_title)
        preferenceUpdateStationImages.setIcon(R.drawable.ic_image_24dp)
        preferenceUpdateStationImages.summary = getString(R.string.pref_update_station_images_summary)
        preferenceUpdateStationImages.setOnPreferenceClickListener {
            // show dialog
            YesNoDialog(this).show(context = activity as Context, type = Keys.DIALOG_UPDATE_STATION_IMAGES, message = R.string.dialog_yes_no_message_update_station_images, yesButton = R.string.dialog_yes_no_positive_button_update_covers)
            return@setOnPreferenceClickListener true
        }

        // set up "App Version" preference
        val preferenceAppVersion: Preference = Preference(context)
        preferenceAppVersion.title = getString(R.string.pref_app_version_title)
        preferenceAppVersion.setIcon(R.drawable.ic_info_24dp)
        preferenceAppVersion.summary = "${getString(R.string.pref_app_version_summary)} ${BuildConfig.VERSION_NAME} (${getString(R.string.app_version_name)})"
        preferenceAppVersion.setOnPreferenceClickListener {
            // copy to clipboard
            val clip: ClipData = ClipData.newPlainText("simple text", preferenceAppVersion.summary)
            val cm: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(clip)
            Toast.makeText(activity as Context, R.string.toastmessage_copied_to_clipboard, Toast.LENGTH_LONG).show()
            return@setOnPreferenceClickListener true
        }

        // set up "Report Issue" preference
        val preferenceReportIssue: Preference = Preference(context)
        preferenceReportIssue.title = getString(R.string.pref_report_issue_title)
        preferenceReportIssue.setIcon(R.drawable.ic_bug_report_24dp)
        preferenceReportIssue.summary = getString(R.string.pref_report_issue_summary)
        preferenceReportIssue.setOnPreferenceClickListener {
            // open web browser
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = "https://github.com/jamal2362/URL-Radio/issues".toUri()
            }
            startActivity(intent)
            return@setOnPreferenceClickListener true
        }


        // set preference categories
        val preferenceCategoryGeneral: PreferenceCategory = PreferenceCategory(activity as Context)
        preferenceCategoryGeneral.title = getString(R.string.pref_general_title)
        preferenceCategoryGeneral.contains(preferenceThemeSelection)

        val preferenceCategoryMaintenance: PreferenceCategory = PreferenceCategory(activity as Context)
        preferenceCategoryMaintenance.title = getString(R.string.pref_maintenance_title)
        preferenceCategoryMaintenance.contains(preferenceUpdateCollection)
        preferenceCategoryMaintenance.contains(preferenceUpdateStationImages)

        val preferenceCategoryAbout: PreferenceCategory = PreferenceCategory(context)
        preferenceCategoryAbout.title = getString(R.string.pref_about_title)
        preferenceCategoryAbout.contains(preferenceAppVersion)
        preferenceCategoryAbout.contains(preferenceReportIssue)


        // setup preference screen
        screen.addPreference(preferenceCategoryGeneral)
        screen.addPreference(preferenceThemeSelection)
        screen.addPreference(preferenceCategoryMaintenance)
        screen.addPreference(preferenceUpdateCollection)
        screen.addPreference(preferenceUpdateStationImages)
        screen.addPreference(preferenceCategoryAbout)
        screen.addPreference(preferenceAppVersion)
        screen.addPreference(preferenceReportIssue)
        preferenceScreen = screen
    }



    /* Overrides onYesNoDialog from YesNoDialogListener */
    override fun onYesNoDialog(type: Int, dialogResult: Boolean, payload: Int, payloadString: String) {
        super.onYesNoDialog(type, dialogResult, payload, payloadString)

        when (type) {
            Keys.DIALOG_UPDATE_STATION_IMAGES -> {
                when (dialogResult) {
                    // user tapped: refresh station images
                    true -> {
                        updateStationImages()
                    }
                }
            }

            Keys.DIALOG_UPDATE_COLLECTION -> {
                when (dialogResult) {
                    // user tapped update collection
                    true -> {
                        updateCollection()
                    }
                }
            }

        }

    }


    /* Overrides onActivityResult from Fragment */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // save OPML file to result file location
//            Keys.REQUEST_SAVE_OPML -> {
//            }
            // let activity handle result
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /* Updates collection */
    private fun updateCollection() {
        if (NetworkHelper.isConnectedToNetwork(activity as Context)) {
            Toast.makeText(activity as Context, R.string.toastmessage_updating_collection, Toast.LENGTH_LONG).show()
            // update collection in player screen
            val bundle: Bundle = bundleOf(
                    Keys.ARG_UPDATE_COLLECTION to true
            )
            this.findNavController().navigate(R.id.player_destination, bundle)
        } else {
            ErrorDialog().show(activity as Context, R.string.dialog_error_title_no_network, R.string.dialog_error_message_no_network)
        }
    }


    /* Updates station images */
    private fun updateStationImages() {
        if (NetworkHelper.isConnectedToNetwork(activity as Context)) {
            Toast.makeText(activity as Context, R.string.toastmessage_updating_station_images, Toast.LENGTH_LONG).show()
            // update collection in player screen
            val bundle: Bundle = bundleOf(
                    Keys.ARG_UPDATE_IMAGES to true
            )
            this.findNavController().navigate(R.id.player_destination, bundle)
        } else {
            ErrorDialog().show(activity as Context, R.string.dialog_error_title_no_network, R.string.dialog_error_message_no_network)
        }
    }

}
