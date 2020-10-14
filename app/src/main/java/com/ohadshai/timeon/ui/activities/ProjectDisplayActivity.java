package com.ohadshai.timeon.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.adapters.ProjectWorkersAdapter;
import com.ohadshai.timeon.db.DBHandler;
import com.ohadshai.timeon.entities.HeaderListItem;
import com.ohadshai.timeon.entities.Project;
import com.ohadshai.timeon.entities.ProjectWorker;
import com.ohadshai.timeon.ui.UIConsts;
import com.ohadshai.timeon.ui.dialogs.ProjectInformationDialog;
import com.ohadshai.timeon.utils.ListItem;
import com.ohadshai.timeon.utils.SelectionHelper;
import com.ohadshai.timeon.utils.TimeSpan;
import com.ohadshai.timeon.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ohadshai.timeon.R.id.rvWorkers;

public class ProjectDisplayActivity extends AppCompatActivity implements SelectionHelper.SelectionController<ProjectWorker> {

    //region Private Members

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    /**
     * Holds the project object.
     */
    private Project _project;

    /**
     * Holds the RecyclerView for the project workers list.
     */
    private RecyclerView _rvWorkers;

    /**
     * Holds the adapter of the project workers for the {@link RecyclerView}.
     */
    private ProjectWorkersAdapter _adapter;

    /**
     * Holds the View layout to display when the workers list is empty.
     */
    private View _layoutWorkersEmpty;

    /**
     * Holds the items in the list.
     */
    private ArrayList<ListItem> _items = new ArrayList<>();

    /**
     * Holds the {@link TextView} control for the project total track label.
     */
    private TextView _lblProjectTimeSpan;

    //region [ Selection ]

    /**
     * Holds he {@link LinearLayout} control for the total track layout.
     */
    private LinearLayout _llTotalTrackLayout;

    /**
     * Holds the selection helper for the fragment.
     */
    private SelectionHelper<ProjectWorker> _selectionHelper;

    //endregion

    //endregion

    //region Events

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_display);

        _repository = DBHandler.getInstance(this);

        _project = _repository.projects.getById(getIntent().getIntExtra(UIConsts.Bundles.PROJECT_ID_KEY, -1)); // Gets the project object from the intent.
        if (_project == null)
            throw new NullPointerException("_project");

        //region Toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        final ImageButton imgbtnNavigateUp = (ImageButton) toolbar.findViewById(R.id.imgbtnNavigateUp);
        imgbtnNavigateUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgbtnNavigateUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.UI.showInformationToast(imgbtnNavigateUp, R.string.general_btn_navigate_up);
                return true;
            }
        });

        ((CircleImageView) toolbar.findViewById(R.id.action_bar_icon)).setImageDrawable(new ColorDrawable(_project.getColor()));

        ((TextView) toolbar.findViewById(R.id.action_bar_title)).setText(_project.getName());

        if (_project.getArchiveDate() == null)
            ((TextView) toolbar.findViewById(R.id.action_bar_subtitle)).setText(R.string.general_active);
        else
            ((TextView) toolbar.findViewById(R.id.action_bar_subtitle)).setText(getString(R.string.general_msg_archived_from) + String.format(" %1$tb %1$td, %1$tY", _project.getArchiveDate()));

        setSupportActionBar(toolbar);

        //endregion

        _lblProjectTimeSpan = (TextView) findViewById(R.id.lblProjectTimeSpan);
        _lblProjectTimeSpan.setText(_project.getTotalTrack().toString());

        _layoutWorkersEmpty = findViewById(R.id.layoutWorkersEmpty);

        Map<Calendar, List<ProjectWorker>> map = this.toMap(_project.getWorkers());
        for (Calendar date : map.keySet()) {
            _items.add(new HeaderListItem(date));

            for (ProjectWorker worker : map.get(date))
                _items.add(worker);
        }

        _rvWorkers = (RecyclerView) findViewById(rvWorkers);
        _adapter = new ProjectWorkersAdapter(_items, _project.getWorkers(), this);
        _rvWorkers.setAdapter(_adapter);
        _rvWorkers.setLayoutManager(new LinearLayoutManager(this));

        //region Selection Related...

        _llTotalTrackLayout = (LinearLayout) findViewById(R.id.llTotalTrackLayout);

        _selectionHelper = new SelectionHelper<>(_project.getWorkers(), new SelectionHelper.SelectionCallback<ProjectWorker>() {

            @Override
            public void onShowHeader() {
                _llTotalTrackLayout.setBackgroundResource(R.drawable.primary_cream_gradient);
            }

            @Override
            public void onAnimateShowHeader() {
                _llTotalTrackLayout.setBackgroundResource(R.drawable.primary_cream_gradient);
            }

            @Override
            public void onSelectionChanged(ArrayList<ProjectWorker> selection) {
                if (selection.size() > 0) {
                    TimeSpan selectionTotal = new TimeSpan(0);
                    for (ProjectWorker worker : selection) {
                        if (worker.getTrackEdit() != null)
                            selectionTotal.add(worker.getTrackEdit());
                        else if (worker.getEnd() != null)
                            selectionTotal.add(new TimeSpan(worker.getEnd().getTimeInMillis() - worker.getStart().getTimeInMillis()));
                        else
                            selectionTotal.add(new TimeSpan(Calendar.getInstance().getTimeInMillis() - worker.getStart().getTimeInMillis()));
                    }
                    _lblProjectTimeSpan.setText(selectionTotal.toString());
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
                _llTotalTrackLayout.setBackgroundResource(R.drawable.white_cream_gradient);
                _lblProjectTimeSpan.setText(_project.getTotalTrack().toString());
            }

            @Override
            public void onAnimateHideHeader() {
                _llTotalTrackLayout.setBackgroundResource(R.drawable.white_cream_gradient);
                _lblProjectTimeSpan.setText(_project.getTotalTrack().toString());
            }

        });

        //endregion

        this.displayListState();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionMoreInformation:
                ProjectInformationDialog.show(getFragmentManager(), _project);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public SelectionHelper<ProjectWorker> getSelectionHelper() {
        return _selectionHelper;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Lets the selection helper handle the save instance state:
        _selectionHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null)
            return;

        // Lets the selection helper handle the restore instance state:
        _selectionHelper.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (_selectionHelper.isInSelectionMode()) {
            _selectionHelper.exitSelectionMode();
        } else {
            super.onBackPressed();
        }
    }

    //endregion

    //region Public API

    /**
     * Displays the state of the adapter (list / empty message), according to the items in the list.
     */
    public void displayListState() {

        if (_project.getWorkers().size() < 1) {
            _rvWorkers.setVisibility(View.GONE);
            _layoutWorkersEmpty.setVisibility(View.VISIBLE);
        } else {
            _layoutWorkersEmpty.setVisibility(View.GONE);
            _rvWorkers.setVisibility(View.VISIBLE);
        }
    }

    //endregion

    //region Private Methods

    /**
     * Maps a list of project workers.
     *
     * @param workers The list of project workers to map.
     * @return Returns the map created.
     */
    private Map<Calendar, List<ProjectWorker>> toMap(@NonNull List<ProjectWorker> workers) {
        Map<Calendar, List<ProjectWorker>> map = new TreeMap<>();
        for (ProjectWorker worker : workers) {
            Calendar date = Utils.Dates.clearTime(worker.getStart());

            List<ProjectWorker> value = map.get(date);
            if (value == null) {
                value = new ArrayList<>();
                map.put(date, value);
            }
            value.add(worker);
        }

        Map<Calendar, List<ProjectWorker>> sorted = new TreeMap<>(Collections.reverseOrder());
        sorted.putAll(map);
        return sorted;
    }

    //endregion

}
