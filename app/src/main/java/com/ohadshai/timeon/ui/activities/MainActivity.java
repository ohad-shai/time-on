package com.ohadshai.timeon.ui.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.entities.Project;
import com.ohadshai.timeon.services.TrackTimeService;
import com.ohadshai.timeon.ui.dialogs.AboutDialog;
import com.ohadshai.timeon.ui.fragments.ProjectsActiveFragment;
import com.ohadshai.timeon.ui.fragments.ProjectsArchiveFragment;
import com.ohadshai.timeon.utils.ProjectStateChangeListener;
import com.ohadshai.timeon.utils.Utils;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, ProjectStateChangeListener {

    //region Constants

    /**
     * Holds a constant that identifies the "workers running" notification id.
     */
    private static final int WORKERS_RUNNING_NOTIFICATION_ID = 31173;

    //endregion

    //region Private Members

    /**
     * Holds the sections pager adapter for the activity.
     */
    private SectionsPagerAdapter _pagerAdapter;

    /**
     * Holds the {@link ViewPager} that will host the section contents.
     */
    private ViewPager _viewPager;

    /**
     * Holds the {@link MenuItem} for the "Create Project".
     */
    private MenuItem _createProjectMenuItem;

    //region [TrackTimeService]

    /**
     * Holds the {@link TrackTimeService}.
     */
    private TrackTimeService _trackTimeService;

    /**
     * Holds an indicator indicating whether the {@link TrackTimeService} is bound or not.
     */
    private boolean _timeTrackServiceBound = false;

    /**
     * Holds the service connection to the {@link TrackTimeService}.
     */
    private ServiceConnection _trackTimeServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackTimeService.TimeTrackerBinder binder = (TrackTimeService.TimeTrackerBinder) service;
            _trackTimeService = binder.getService();
            _timeTrackServiceBound = true;

            // Restores the project workers:
            if (_pagerAdapter.getProjectsActiveFragment() != null)
                _pagerAdapter.getProjectsActiveFragment().refreshProjectsList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _timeTrackServiceBound = false;
        }
    };

    //endregion

    //endregion

    //region Public Static Members

    /**
     * Holds an indicator indicating whether the activity is currently in selection mode or not.
     */
    public static boolean IS_IN_SELECTION_MODE = false;

    //endregion

    //region Events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loads the default values from the preferences file:
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Binds the "TrackTimeService":
        bindService(new Intent(this, TrackTimeService.class), _trackTimeServiceConnection, Context.BIND_AUTO_CREATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        _viewPager = (ViewPager) findViewById(R.id.viewPagerContainer);
        _viewPager.addOnPageChangeListener(this);
        _viewPager.setAdapter(_pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(_viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        _createProjectMenuItem = menu.findItem(R.id.actionCreateProject);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSettings:
                this.actionSettings();
                return true;
            case R.id.actionAbout:
                AboutDialog.show(getFragmentManager());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Cancels the "workers running" notification, if shown in the last session:
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(WORKERS_RUNNING_NOTIFICATION_ID);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Checks if there's any project worker running, to notify the user:
        if (_trackTimeService != null && _trackTimeService.isAnyRunning())
            this.showWorkersRunningNotification();
    }

    //region [ View Pager ]

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            if (_pagerAdapter.getProjectsActiveFragment() != null)
                _pagerAdapter.getProjectsActiveFragment().onPageSelected();

            if (_pagerAdapter.getProjectsArchiveFragment() != null)
                _pagerAdapter.getProjectsArchiveFragment().onPageUnselected();
        } else if (position == 1) {
            if (_pagerAdapter.getProjectsArchiveFragment() != null)
                _pagerAdapter.getProjectsArchiveFragment().onPageSelected();

            if (_pagerAdapter.getProjectsActiveFragment() != null)
                _pagerAdapter.getProjectsActiveFragment().onPageUnselected();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    //endregion

    //region [ ProjectStateChangeListener ]

    @Override
    public void onProjectActivated(Project project) {
        // Tells the active fragment a project activated:
        if (_pagerAdapter.getProjectsActiveFragment() != null)
            _pagerAdapter.getProjectsActiveFragment().onProjectActivated(project);
    }

    @Override
    public void onProjectArchived(Project project) {
        // Tells the archive fragment a project archived:
        if (_pagerAdapter.getProjectsArchiveFragment() != null)
            _pagerAdapter.getProjectsArchiveFragment().onProjectArchived(project);
    }

    //endregion

    @Override
    public void onBackPressed() {
        if (_pagerAdapter.getProjectsActiveFragment() != null && _pagerAdapter.getProjectsActiveFragment().onBackPressed())
            return;
        else if (_pagerAdapter.getProjectsArchiveFragment() != null && _pagerAdapter.getProjectsArchiveFragment().onBackPressed())
            return;

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbinds the "TrackTimeService":
        if (_timeTrackServiceBound) {
            unbindService(_trackTimeServiceConnection);
            _timeTrackServiceBound = false;
        }

        _viewPager.removeOnPageChangeListener(this);
    }

    //endregion

    //region Public API

    /**
     * Gets the track time service.
     *
     * @return Returns the track time service.
     */
    public TrackTimeService getTrackTimeService() {
        return _trackTimeService;
    }

    /**
     * Gets the {@link MenuItem} "Create Project".
     *
     * @return Returns the {@link MenuItem} "Create Project".
     */
    public MenuItem getCreateProjectMenuItem() {
        return _createProjectMenuItem;
    }

    //endregion

    //region Private Methods

    /**
     * Method procedure for menu action: "Settings".
     */
    private void actionSettings() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    /**
     * Shows a progress notification that there are project workers running.
     */
    private void showWorkersRunningNotification() {
        // Declares the result intent when clicking the notification:
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // Builds the notification:
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.general_msg_app_is_running))
                .setLights(Color.rgb(255, 125, 0), 500, 500)
                .setSmallIcon(R.mipmap.ic_timeon_status_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_timeon_logo))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentIntent(resultPendingIntent)
                .setProgress(0, 0, true)
                .setAutoCancel(true)
                .setOngoing(true);

        // Sets the notification color if the device's manufacturer is not "samsung" (bug on samsung):
        if (!Build.MANUFACTURER.equals("samsung"))
            builder.setColor(Utils.Colors.PRIMARY_COLOR);

        // Sends to notify this notification:
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(WORKERS_RUNNING_NOTIFICATION_ID, builder.build());
    }

    //endregion

    //region Inner Classes

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        //region Private Methods

        /**
         * Holds the list of fragment registered to the adapter.
         */
        private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        //endregion

        //region C'tor

        /**
         * Initializes a new instance of a {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
         *
         * @param fm The fragment manager of the activity.
         */
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //endregion

        //region Events

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ProjectsActiveFragment();
                case 1:
                    return new ProjectsArchiveFragment();
                default:
                    throw new IndexOutOfBoundsException("SectionsPagerAdapter getItem() encountered a position out of bounds.");
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.general_active);
                case 1:
                    return getString(R.string.general_archive);
                default:
                    throw new IndexOutOfBoundsException("SectionsPagerAdapter getItem() encountered a position out of bounds.");
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        //endregion

        //region Local Methods

        /**
         * Gets the projects active fragment.
         *
         * @return Returns the projects active fragment.
         */
        ProjectsActiveFragment getProjectsActiveFragment() {
            return (ProjectsActiveFragment) registeredFragments.get(0);
        }

        /**
         * Gets the projects archive fragment.
         *
         * @return Returns the projects archive fragment.
         */
        ProjectsArchiveFragment getProjectsArchiveFragment() {
            return (ProjectsArchiveFragment) registeredFragments.get(1);
        }

        //endregion

    }

    //endregion

}
