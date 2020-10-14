package com.ohadshai.timeon.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.db.DBHandler;
import com.ohadshai.timeon.entities.Project;
import com.ohadshai.timeon.entities.ProjectWorker;
import com.ohadshai.timeon.services.TrackTimeService;
import com.ohadshai.timeon.ui.UIConsts;
import com.ohadshai.timeon.ui.activities.MainActivity;
import com.ohadshai.timeon.ui.activities.ProjectCreateActivity;
import com.ohadshai.timeon.ui.activities.ProjectDisplayActivity;
import com.ohadshai.timeon.ui.activities.SettingsActivity;
import com.ohadshai.timeon.ui.dialogs.ProjectInformationDialog;
import com.ohadshai.timeon.ui.dialogs.TimeSpanPickerDialog;
import com.ohadshai.timeon.ui.fragments.ProjectsActiveFragment;
import com.ohadshai.timeon.utils.SelectionHelper;
import com.ohadshai.timeon.utils.TimeSpan;
import com.ohadshai.timeon.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;

import static com.ohadshai.timeon.utils.SelectionHelper.SELECTED;

/**
 * Represents a projects active adapter for RecyclerView.
 * Created by Ohad on 2/24/2017.
 */
public class ProjectsActiveAdapter extends RecyclerView.Adapter<ProjectsActiveAdapter.ViewHolder> {

    //region Private Members

    /**
     * Holds the list of projects to adapt.
     */
    private ArrayList<Project> _projects;

    /**
     * Holds the fragment owner of the adapter.
     */
    private ProjectsActiveFragment _fragment;

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    /**
     * Holds the display format of the project items.
     */
    private int _displayFormat;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of a projects adapter for RecyclerView.
     *
     * @param projects      The list of projects to adapt.
     * @param fragment      The fragment owner of the adapter.
     * @param displayFormat The display format of the projects.
     */
    public ProjectsActiveAdapter(ArrayList<Project> projects, ProjectsActiveFragment fragment, int displayFormat) {
        this._projects = projects;
        this._fragment = fragment;
        this._repository = DBHandler.getInstance(getContext());
        this._displayFormat = displayFormat;
    }

    //endregion

    //region Adapter Events

    @Override
    public ProjectsActiveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflates the custom layout:
        View projectView;
        if (_displayFormat == SettingsActivity.DISPLAY_FORMAT_MINIMIZED)
            projectView = inflater.inflate(R.layout.item_project_small, parent, false);
        else
            projectView = inflater.inflate(R.layout.item_project_big, parent, false);

        return new ViewHolder(projectView); // Returns a new holder instance.
    }

    @Override
    public void onBindViewHolder(ProjectsActiveAdapter.ViewHolder viewHolder, int position) {
        viewHolder.bindViewHolder(_projects.get(position)); // Gets the project by the position, and binds it to the view holder.
    }

    @Override
    public int getItemCount() {
        return _projects.size();
    }

    //endregion

    //region Public API

    /**
     * Gets the context of the projects adapter.
     *
     * @return Returns the context of the projects adapter.
     */
    public Context getContext() {
        return this._fragment.getContext();
    }

    /**
     * Gets the activity owner.
     *
     * @return Returns the activity owner.
     */
    public Activity getActivity() {
        return _fragment.getActivity();
    }

    /**
     * Initializes a list of projects safely to the adapter (keeps the reference).
     *
     * @param projects The projects list to initialize the adapter.
     */
    public void initProjects(ArrayList<Project> projects) {
        if (this._projects == null)
            this._projects = new ArrayList<>();
        else
            this._projects.clear();

        for (Project project : projects)
            this._projects.add(project);
    }

    //endregion

    //region Private Methods

    /**
     * Gets the time track service from the activity owner.
     *
     * @return Returns the time track service from the activity owner.
     */
    private TrackTimeService getTimeTrackService() {
        return ((MainActivity) getActivity()).getTrackTimeService();
    }

    //endregion

    //region Inner Classes

    class ViewHolder extends RecyclerView.ViewHolder implements SelectionHelper.SelectionItemController<Project>, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        //region Private Members

        /**
         * Holds the project object of the current position item.
         */
        private Project project;

        /**
         * Holds the current worker of the project.
         */
        private ProjectWorker currentWorker;

        /**
         * Holds an indicator if a time tracker is running or not.
         */
        private boolean isRunning = false;

        private CardView cardProject;
        private TextView lblProjectName;
        private View viewProjectColor;
        private TextView lblProjectRunning;
        private TextView txtProjectTimeSpan;
        private ImageButton imgBtnProjectRun;
        private Button btnProjectActions;

        //region Maximized Project

        private Button btnProjectInformation;
        private Button btnProjectEditTime;
        private Button btnProjectFullReport;

        //endregion

        private RelativeLayout _rlSelection;
        private CheckBox _chkSelection;

        //endregion

        ViewHolder(final View itemView) {
            super(itemView);

            cardProject = (CardView) itemView.findViewById(R.id.cardProject);
            cardProject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionProjectDisplay();
                }
            });
            cardProject.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Checks if not already in selection mode:
                    if (!getSelectionHelper().isInSelectionMode()) {
                        getSelectionHelper().enterSelectionMode(); // Enters the selection mode.
                        itemSelection(true); // Selects this item that started the selection mode.
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            lblProjectName = (TextView) itemView.findViewById(R.id.lblProjectName);

            viewProjectColor = itemView.findViewById(R.id.viewProjectColor);

            lblProjectRunning = (TextView) itemView.findViewById(R.id.lblProjectRunning);

            txtProjectTimeSpan = (TextView) itemView.findViewById(R.id.txtProjectTimeSpan);

            imgBtnProjectRun = (ImageButton) itemView.findViewById(R.id.imgBtnProjectRun);
            imgBtnProjectRun.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isRunning)
                        endWorker();
                    else
                        startWorker();
                }
            });
            imgBtnProjectRun.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Utils.UI.showInformationToast(imgBtnProjectRun, R.string.project_play_btn_info);
                    return true;
                }
            });

            btnProjectActions = (Button) itemView.findViewById(R.id.btnProjectActions);
            btnProjectActions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performLongClick();
                }
            });
            btnProjectActions.setOnCreateContextMenuListener(this);

            // Checks if the display format is maximized:
            if (_displayFormat == SettingsActivity.DISPLAY_FORMAT_MAXIMIZED) {

                btnProjectInformation = (Button) itemView.findViewById(R.id.btnProjectInformation);
                btnProjectInformation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actionProjectInformation(getAdapterPosition());
                    }
                });

                btnProjectEditTime = (Button) itemView.findViewById(R.id.btnProjectEditTime);
                btnProjectEditTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actionEditTime(getAdapterPosition());
                    }
                });

                btnProjectFullReport = (Button) itemView.findViewById(R.id.btnProjectFullReport);
                btnProjectFullReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionProjectDisplay();
                    }
                });

            }

            _rlSelection = (RelativeLayout) itemView.findViewById(R.id.rlSelection);
            _rlSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _chkSelection.performClick();
                }
            });

            _chkSelection = (CheckBox) itemView.findViewById(R.id.chkSelection);
            _chkSelection.setChecked(false);
            _chkSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (_chkSelection.isChecked())
                        itemSelection(true);
                    else
                        itemSelection(false);
                }
            });

        }

        //region WorkerViewHolder Events

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (_displayFormat == UIConsts.Preferences.DISPLAY_FORMAT_MAXIMIZED_VALUE)
                getActivity().getMenuInflater().inflate(R.menu.context_menu_project_max_actions, menu);
            else
                getActivity().getMenuInflater().inflate(R.menu.context_menu_project_min_actions, menu);

            // Registers to all the click listeners of the menu items in the context menu:
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setOnMenuItemClickListener(this);

            // Sets the context menu header custom view:
            View header = (View) getActivity().getLayoutInflater().inflate(R.layout.context_menu_header_project_actions, null);
            ((TextView) header.findViewById(R.id.txtProjectName)).setText(project.getName());
            menu.setHeaderView(header);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.actionMoveToArchive:
                    this.actionMoveToArchive(getAdapterPosition());
                    return true;
                case R.id.actionEdit:
                    this.actionEdit();
                    return true;
                case R.id.actionDelete:
                    this.deleteProject(getAdapterPosition());
                    return true;
                case R.id.actionEditTime:
                    this.actionEditTime(getAdapterPosition());
                    return true;
                default:
                    return false;
            }
        }

        //region [ SelectionHelper.SelectionItemController<Project> ]

        @Override
        public SelectionHelper<Project> getSelectionHelper() {
            return ((SelectionHelper.SelectionController<Project>) _fragment).getSelectionHelper();
        }

        @Override
        public void drawSelection(int state) {
            // Checks the display format:
            if (_displayFormat == UIConsts.Preferences.DISPLAY_FORMAT_MAXIMIZED_VALUE) {
                if (state == SELECTED) {
                    _chkSelection.setChecked(true);
                    _rlSelection.setBackgroundResource(R.drawable.selected_radial_background_style);
                    _rlSelection.setVisibility(View.VISIBLE);
                } else if (state == SelectionHelper.UNSELECTED) {
                    _chkSelection.setChecked(false);
                    _rlSelection.setBackgroundResource(R.drawable.select_radial_background_style);
                    _rlSelection.setVisibility(View.VISIBLE);
                } else if (state == SelectionHelper.GONE) {
                    _rlSelection.setBackgroundResource(R.drawable.select_radial_background_style);
                    _rlSelection.setVisibility(View.GONE);
                } else {
                    throw new IllegalArgumentException("state is invalid.");
                }
            } else {
                if (state == SELECTED) {
                    _chkSelection.setChecked(true);
                    _rlSelection.setBackgroundResource(R.drawable.selected_background_style);
                    _rlSelection.setVisibility(View.VISIBLE);
                } else if (state == SelectionHelper.UNSELECTED) {
                    _chkSelection.setChecked(false);
                    _rlSelection.setBackgroundResource(R.drawable.select_background_style);
                    _rlSelection.setVisibility(View.VISIBLE);
                } else if (state == SelectionHelper.GONE) {
                    _rlSelection.setBackgroundResource(R.drawable.select_background_style);
                    _rlSelection.setVisibility(View.GONE);
                } else {
                    throw new IllegalArgumentException("state is invalid.");
                }
            }
        }

        @Override
        public void bindSelection() {
            // Checks if currently in the selection mode:
            if (getSelectionHelper() != null && getSelectionHelper().isInSelectionMode()) {
                // Checks if the item is selected in the selection list:
                if (getSelectionHelper().isItemSelected(project))
                    this.drawSelection(SELECTED);
                else
                    this.drawSelection(SelectionHelper.UNSELECTED);
            } else {
                this.drawSelection(SelectionHelper.GONE);
            }
        }

        @Override
        public void itemSelection(boolean isSelected) {
            if (isSelected) {
                drawSelection(SelectionHelper.SELECTED);
                getSelectionHelper().itemSelection(getLayoutPosition(), true);
            } else {
                drawSelection(SelectionHelper.UNSELECTED);
                getSelectionHelper().itemSelection(getLayoutPosition(), false);
            }
        }

        //endregion

        //endregion

        //region Local Methods

        /**
         * Binds a Project object to the view holder.
         *
         * @param project The project object to bind.
         */
        void bindViewHolder(Project project) {
            this.project = project; // Sets the project object to the view holder local.

            // Sets item views, based on the views and the data model:

            lblProjectName.setText(project.getName()); // Sets the project name.
            txtProjectTimeSpan.setText(project.getTotalTrack().toString()); // Sets the project total track.

            imgBtnProjectRun.setImageResource(R.mipmap.ic_big_play_black);
            lblProjectRunning.setVisibility(View.INVISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewProjectColor.setBackgroundColor(project.getColor()); // Sets the project color.
            } else {
                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.project_color_old_api_corner_radius);
                drawable.setColorFilter(project.getColor(), PorterDuff.Mode.SRC_ATOP);
                // Sets the project color:
                if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
                    viewProjectColor.setBackgroundDrawable(drawable);
                else
                    viewProjectColor.setBackground(drawable);
            }

            // Restores the worker state:
            for (ProjectWorker worker : project.getWorkers()) {
                // Checks if there's a worker not closed:
                if (worker.getEnd() == null)
                    restoreWorker(worker);
            }

            this.bindSelection();
        }

        /**
         * Restores a project worker.
         */
        private void restoreWorker(ProjectWorker worker) {
            currentWorker = worker;

            // Change UI tracking indicators:
            imgBtnProjectRun.setImageResource(R.mipmap.ic_big_pause_black);
            lblProjectRunning.setVisibility(View.VISIBLE);

            // Shows the time tracked so far:
            TimeSpan track = new TimeSpan(Calendar.getInstance().getTimeInMillis() - currentWorker.getStart().getTimeInMillis()); // The time track from the worker start.
            txtProjectTimeSpan.setText(new TimeSpan(project.getTotalTrack().getTotalMilliseconds() + track.getTotalMilliseconds()).toString()); // Updates the UI time display.

            // Registers the worker to a listener fired when to update the UI (every 1 second):
            if (getTimeTrackService() != null) {
                getTimeTrackService().registerWorker(currentWorker, new TrackTimeService.TimeTrackerListener() {
                    @Override
                    public void onTrackUpdate() {
                        TimeSpan track = new TimeSpan(Calendar.getInstance().getTimeInMillis() - currentWorker.getStart().getTimeInMillis()); // The time track from the worker start.
                        txtProjectTimeSpan.setText(new TimeSpan(project.getTotalTrack().getTotalMilliseconds() + track.getTotalMilliseconds()).toString()); // Updates the UI time display.
                    }
                });
            }

            isRunning = true;
        }

        //endregion

        //region Private Methods

        /**
         * Starts a project worker.
         */
        private void startWorker() {
            if (isRunning)
                return;

            // Change UI tracking indicators:
            imgBtnProjectRun.setImageResource(R.mipmap.ic_big_pause_black);
            lblProjectRunning.setVisibility(View.VISIBLE);

            currentWorker = _repository.workers.start(project.getId()); // Starts the worker in the DB.
            project.getWorkers().add(0, currentWorker); // Adds the worker to the project object.

            // Registers the worker to a listener fired when to update the UI (every 1 second):
            getTimeTrackService().registerWorker(currentWorker, new TrackTimeService.TimeTrackerListener() {
                @Override
                public void onTrackUpdate() {
                    TimeSpan track = new TimeSpan(Calendar.getInstance().getTimeInMillis() - currentWorker.getStart().getTimeInMillis()); // The time track from the worker start.
                    txtProjectTimeSpan.setText(new TimeSpan(project.getTotalTrack().getTotalMilliseconds() + track.getTotalMilliseconds()).toString()); // Updates the UI time display.
                }
            });

            isRunning = true;

            // Moves the project to top:
            int position = getLayoutPosition();
            _repository.projects.movePositionToTop(position);
            Project.updateListPositionsForMoveTop(_projects, position);
            project.setPosition(0);
            _projects.remove(position);
            _projects.add(0, project);
            notifyItemMoved(position, 0);
            _fragment.scrollViewToTop();
        }

        /**
         * Ends a project worker.
         */
        private void endWorker() {
            if (!isRunning)
                return;

            // Change UI tracking indicators:
            imgBtnProjectRun.setImageResource(R.mipmap.ic_big_play_black);
            lblProjectRunning.setVisibility(View.INVISIBLE);

            // Unregisters the worker listener for update UI events.
            getTimeTrackService().unregisterWorker(currentWorker);

            // Checks if 1 second is passed (for donkey users):
            if (Calendar.getInstance().getTimeInMillis() - currentWorker.getStart().getTimeInMillis() > 999) {
                currentWorker.setEnd(Calendar.getInstance());
                project.getTotalTrack().add(new TimeSpan(currentWorker.getEnd().getTimeInMillis() - currentWorker.getStart().getTimeInMillis())); // Adds the worker track to the total track.

                _repository.projects.updateTotalTrack(project.getId(), project.getTotalTrack()); // Updates the total track in the DB.
                _repository.workers.end(currentWorker); // Ends the worker in the database.

                txtProjectTimeSpan.setText(project.getTotalTrack().toString()); // Sets the project total track.
            } else {
                // Removes this donkey user dummy action:
                _repository.workers.remove(currentWorker);
                project.getWorkers().remove(0);
            }

            isRunning = false;
            currentWorker = null;
        }

        /**
         * Deletes a project from the adapter.
         *
         * @param position The position of the project to delete.
         */
        private void deleteProject(final int position) {
            if (isRunning)
                this.endWorker();

            final Project p = _projects.get(position);

            _repository.projects.delete(p);
            _projects.remove(position);
            Project.updateListPositionsForDelete(_projects, position);
            notifyItemRemoved(position);

            _fragment.displayListState();

            Snackbar.make(getActivity().findViewById(R.id.coordinator), "\"" + p.getName() + "\" " + getActivity().getString(R.string.general_msg_deleted), 10000)
                    .setAction(getActivity().getString(R.string.general_btn_undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            p.setId(_repository.projects.restore(p));
                            Project.updateListPositionsForInsert(_projects, position);
                            _projects.add(position, p);
                            notifyItemInserted(position);

                            _fragment.displayListState();

                            if (position == 0)
                                _fragment.scrollViewToTop();
                        }
                    }).setActionTextColor(Utils.Colors.PRIMARY_COLOR).show();
        }

        /**
         * Updates a project in the adapter.
         *
         * @param position The position of the project in the adapter's list.
         * @param project  The project object to update, with the values to update.
         */
        private void updateProject(int position, Project project) {
            _repository.projects.update(project);
            _projects.set(position, project);
            notifyItemChanged(position);
        }

        /**
         * Shows the project description by the position.
         *
         * @param position The position of the project to show the description.
         */
        private void actionProjectInformation(int position) {
            ProjectInformationDialog.show(getActivity().getFragmentManager(), _projects.get(position));
        }

        /**
         * Edits the time of a project.
         *
         * @param position The position of the project to edit the time.
         */
        private void actionEditTime(final int position) {
            final Project proj = _projects.get(position);
            final TimeSpan lastTotalTrack = new TimeSpan(proj.getTotalTrack().getTotalMilliseconds());

            TimeSpanPickerDialog dialog = new TimeSpanPickerDialog();
            dialog.setProjectName(proj.getName());
            dialog.setTimeSpan(new TimeSpan(proj.getTotalTrack().getTotalMilliseconds()));
            dialog.setOnPositiveResultListener(new TimeSpanPickerDialog.PositiveResultListener() {
                @Override
                public void onPositiveResult(final TimeSpan trackEdit) {
                    proj.getTotalTrack().add(trackEdit);
                    final ProjectWorker worker = _repository.workers.trackEdit(proj.getId(), trackEdit);
                    updateProject(position, proj);

                    Snackbar.make(getActivity().findViewById(R.id.coordinator), getActivity().getString(R.string.dialog_time_span_picker_edit_success) + " \"" + proj.getName() + "\"", 10000)
                            .setAction(getActivity().getString(R.string.general_btn_undo), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    proj.setTotalTrack(lastTotalTrack);
                                    _repository.workers.remove(worker);
                                    updateProject(position, proj);
                                }
                            }).setActionTextColor(Utils.Colors.PRIMARY_COLOR).show();
                }
            });
            dialog.show(getActivity().getFragmentManager(), UIConsts.Fragments.TIME_SPAN_PICKER_DIALOG_TAG);
        }

        /**
         * Shows the project's display activity.
         */
        private void actionProjectDisplay() {
            Intent displayIntent = new Intent(getContext(), ProjectDisplayActivity.class);
            displayIntent.putExtra(UIConsts.Bundles.PROJECT_ID_KEY, project.getId());
            _fragment.startActivity(displayIntent);
        }

        /**
         * Method procedure for context menu action: "Edit".
         */
        private void actionEdit() {
            Intent editIntent = new Intent(getContext(), ProjectCreateActivity.class);
            editIntent.putExtra(UIConsts.Bundles.MODE.KEY_NAME, UIConsts.Bundles.MODE.UPDATE); // Tells the "Create Activity" to go in edit mode.
            editIntent.putExtra(UIConsts.Bundles.PROJECT_KEY, project);
            _fragment.startActivityForResult(editIntent, UIConsts.RequestCodes.EDIT_PROJECT_REQUEST_CODE);
        }

        /**
         * Moves a project to the archive.
         *
         * @param position The position of the project to move.
         */
        private void actionMoveToArchive(final int position) {
            final Project p = _projects.get(position);

            // Checks if currently running:
            if (isRunning) {
                // Asks the user to stop and move the project, because it's currently tracking:
                Snackbar.make(getActivity().findViewById(R.id.coordinator), R.string.project_move_to_archive_currently_tracking, 10000)
                        .setAction(getActivity().getString(R.string.general_btn_yes), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                endWorker();

                                p.setArchiveDate(Calendar.getInstance());
                                _repository.projects.moveToArchive(p);
                                _projects.remove(position);
                                Project.updateListPositionsForDelete(_projects, position);
                                notifyItemRemoved(position);
                                _fragment.displayListState();
                                _fragment.onProjectArchived(p);
                            }
                        }).setActionTextColor(Utils.Colors.PRIMARY_COLOR).show();
                return;
            }

            p.setArchiveDate(Calendar.getInstance());
            _repository.projects.moveToArchive(p);
            _projects.remove(position);
            Project.updateListPositionsForDelete(_projects, position);
            notifyItemRemoved(position);
            _fragment.displayListState();
            _fragment.onProjectArchived(p);

            // Shows the user the project moved to archive:
            Snackbar.make(getActivity().findViewById(R.id.coordinator), "\"" + p.getName() + "\" " + _fragment.getString(R.string.general_msg_archived), Snackbar.LENGTH_SHORT).show();
        }

        //endregion

    }

    //endregion

}
