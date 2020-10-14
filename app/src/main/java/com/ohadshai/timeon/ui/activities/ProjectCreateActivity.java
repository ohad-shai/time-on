package com.ohadshai.timeon.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.db.DBHandler;
import com.ohadshai.timeon.entities.Project;
import com.ohadshai.timeon.ui.UIConsts;
import com.ohadshai.timeon.utils.TopAlert;

import petrov.kristiyan.colorpicker.ColorPicker;

public class ProjectCreateActivity extends AppCompatActivity {

    //region Private Members

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    /**
     * Holds the mode of the activity (whether it's create, edit, etc...).
     */
    private int _mode;

    /**
     * Holds the project object (used in edit mode).
     */
    private Project _project;

    /**
     * Holds the color of the project.
     */
    private int _projectColor = Color.LTGRAY;

    /**
     * Holds the EditText control for the project name.
     */
    private EditText _txtProjectName;

    /**
     * Holds the EditText control for the project description.
     */
    private EditText _txtProjectDescription;

    /**
     * Holds the button control of the color picker.
     */
    private Button _btnSelectColor;

    /**
     * Holds the layout color selection.
     */
    private FrameLayout _flColorSelection;

    //endregion

    //region Events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Adds the "Navigate Up to Parent Activity" action.

        _mode = getIntent().getIntExtra(UIConsts.Bundles.MODE.KEY_NAME, UIConsts.Bundles.MODE.UNSPECIFIED); // Gets the mode of the activity.

        if (_mode == UIConsts.Bundles.MODE.UPDATE)
            setTitle(getString(R.string.project_edit_activity_title)); // If in edit mode, then changes the title to "Edit Movie".

        this.initControls();

        if (savedInstanceState == null)
            this.firstInitControls();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSaveProject:
                this.actionSaveProject();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(UIConsts.Bundles.MODE.KEY_NAME, _mode);
        outState.putParcelable(UIConsts.Bundles.PROJECT_KEY, _project);
        outState.putInt(UIConsts.Bundles.COLOR_KEY, _projectColor);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        _mode = savedInstanceState.getInt(UIConsts.Bundles.MODE.KEY_NAME);
        _project = savedInstanceState.getParcelable(UIConsts.Bundles.PROJECT_KEY);
        _projectColor = savedInstanceState.getInt(UIConsts.Bundles.COLOR_KEY, Color.LTGRAY);

        if (_projectColor != Color.LTGRAY)
            this.changeProjectColor(_projectColor);
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all view controls.
     */
    private void initControls() {

        _repository = DBHandler.getInstance(this); // Initializes the DB.

        _txtProjectName = (EditText) findViewById(R.id.txtCreateProjectName);

        _txtProjectDescription = (EditText) findViewById(R.id.txtCreateProjectDescription);

        _flColorSelection = (FrameLayout) findViewById(R.id.flColorSelection);

        _btnSelectColor = (Button) findViewById(R.id.btnCreateProjectColor);
        _btnSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ColorPicker colorPicker = new ColorPicker(ProjectCreateActivity.this);
                colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        changeProjectColor(color);
                    }
                }).addListenerButton("Default", new ColorPicker.OnButtonListener() {
                    @Override
                    public void onClick(View v, int position, int color) {
                        changeProjectColor(Color.LTGRAY);
                        colorPicker.dismissDialog();
                    }
                }).disableDefaultButtons(false).setDefaultColorButton(Color.LTGRAY).setRoundColorButton(true).setColumns(5).show();
            }
        });

    }

    /**
     * Initializes all view controls, for the first initialization.
     */
    private void firstInitControls() {

        // Checks if the activity is in edit mode:
        if (_mode == UIConsts.Bundles.MODE.UPDATE) {
            _project = getIntent().getParcelableExtra(UIConsts.Bundles.PROJECT_KEY);
            if (_project == null)
                throw new NullPointerException("PROJECT_KEY not provided.");

            _txtProjectName.setText(_project.getName());
            _txtProjectDescription.setText(_project.getDescription());
            this.changeProjectColor(_project.getColor());
        }

    }

    /**
     * Changes the project color.
     *
     * @param color The color to set.
     */
    private void changeProjectColor(int color) {
        this._flColorSelection.setBackgroundColor(color);
        this._projectColor = color;
    }

    /**
     * Method procedure for menu action: "Save Project".
     */
    private void actionSaveProject() {
        final String projectName = _txtProjectName.getText().toString();
        final String projectDesc = _txtProjectDescription.getText().toString();

        //region Validations

        if (projectName.trim().equals("")) {
            TopAlert.make(findViewById(R.id.coordinator), (ViewGroup) findViewById(R.id.llFloatingBelowTopAlert), R.string.project_validation_name_empty).show();
            return;
        } else if (_mode != UIConsts.Bundles.MODE.UPDATE && _repository.projects.checkNameExists(projectName)) {
            TopAlert.make(findViewById(R.id.coordinator), (ViewGroup) findViewById(R.id.llFloatingBelowTopAlert), R.string.project_validation_name_exists).show();
            return;
        } else if (_mode == UIConsts.Bundles.MODE.UPDATE && !projectName.toLowerCase().equals(_project.getName().toLowerCase())) {
            // If in edit mode, and the project name is different, then checks if a project already exists in the database:
            if (_repository.projects.checkNameExists(projectName)) {
                TopAlert.make(findViewById(R.id.coordinator), (ViewGroup) findViewById(R.id.llFloatingBelowTopAlert), R.string.project_validation_name_exists).show();
                return;
            }
        }

        //endregion

        Intent returnIntent = new Intent();

        // Checks if in edit mode:
        if (_mode == UIConsts.Bundles.MODE.UPDATE) {
            _project.setName(projectName);
            _project.setDescription(projectDesc);
            _project.setColor(_projectColor);
            _repository.projects.update(_project); // Updates the project in the database.
            returnIntent.putExtra(UIConsts.Bundles.PROJECT_POSITION_KEY, _project.getPosition());
        } else {
            _repository.projects.create(Project.make(projectName, projectDesc, _projectColor)); // Saves the new project to the database.
        }

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    //endregion

}
