package com.ohadshai.timeon.loaders;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.ohadshai.timeon.db.DBHandler;
import com.ohadshai.timeon.entities.Project;

import java.util.ArrayList;

/**
 * Represents a loader for the active projects list.
 * Created by Ohad on 2/23/2017.
 */
public class ProjectsActiveLoader extends AsyncTaskLoader<ArrayList<Project>> {

    //region Private Members

    /**
     * Holds the database interactions object.
     */
    private DBHandler _repository;

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a loader for the active projects list.
     *
     * @param context The context owner.
     */
    public ProjectsActiveLoader(Context context) {
        super(context);
        _repository = DBHandler.getInstance(context);
    }

    //endregion

    //region Loader Events

    @Override
    public ArrayList<Project> loadInBackground() {
        return _repository.projects.getAllActive();
    }

    //endregion

}
