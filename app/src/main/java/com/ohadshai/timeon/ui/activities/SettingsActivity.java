package com.ohadshai.timeon.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.ui.UIConsts;
import com.ohadshai.timeon.ui.dialogs.AboutDialog;
import com.ohadshai.timeon.utils.AppCompatPreferenceActivity;

public class SettingsActivity extends AppCompatPreferenceActivity {

    //region Public Members

    /**
     * Holds the value indicating the display format is maximized.
     */
    public static final int DISPLAY_FORMAT_MAXIMIZED = 1;

    /**
     * Holds the value indicating the display format is minimized.
     */
    public static final int DISPLAY_FORMAT_MINIMIZED = 2;

    //endregion

    //region Events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        this.setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region Private Methods

    /**
     * Sets the action bar, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // Shows the Up button in the action bar.
        }
    }

    //endregion

    //region Inner Classes

    /**
     * Holds the main PreferenceFragment that display all the preferences.
     */
    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            //region "Rate this app" - Preference

            findPreference(UIConsts.Preferences.RATE_APP_KEY).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Opens "Google Play" in the app profile to let the user rate this app:
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + UIConsts.APP_PACKAGE_NAME)));
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), R.string.general_msg_google_play_not_found, Toast.LENGTH_LONG).show(); // No "Google Play" was found.
                    }
                    return true;
                }
            });

            //endregion

            //region "About" - Preference

            findPreference(UIConsts.Preferences.ABOUT_APP_KEY).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Shows the "Application About" custom dialog:
                    new AboutDialog().show(getFragmentManager(), UIConsts.Fragments.APP_ABOUT_DIALOG_TAG);
                    return true;
                }
            });

            //endregion

        }

    }

    //endregion

}
