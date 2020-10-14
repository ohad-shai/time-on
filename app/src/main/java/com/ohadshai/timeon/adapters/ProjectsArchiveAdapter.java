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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.db.DBHandler;
import com.ohadshai.timeon.entities.Project;
import com.ohadshai.timeon.entities.ProjectWorker;
import com.ohadshai.timeon.ui.UIConsts;
import com.ohadshai.timeon.ui.activities.ProjectCreateActivity;
import com.ohadshai.timeon.ui.activities.ProjectDisplayActivity;
import com.ohadshai.timeon.ui.dialogs.TimeSpanPickerDialog;
import com.ohadshai.timeon.ui.fragments.ProjectsArchiveFragment;
import com.ohadshai.timeon.utils.SelectionHelper;
import com.ohadshai.timeon.utils.TimeSpan;
import com.ohadshai.timeon.utils.Utils;

import java.util.ArrayList;

import static com.ohadshai.timeon.utils.SelectionHelper.SELECTED;

/**
 * Represents a projects archive adapter for RecyclerView.
 * Created by Ohad on 3/11/2017.
 */
public class ProjectsArchiveAdapter extends RecyclerView.Adapter<ProjectsArchiveAdapter.ViewHolder> {

    //region Private Members

    /**
     * Holds the list of projects to adapt.
     */
    private ArrayList<Project> _projects;

    /**
     * Holds the fragment owner of the adapter.
     */
    private ProjectsArchiveFragment _fragment;

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of a projects archive adapter for RecyclerView.
     *
     * @param projects The list of projects to adapt.
     * @param fragment The fragment owner of the adapter.
     */
    public ProjectsArchiveAdapter(ArrayList<Project> projects, ProjectsArchiveFragment fragment) {
        this._projects = projects;
        this._fragment = fragment;
        this._repository = DBHandler.getInstance(getContext());
    }

    //endregion

    //region Adapter Events

    @Override
    public ProjectsArchiveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflates the custom layout:
        View projectView = inflater.inflate(R.layout.item_project_archive_small, parent, false);
        return new ViewHolder(projectView); // Returns a new holder instance.
    }

    @Override
    public void onBindViewHolder(ProjectsArchiveAdapter.ViewHolder viewHolder, int position) {
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

    //region Inner Classes

    class ViewHolder extends RecyclerView.ViewHolder implements SelectionHelper.SelectionItemController<Project>, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        //region Private Members

        /**
         * Holds the project object of the current position item.
         */
        private Project project;

        private CardView cardProject;
        private TextView lblProjectName;
        private View viewProjectColor;
        private TextView txtProjectTimeSpan;
        private Button btnProjectActions;

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

            txtProjectTimeSpan = (TextView) itemView.findViewById(R.id.txtProjectTimeSpan);

            btnProjectActions = (Button) itemView.findViewById(R.id.btnProjectActions);
            btnProjectActions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performLongClick();
                }
            });
            btnProjectActions.setOnCreateContextMenuListener(this);

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
            getActivity().getMenuInflater().inflate(R.menu.context_menu_project_archive_actions, menu);

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
                case R.id.actionReturnToActive:
                    this.actionReturnToActive(getAdapterPosition());
                    return true;
                case R.id.actionEditTime:
                    this.actionEditTime(getAdapterPosition());
                    return true;
                case R.id.actionEdit:
                    this.actionEdit();
                    return true;
                case R.id.actionDelete:
                    this.deleteProject(getAdapterPosition());
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

            // Sets the project color.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewProjectColor.setBackgroundColor(project.getColor());
            } else {
                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.project_color_old_api_corner_radius);
                drawable.setColorFilter(project.getColor(), PorterDuff.Mode.SRC_ATOP);
                // Sets the project color:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                    viewProjectColor.setBackgroundDrawable(drawable);
                else
                    viewProjectColor.setBackground(drawable);
            }

            this.bindSelection();
        }

        //endregion

        //region Private Methods

        /**
         * Deletes a project from the adapter.
         *
         * @param position The position of the project to delete.
         */
        private void deleteProject(final int position) {
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
                            _projects.add(position, p);
                            notifyItemInserted(position);

                            _fragment.displayListState();
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
         * Shows the project's display activity.
         */
        private void actionProjectDisplay() {
            Intent displayIntent = new Intent(getContext(), ProjectDisplayActivity.class);
            displayIntent.putExtra(UIConsts.Bundles.PROJECT_ID_KEY, project.getId());
            _fragment.startActivity(displayIntent);
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
         * Method procedure for context menu action: "Edit".
         */
        private void actionEdit() {
            Intent editIntent = new Intent(getContext(), ProjectCreateActivity.class);
            editIntent.putExtra(UIConsts.Bundles.MODE.KEY_NAME, UIConsts.Bundles.MODE.UPDATE); // Tells the "Create Activity" to go in edit mode.
            editIntent.putExtra(UIConsts.Bundles.PROJECT_KEY, project);
            _fragment.startActivityForResult(editIntent, UIConsts.RequestCodes.EDIT_PROJECT_REQUEST_CODE);
        }

        /**
         * Returns a project to active.
         *
         * @param position The position of the project to return.
         */
        private void actionReturnToActive(int position) {
            Project p = _projects.get(position);

            _repository.projects.returnToActive(p);
            _projects.remove(position);
            Project.updateListPositionsForDelete(_projects, position);
            notifyItemRemoved(position);
            _fragment.displayListState();

            p.setArchiveDate(null);
            p.setPosition(0);
            _fragment.onProjectActivated(p);

            // Shows the user the project moved to archive:
            Snackbar.make(getActivity().findViewById(R.id.coordinator), "\"" + p.getName() + "\" " + _fragment.getString(R.string.general_msg_activated), Snackbar.LENGTH_SHORT).show();
        }

        //endregion

    }

    //endregion

}
