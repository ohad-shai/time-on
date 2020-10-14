
package com.ohadshai.timeon.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.adapters.ProjectsActiveAdapter;
import com.ohadshai.timeon.db.DBHandler;
import com.ohadshai.timeon.entities.Project;
import com.ohadshai.timeon.entities.ProjectWorker;
import com.ohadshai.timeon.loaders.ProjectsActiveLoader;
import com.ohadshai.timeon.ui.UIConsts;
import com.ohadshai.timeon.ui.activities.MainActivity;
import com.ohadshai.timeon.ui.activities.ProjectCreateActivity;
import com.ohadshai.timeon.utils.AppBarStateChangeListener;
import com.ohadshai.timeon.utils.ProjectStateChangeListener;
import com.ohadshai.timeon.utils.SelectionHelper;
import com.ohadshai.timeon.utils.TimeSpan;
import com.ohadshai.timeon.utils.Utils;
import com.ohadshai.timeon.utils.ViewPagerSelectListener;

import java.util.ArrayList;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;
import static com.ohadshai.timeon.R.id.actionEdit;
import static com.ohadshai.timeon.R.id.appBarSelection;

/**
 * Represents a {@link Fragment} for the active projects.
 * Created by Ohad on 11/23/2016.
 */
public class ProjectsActiveFragment extends Fragment implements SelectionHelper.SelectionController<Project>, ViewPagerSelectListener, ProjectStateChangeListener, SharedPreferences.OnSharedPreferenceChangeListener, MenuItem.OnMenuItemClickListener {

    //region Constants

    /**
     * Holds a constant for the active places loader id.
     */
    private static final int PROJECTS_ACTIVE_LOADER_ID = 221773;

    //endregion

    //region Private Members

    /**
     * Holds the view of this fragment.
     */
    private View _view;

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    /**
     * Holds the list of projects.
     */
    private ArrayList<Project> _projects = new ArrayList<>();

    /**
     * Holds the adapter of the projects for the RecyclerView.
     */
    private ProjectsActiveAdapter _adapter;

    /**
     * Holds the RecyclerView control of the projects list.
     */
    private RecyclerView _rvProjects;

    /**
     * Holds the {@link ProgressBar} control.
     */
    private ProgressBar _progressBar;

    /**
     * Holds the button control for the not found projects.
     */
    private Button _btnNotFoundProjects;

    /**
     * Holds the SharedPreferences control.
     */
    private SharedPreferences _preferences;

    //region [ Projects Selection ]

    /**
     * Holds the {@link AppBarLayout} for the main header.
     */
    private AppBarLayout _appBarMain;

    /**
     * Holds the {@link AppBarLayout} for the selection header.
     */
    private AppBarLayout _appBarSelection;

    /**
     * Holds the {@link Toolbar} control for the selection header.
     */
    private Toolbar _toolbarSelection;

    /**
     * Holds the {@link TextView} control for the selection title.
     */
    private TextView _txtSelectionTitle;

    /**
     * Holds the selection helper for the fragment.
     */
    private SelectionHelper<Project> _selectionHelper = new SelectionHelper<>(_projects, new SelectionHelper.SelectionCallback<Project>() {

        @Override
        public void onSelectionEnter() {
            MainActivity.IS_IN_SELECTION_MODE = true;
        }

        @Override
        public void onSelectionExit() {
            MainActivity.IS_IN_SELECTION_MODE = false;
        }

        @Override
        public void onShowHeader() {
            _appBarMain.setExpanded(false, false);
            _appBarMain.setVisibility(View.GONE);

            _appBarSelection.setVisibility(View.VISIBLE);
            //region Toolbar Menu Items...

            _toolbarSelection.getMenu().clear();
            _toolbarSelection.inflateMenu(R.menu.menu_selection_active);

            MenuItem editMenuItem = _toolbarSelection.getMenu().findItem(actionEdit);
            editMenuItem.getIcon().mutate().setColorFilter(Utils.Colors.PRIMARY_COLOR, PorterDuff.Mode.SRC_ATOP);
            editMenuItem.setOnMenuItemClickListener(ProjectsActiveFragment.this);

            MenuItem archiveMenuItem = _toolbarSelection.getMenu().findItem(R.id.actionMoveToArchive);
            archiveMenuItem.getIcon().mutate().setColorFilter(Utils.Colors.PRIMARY_COLOR, PorterDuff.Mode.SRC_ATOP);
            archiveMenuItem.setOnMenuItemClickListener(ProjectsActiveFragment.this);

            MenuItem deleteMenuItem = _toolbarSelection.getMenu().findItem(R.id.actionDelete);
            deleteMenuItem.getIcon().mutate().setColorFilter(Utils.Colors.PRIMARY_COLOR, PorterDuff.Mode.SRC_ATOP);
            deleteMenuItem.setOnMenuItemClickListener(ProjectsActiveFragment.this);

            MenuItem selectAllMenuItem = _toolbarSelection.getMenu().findItem(R.id.actionSelectAll);
            selectAllMenuItem.setOnMenuItemClickListener(ProjectsActiveFragment.this);

            final ImageButton imgbtnClose = (ImageButton) _toolbarSelection.findViewById(R.id.imgbtnClose);
            imgbtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _selectionHelper.exitSelectionMode();
                }
            });
            imgbtnClose.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Utils.UI.showInformationToast(imgbtnClose, R.string.general_hint_exit_selection);
                    return true;
                }
            });

            //endregion
            _toolbarSelection.setVisibility(View.VISIBLE);

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            }

            //endregion
        }

        @Override
        public void onAnimateShowHeader() {
            _appBarMain.setExpanded(false, true);
            _appBarMain.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        _appBarMain.setVisibility(View.GONE);
                        appBarLayout.removeOnOffsetChangedListener(this);
                    }
                }
            });

            _appBarSelection.setVisibility(View.VISIBLE);
            //region Toolbar Menu Items...

            _toolbarSelection.getMenu().clear();
            _toolbarSelection.inflateMenu(R.menu.menu_selection_active);

            MenuItem editMenuItem = _toolbarSelection.getMenu().findItem(actionEdit);
            editMenuItem.getIcon().mutate().setColorFilter(Utils.Colors.PRIMARY_COLOR, PorterDuff.Mode.SRC_ATOP);
            editMenuItem.setOnMenuItemClickListener(ProjectsActiveFragment.this);

            MenuItem archiveMenuItem = _toolbarSelection.getMenu().findItem(R.id.actionMoveToArchive);
            archiveMenuItem.getIcon().mutate().setColorFilter(Utils.Colors.PRIMARY_COLOR, PorterDuff.Mode.SRC_ATOP);
            archiveMenuItem.setOnMenuItemClickListener(ProjectsActiveFragment.this);

            MenuItem deleteMenuItem = _toolbarSelection.getMenu().findItem(R.id.actionDelete);
            deleteMenuItem.getIcon().mutate().setColorFilter(Utils.Colors.PRIMARY_COLOR, PorterDuff.Mode.SRC_ATOP);
            deleteMenuItem.setOnMenuItemClickListener(ProjectsActiveFragment.this);

            MenuItem selectAllMenuItem = _toolbarSelection.getMenu().findItem(R.id.actionSelectAll);
            selectAllMenuItem.setOnMenuItemClickListener(ProjectsActiveFragment.this);

            final ImageButton imgbtnClose = (ImageButton) _toolbarSelection.findViewById(R.id.imgbtnClose);
            imgbtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _selectionHelper.exitSelectionMode();
                }
            });
            imgbtnClose.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Utils.UI.showInformationToast(imgbtnClose, R.string.general_hint_exit_selection);
                    return true;
                }
            });

            //endregion
            _toolbarSelection.setVisibility(View.VISIBLE);

            //region Selection layout animation...

            // Defines the point for the clipping circle:
            int revealX = _toolbarSelection.getWidth() / 2;
            int revealY = _toolbarSelection.getHeight() + 500;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(revealX, revealY);

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_toolbarSelection, revealX, revealY, 500, revealRadius);
                anim.setInterpolator(new AccelerateInterpolator());
            } else {
                // Fade In for API below 21:
                _toolbarSelection.setAlpha(0);
                anim = ObjectAnimator.ofFloat(_toolbarSelection, "alpha", 1);
            }
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    _appBarMain.setVisibility(View.GONE);
                    _appBarMain.requestLayout();
                }
            });
            anim.start();

            //endregion

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorPrimary));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (_selectionHelper != null && _selectionHelper.isInSelectionMode())
                                window.setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    }
                });
                colorAnimation.setDuration(500);
                colorAnimation.start();
            }

            //endregion
        }

        @Override
        public void onSelectionChanged(ArrayList<Project> selection) {
            if (selection.size() > 0)
                _txtSelectionTitle.setText(String.valueOf(selection.size()));

            MenuItem editMenuItem = _toolbarSelection.getMenu().findItem(actionEdit);
            if (editMenuItem != null) {
                if (selection.size() == 1)
                    _toolbarSelection.getMenu().findItem(actionEdit).setVisible(true);
                else if (selection.size() > 1)
                    _toolbarSelection.getMenu().findItem(actionEdit).setVisible(false);
            }
        }

        @Override
        public void onUpdateItemsLayout() {
            if (_adapter != null)
                _adapter.notifyDataSetChanged();
        }

        @Override
        public void onUpdateItemLayout(int position) {
            if (_adapter != null)
                _adapter.notifyItemChanged(position);
        }

        @Override
        public void onHideHeader() {
            _appBarMain.setVisibility(View.VISIBLE);
            _appBarMain.setExpanded(true, true);

            _appBarSelection.setVisibility(View.INVISIBLE);
            _toolbarSelection.setVisibility(View.INVISIBLE);
            _toolbarSelection.getMenu().clear();

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }

            //endregion
        }

        @Override
        public void onAnimateHideHeader() {
            _appBarMain.setExpanded(false, false);
            _appBarMain.setVisibility(View.VISIBLE);
            _appBarMain.setExpanded(true, true);

            //region Selection layout animation...

            // Defines the point for the clipping circle:
            int revealX = _appBarSelection.getWidth() / 2;
            int revealY = -500;

            // Defines the final radius for the clipping circle:
            float revealRadius = (float) Math.hypot(revealX, revealY);

            Animator anim = null;
            // Checks and targets the animation capabilities:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Circular Reveal for API above 21:
                anim = ViewAnimationUtils.createCircularReveal(_appBarSelection, revealX, revealY, revealRadius, 500);
                anim.setInterpolator(new DecelerateInterpolator());
            } else {
                // Fade Out for API below 21:
                _appBarSelection.setAlpha(1);
                anim = ObjectAnimator.ofFloat(appBarSelection, "alpha", 0);
            }

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    _appBarSelection.setVisibility(View.INVISIBLE);
                    _toolbarSelection.setVisibility(View.INVISIBLE);
                    _toolbarSelection.getMenu().clear();
                }
            });
            anim.start();

            //endregion

            //region StatusBar animation...

            // Works only for API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                ValueAnimator colorAnimation = ValueAnimator.ofArgb(getActivity().getResources().getColor(R.color.colorPrimary), getActivity().getResources().getColor(R.color.colorPrimaryDark));
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (_selectionHelper != null && !_selectionHelper.isInSelectionMode())
                                window.setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    }
                });
                colorAnimation.setDuration(500);
                colorAnimation.setStartDelay(200);
                colorAnimation.setInterpolator(new DecelerateInterpolator());
                colorAnimation.start();
            }

            //endregion
        }

    });

    //endregion

    //endregion

    /**
     * Initializes a new instance of a {@link Fragment} for the projects.
     */
    public ProjectsActiveFragment() {
        // Required empty public constructor.
    }

    //region Fragment Events

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_projects_active, container, false);

        _preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        _preferences.registerOnSharedPreferenceChangeListener(this);

        _repository = DBHandler.getInstance(getActivity());

        _progressBar = (ProgressBar) _view.findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.VISIBLE);

        _rvProjects = (RecyclerView) _view.findViewById(R.id.rvProjects);
        _rvProjects.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapter = new ProjectsActiveAdapter(_projects, this, getDisplayFormat());
        _rvProjects.setAdapter(_adapter);

        _btnNotFoundProjects = (Button) _view.findViewById(R.id.btnNotFoundCreateProject);
        _btnNotFoundProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionCreateProject();
            }
        });

        //region Selection Views

        _appBarMain = (AppBarLayout) getActivity().findViewById(R.id.appBarMain);

        _appBarSelection = (AppBarLayout) getActivity().findViewById(appBarSelection);

        _toolbarSelection = (Toolbar) getActivity().findViewById(R.id.toolbarSelection);

        _txtSelectionTitle = (TextView) _toolbarSelection.findViewById(R.id.txtSelectionTitle);

        //endregion

        if (savedInstanceState == null)
            this.loadProjectsActiveList();

        return _view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Shows the action "Create Project":
        menu.findItem(R.id.actionCreateProject).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionCreateProject:
                this.actionCreateProject();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UIConsts.RequestCodes.CREATE_PROJECT_REQUEST_CODE && resultCode == RESULT_OK) {
            this.notifyNewProjectInserted();
        } else if (requestCode == UIConsts.RequestCodes.EDIT_PROJECT_REQUEST_CODE && resultCode == RESULT_OK) {
            int position = data.getIntExtra(UIConsts.Bundles.PROJECT_POSITION_KEY, -1); // Gets the edited project position in the list.
            if (position > -1)
                this.notifyProjectUpdated(position);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Checks if the display format changed:
        if (key.equals(UIConsts.Preferences.DISPLAY_FORMAT_KEY)) {
            _adapter = new ProjectsActiveAdapter(_projects, this, getDisplayFormat());
            _rvProjects.setAdapter(_adapter);
            Toast.makeText(getActivity(), getString(R.string.settings_display_format_changed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public SelectionHelper<Project> getSelectionHelper() {
        return _selectionHelper;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Saves the projects list:
        outState.putParcelableArrayList(UIConsts.Bundles.PROJECTS_LIST_KEY, _projects);

        // Lets the selection helper handle the save instance state:
        _selectionHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null)
            return;

        // Restores the projects list:
        ArrayList<Project> projects = savedInstanceState.getParcelableArrayList(UIConsts.Bundles.PROJECTS_LIST_KEY);
        if (projects != null) {
            _projects.clear();
            for (Project project : projects)
                _projects.add(project);
        }

        this.displayListState();

        // Lets the selection helper handle the restore instance state:
        if (_projects.size() > 0)
            _selectionHelper.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Validates that in selection mode:
        if (_selectionHelper == null || !_selectionHelper.isInSelectionMode())
            return false;

        if (item.getItemId() == actionEdit) {
            this.actionSelectionEdit(_selectionHelper.getSelection().get(0));
            return true;
        } else if (item.getItemId() == R.id.actionDelete) {
            this.actionSelectionDelete(_selectionHelper.getSelection());
            return true;
        } else if (item.getItemId() == R.id.actionMoveToArchive) {
            this.actionSelectionMoveToArchive(_selectionHelper.getSelection());
            return true;
        } else if (item.getItemId() == R.id.actionSelectAll) {
            _selectionHelper.actionSelectAll();
            return true;
        } else {
            return false;
        }
    }

    //region [ ViewPagerSelectListener ]

    @Override
    public void onPageSelected() {
        // Shows the action "Create Project":
        ((MainActivity) getActivity()).getCreateProjectMenuItem().setVisible(true);
    }

    @Override
    public void onPageUnselected() {
        // Hides the action "Create Project":
        ((MainActivity) getActivity()).getCreateProjectMenuItem().setVisible(false);

        if (_selectionHelper.isInSelectionMode())
            _selectionHelper.exitSelectionMode();
    }

    //endregion

    //region [ ProjectStateChangeListener ]

    @Override
    public void onProjectActivated(Project project) {
        // Handles a project returned to active:
        Project.updateListPositionsForInsert(_projects, 0);
        project.setPosition(0);
        _projects.add(0, project);
        _adapter.notifyItemInserted(0);
        this.displayListState();
        this.scrollViewToTop();
    }

    @Override
    public void onProjectArchived(Project project) {
        // Tells that a project is archived:
        ((ProjectStateChangeListener) getActivity()).onProjectArchived(project);
    }

    //endregion

    public boolean onBackPressed() {
        if (_selectionHelper.isInSelectionMode()) {
            _selectionHelper.exitSelectionMode();
            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        _preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    //endregion

    //region Public API

    @Override
    public Context getContext() {
        if (_view != null)
            return _view.getContext();
        else if (getActivity() != null)
            return getActivity().getApplicationContext();
        else
            return super.getContext();
    }

    /**
     * Displays the state of the adapter (list / empty message), according to the items in the list.
     */
    public void displayListState() {
        _progressBar.setVisibility(View.GONE);

        if (this._projects.size() < 1) {
            _rvProjects.setVisibility(View.GONE);
            _btnNotFoundProjects.setVisibility(View.VISIBLE);
        } else {
            _btnNotFoundProjects.setVisibility(View.GONE);
            _rvProjects.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Scrolls the view to top .
     */
    public void scrollViewToTop() {
        _rvProjects.scrollToPosition(0);
    }

    /**
     * Loads the active projects list from the DB.
     */
    public void loadProjectsActiveList() {
        getLoaderManager().initLoader(PROJECTS_ACTIVE_LOADER_ID, null, new LoaderManager.LoaderCallbacks<ArrayList<Project>>() {
            @Override
            public Loader<ArrayList<Project>> onCreateLoader(int id, Bundle args) {
                return new ProjectsActiveLoader(getContext());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<Project>> loader, ArrayList<Project> data) {
                if (data == null)
                    return;

                _projects.clear();
                for (Project project : data)
                    _projects.add(project);
                _adapter.notifyDataSetChanged();
                displayListState();
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<Project>> loader) {
            }
        }).forceLoad();
    }

    /**
     * Refreshes the adapter's projects list.
     */
    public void refreshProjectsList() {
        if (_adapter != null)
            _adapter.notifyDataSetChanged();
    }

    //endregion

    //region Private Methods

    /**
     * Initializes a list of projects safely (keeps the reference).
     *
     * @param projects The projects list to initialize.
     */
    private void initProjects(ArrayList<Project> projects) {
        this._projects.clear();
        for (Project project : projects)
            this._projects.add(project);
    }

    /**
     * Method procedure for action: "Create Project" (starts a new activity to create a project).
     */
    private void actionCreateProject() {
        startActivityForResult(new Intent(getActivity(), ProjectCreateActivity.class), UIConsts.RequestCodes.CREATE_PROJECT_REQUEST_CODE);
    }

    /**
     * Notifies a new project inserted to the list.
     */
    private void notifyNewProjectInserted() {
        // Displays the list with the new project item created:
        this.initProjects(_repository.projects.getAllActive());
        _adapter.notifyItemInserted(0);
        _rvProjects.scrollToPosition(0);
        this.displayListState();
    }

    /**
     * Notifies a project updated in the list.
     *
     * @param position The position of the updated project item in the list.
     */
    private void notifyProjectUpdated(int position) {
        this.initProjects(_repository.projects.getAllActive());
        _adapter.notifyItemChanged(position);
    }

    /**
     * Gets the projects display format, from the _preferences.
     *
     * @return Returns the display format value.
     */
    private int getDisplayFormat() {
        return Integer.valueOf(_preferences.getString(UIConsts.Preferences.DISPLAY_FORMAT_KEY, String.valueOf(UIConsts.Preferences.DISPLAY_FORMAT_MAXIMIZED_VALUE)));
    }

    /**
     * Method procedure for the selection action: "Edit".
     *
     * @param project The project selected to edit.
     */
    private void actionSelectionEdit(Project project) {
        if (project == null)
            throw new NullPointerException("project");

        Intent editIntent = new Intent(getContext(), ProjectCreateActivity.class);
        editIntent.putExtra(UIConsts.Bundles.MODE.KEY_NAME, UIConsts.Bundles.MODE.UPDATE); // Tells the "Create Activity" to go in edit mode.
        editIntent.putExtra(UIConsts.Bundles.PROJECT_KEY, project);
        startActivityForResult(editIntent, UIConsts.RequestCodes.EDIT_PROJECT_REQUEST_CODE);

        _selectionHelper.exitSelectionMode();
    }

    /**
     * Method procedure for the selection action: "Delete".
     *
     * @param selection The selected projects list to delete.
     */
    private void actionSelectionDelete(ArrayList<Project> selection) {
        if (selection == null)
            throw new NullPointerException("selection");

        final ArrayList<Project> cache = new ArrayList<>();

        for (int i = 0; i < selection.size(); i++) {
            Project project = selection.get(i);
            cache.add(project);

            // Checks to close any open workers:
            for (ProjectWorker worker : project.getWorkers()) {
                if (worker.getEnd() == null) {
                    // Unregisters the worker listener for update UI events.
                    ((MainActivity) getActivity()).getTrackTimeService().unregisterWorker(worker);

                    worker.setEnd(Calendar.getInstance());
                    project.getTotalTrack().add(new TimeSpan(worker.getEnd().getTimeInMillis() - worker.getStart().getTimeInMillis())); // Adds the worker track to the total track.

                    _repository.projects.updateTotalTrack(project.getId(), project.getTotalTrack()); // Updates the total track in the DB.
                    _repository.workers.end(worker); // Ends the worker in the database.
                }
            }

            int position = _projects.indexOf(project);
            _repository.projects.delete(project);
            _projects.remove(position);
            Project.updateListPositionsForDelete(_projects, position);
            _adapter.notifyItemRemoved(position);
        }

        // Shows the user the number of deleted projects, and an option to restore:
        Snackbar.make(getActivity().findViewById(R.id.coordinator), selection.size() + " " + getActivity().getString(R.string.general_msg_deleted), 10000)
                .setAction(getActivity().getString(R.string.general_btn_undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        Collections.sort(cache, new Project.PositionComparator());

                        for (int i = cache.size() - 1; i >= 0; i--) {
                            Project project = cache.get(i);
                            project.setId(_repository.projects.restore(project));
                            Project.updateListPositionsForInsert(_projects, project.getPosition());
                            _projects.add(project.getPosition(), project);
                            _adapter.notifyItemInserted(project.getPosition());

                            if (project.getPosition() == 0)
                                scrollViewToTop();
                        }
                        displayListState();
                    }
                }).setActionTextColor(Utils.Colors.PRIMARY_COLOR).show();

        this.displayListState();
        _selectionHelper.exitSelectionModeFromAction();
    }

    /**
     * Method procedure for the selection action: "Move to Archive".
     *
     * @param selection The selected projects list to move to archive.
     */
    private void actionSelectionMoveToArchive(final ArrayList<Project> selection) {
        if (selection == null)
            throw new NullPointerException("selection");

        // Checks if currently running:
        for (Project project : selection) {
            for (final ProjectWorker worker : project.getWorkers()) {
                if (worker.getEnd() == null) {
                    // Asks the user to stop and move the project, because it's currently tracking:
                    Snackbar.make(getActivity().findViewById(R.id.coordinator), R.string.project_move_to_archive_currently_some_tracking, 10000)
                            .setAction(getActivity().getString(R.string.general_btn_yes), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    for (Project project : selection) {
                                        // Ends each running worker in the selection:
                                        for (ProjectWorker w : project.getWorkers()) {
                                            if (worker.getEnd() == null) {
                                                // Unregisters the worker listener for update UI events.
                                                ((MainActivity) getActivity()).getTrackTimeService().unregisterWorker(worker);

                                                worker.setEnd(Calendar.getInstance());
                                                project.getTotalTrack().add(new TimeSpan(worker.getEnd().getTimeInMillis() - worker.getStart().getTimeInMillis())); // Adds the worker track to the total track.

                                                _repository.projects.updateTotalTrack(project.getId(), project.getTotalTrack()); // Updates the total track in the DB.
                                                _repository.workers.end(worker); // Ends the worker in the database.
                                            }
                                        }

                                        int position = _projects.indexOf(project);
                                        project.setArchiveDate(Calendar.getInstance());
                                        _repository.projects.moveToArchive(project);
                                        _projects.remove(project);
                                        Project.updateListPositionsForDelete(_projects, position);
                                        _adapter.notifyItemRemoved(position);
                                        onProjectArchived(project);
                                    }

                                    // Shows the user the number of moved projects:
                                    Snackbar.make(getActivity().findViewById(R.id.coordinator), selection.size() + " " + getString(R.string.general_msg_archived), Snackbar.LENGTH_SHORT).show();

                                    displayListState();
                                    _selectionHelper.exitSelectionModeFromAction();
                                }
                            }).setActionTextColor(Utils.Colors.PRIMARY_COLOR).show();
                    return;
                }
            }
        }

        for (Project project : selection) {
            int position = _projects.indexOf(project);
            project.setArchiveDate(Calendar.getInstance());
            _repository.projects.moveToArchive(project);
            _projects.remove(project);
            Project.updateListPositionsForDelete(_projects, position);
            _adapter.notifyItemRemoved(position);
            this.onProjectArchived(project);
        }

        // Shows the user the number of moved projects:
        Snackbar.make(getActivity().findViewById(R.id.coordinator), selection.size() + " " + getString(R.string.general_msg_archived), Snackbar.LENGTH_SHORT).show();

        this.displayListState();
        _selectionHelper.exitSelectionModeFromAction();
    }

    //endregion

}
