package com.ohadshai.timeon.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ohadshai.timeon.entities.Project;
import com.ohadshai.timeon.entities.ProjectWorker;
import com.ohadshai.timeon.utils.TimeSpan;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Represents a handler for database interactions.
 * Created by Ohad on 9/10/2016.
 */
public class DBHandler {

    //region Private Members

    /**
     * Holds the instance of the DBHandler for all the application.
     */
    private static DBHandler _instance;

    /**
     * Holds the helper of the database interactions.
     */
    private DBHelper helper;

    //endregion

    //region (Database Interactions) Public Members

    /**
     * Holds the database interaction with: "Projects".
     */
    public DBHandler.Projects projects;

    /**
     * Holds the database interaction with: "Project Workers".
     */
    public DBHandler.ProjectWorkers workers;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of a handler for database interactions.
     *
     * @param context The context of the handler owner.
     */
    public DBHandler(Context context) {
        helper = new DBHelper(context, DBConsts.DB_NAME, null, DBConsts.DB_VERSION);

        // Initializes the database interaction objects:
        this.projects = new Projects();
        this.workers = new ProjectWorkers();
    }

    //endregion

    //region Public Static API

    /**
     * Gets the DBHandler instance of the application, or creates a new instance if null.
     *
     * @param context The context of the DBHandler owner.
     * @return Returns the DBHandler instance of the application.
     */
    public static DBHandler getInstance(Context context) {
        if (_instance == null)
            _instance = new DBHandler(context.getApplicationContext());

        return _instance;
    }

    //endregion

    //region Inner Classes

    /**
     * Represents the database interaction with: "Projects".
     */
    public class Projects {

        /**
         * Creates a new project to the database.
         *
         * @param project The project object to create.
         * @return Returns the project id created in the database.
         */
        public int create(Project project) {
            if (project == null)
                throw new NullPointerException("project");
            if (project.getName() == null || project.getName().equals(""))
                throw new NullPointerException("project: name");

            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Projects.COLUMN_Name, project.getName());
            values.put(DBConsts.Table_Projects.COLUMN_Description, project.getDescription());
            values.put(DBConsts.Table_Projects.COLUMN_Color, project.getColor());
            values.put(DBConsts.Table_Projects.COLUMN_TotalTrack, project.getTotalTrack().getTotalMilliseconds());
            values.put(DBConsts.Table_Projects.COLUMN_Position, (project.getPosition() - 1)); // Decreases the position by one (should be 0), because all positions will be updated by one.
            values.put(DBConsts.Table_Projects.COLUMN_CreateDate, project.getCreateDate().getTimeInMillis());
            if (project.getArchiveDate() != null)
                values.put(DBConsts.Table_Projects.COLUMN_ArchiveDate, project.getArchiveDate().getTimeInMillis());

            int projectId = (int) db.insert(DBConsts.Table_Projects.TABLE_NAME, null, values);

            // Updates all the positions (Increases all positions by one):
            db.execSQL("UPDATE " + DBConsts.Table_Projects.TABLE_NAME + " SET " + DBConsts.Table_Projects.COLUMN_Position + "=(" + DBConsts.Table_Projects.COLUMN_Position + " + 1)");

            //region Inserts the project workers...

            for (ProjectWorker worker : project.getWorkers()) {
                values = new ContentValues();
                values.put(DBConsts.Table_ProjectWorkers.COLUMN_ProjectID, projectId);
                values.put(DBConsts.Table_ProjectWorkers.COLUMN_Start, worker.getStart().getTimeInMillis());
                if (worker.getEnd() != null)
                    values.put(DBConsts.Table_ProjectWorkers.COLUMN_End, worker.getEnd().getTimeInMillis());
                db.insert(DBConsts.Table_ProjectWorkers.TABLE_NAME, null, values);
            }

            //endregion

            db.setTransactionSuccessful();
            db.endTransaction();

            return projectId;
        }

        /**
         * Updates a project from the database.
         *
         * @param project The project entity to update from the database, with the new values.
         */
        public void update(Project project) {
            if (project == null)
                throw new NullPointerException("project");
            if (project.getId() < 1)
                throw new IllegalStateException("project: id");
            if (project.getName() == null || project.getName().equals(""))
                throw new NullPointerException("project: name");

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Projects.COLUMN_Name, project.getName());
            values.put(DBConsts.Table_Projects.COLUMN_Description, project.getDescription());
            values.put(DBConsts.Table_Projects.COLUMN_Color, project.getColor());
            values.put(DBConsts.Table_Projects.COLUMN_TotalTrack, project.getTotalTrack().getTotalMilliseconds());
            values.put(DBConsts.Table_Projects.COLUMN_Position, project.getPosition());
            values.put(DBConsts.Table_Projects.COLUMN_CreateDate, project.getCreateDate().toString());
            if (project.getArchiveDate() != null)
                values.put(DBConsts.Table_Projects.COLUMN_ArchiveDate, project.getArchiveDate().getTimeInMillis());
            else
                values.putNull(DBConsts.Table_Projects.COLUMN_ArchiveDate);

            db.update(DBConsts.Table_Projects.TABLE_NAME, values, DBConsts.Table_Projects.COLUMN_id + "=" + project.getId(), null);
        }

        /**
         * Updates a project from the database.
         *
         * @param projectId  The project id to update from the database.
         * @param totalTrack The new total track value to update.
         */
        public void updateTotalTrack(int projectId, TimeSpan totalTrack) {
            if (projectId < 1)
                throw new IllegalArgumentException("projectId");

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Projects.COLUMN_TotalTrack, totalTrack.getTotalMilliseconds());

            db.update(DBConsts.Table_Projects.TABLE_NAME, values, DBConsts.Table_Projects.COLUMN_id + "=" + projectId, null);
        }

        /**
         * Deletes a project from the database.
         *
         * @param project The project to delete.
         */
        public void delete(Project project) {
            if (project == null)
                throw new IllegalArgumentException("project");
            if (project.getId() < 1)
                throw new IllegalStateException("project: id (" + project.getId() + ")");

            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();

            db.delete(DBConsts.Table_Projects.TABLE_NAME, DBConsts.Table_Projects.COLUMN_id + "=" + project.getId(), null);

            // Updates positions (Decreases all the bigger positions by one):
            db.execSQL("UPDATE " + DBConsts.Table_Projects.TABLE_NAME + " SET " + DBConsts.Table_Projects.COLUMN_Position + "=(" + DBConsts.Table_Projects.COLUMN_Position + " - 1) WHERE " + DBConsts.Table_Projects.COLUMN_Position + ">" + project.getPosition());

            db.setTransactionSuccessful();
            db.endTransaction();
        }

        /**
         * Gets all the projects from the database.
         *
         * @return Returns a list of projects.
         */
        public ArrayList<Project> getAll() {
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Projects.TABLE_NAME, null, null, null, null, null, DBConsts.Table_Projects.COLUMN_Position);

            ArrayList<Project> projects = new ArrayList<>();
            while (cursor.moveToNext())
                projects.add(this.createProjectFromCursor(cursor));

            return projects;
        }

        /**
         * Gets a project by the id.
         *
         * @param projectId The id of the project to get.
         * @return Returns a project object if found, otherwise null.
         */
        public Project getById(int projectId) {
            if (projectId < 1)
                throw new IllegalArgumentException("projectId");

            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(DBConsts.Table_Projects.TABLE_NAME, null, DBConsts.Table_Projects.COLUMN_id + "=" + projectId, null, null, null, null);

            Project project = null;
            if (cursor.moveToFirst())
                project = this.createProjectFromCursor(cursor);

            return project;
        }

        /**
         * Checks if a project name already exists.
         *
         * @param projectName The project name to check if exists.
         * @return Returns true if the project name exists, otherwise false.
         */
        public boolean checkNameExists(String projectName) {
            if (projectName == null || projectName.equals(""))
                throw new NullPointerException("projectName");

            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Projects.TABLE_NAME, new String[]{DBConsts.Table_Projects.COLUMN_id}, DBConsts.Table_Projects.COLUMN_Name + "=?", new String[]{projectName}, null, null, null);
            return cursor.getCount() > 0;
        }

        /**
         * Moves a project position to top.
         *
         * @param currentProjectPosition The current position of the project.
         */
        public void movePositionToTop(int currentProjectPosition) {
            if (currentProjectPosition < 0)
                throw new IllegalArgumentException("currentProjectPosition");

            if (currentProjectPosition == 0)
                return;

            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();

            db.execSQL("UPDATE " + DBConsts.Table_Projects.TABLE_NAME + " SET " + DBConsts.Table_Projects.COLUMN_Position + "=-1 WHERE " + DBConsts.Table_Projects.COLUMN_Position + "=" + currentProjectPosition);

            // Updates positions (Increases all the smaller positions by one):
            db.execSQL("UPDATE " + DBConsts.Table_Projects.TABLE_NAME + " SET " + DBConsts.Table_Projects.COLUMN_Position + "=(" + DBConsts.Table_Projects.COLUMN_Position + " + 1) WHERE " + DBConsts.Table_Projects.COLUMN_Position + "<=" + currentProjectPosition);

            db.setTransactionSuccessful();
            db.endTransaction();
        }

        /**
         * Restores a project to the database.
         *
         * @param project The project object to restore.
         * @return Returns the project id created in the database.
         */
        public int restore(Project project) {
            if (project == null)
                throw new NullPointerException("project");
            if (project.getName() == null || project.getName().equals(""))
                throw new NullPointerException("project: name");

            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();

            // Updates positions (Increases all the bigger positions by one):
            db.execSQL("UPDATE " + DBConsts.Table_Projects.TABLE_NAME + " SET " + DBConsts.Table_Projects.COLUMN_Position + "=(" + DBConsts.Table_Projects.COLUMN_Position + " + 1) WHERE " + DBConsts.Table_Projects.COLUMN_Position + ">=" + project.getPosition());

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Projects.COLUMN_Name, project.getName());
            values.put(DBConsts.Table_Projects.COLUMN_Description, project.getDescription());
            values.put(DBConsts.Table_Projects.COLUMN_Color, project.getColor());
            values.put(DBConsts.Table_Projects.COLUMN_TotalTrack, project.getTotalTrack().getTotalMilliseconds());
            values.put(DBConsts.Table_Projects.COLUMN_Position, project.getPosition());
            values.put(DBConsts.Table_Projects.COLUMN_CreateDate, project.getCreateDate().getTimeInMillis());
            if (project.getArchiveDate() != null)
                values.put(DBConsts.Table_Projects.COLUMN_ArchiveDate, project.getArchiveDate().getTimeInMillis());

            int projectId = (int) db.insert(DBConsts.Table_Projects.TABLE_NAME, null, values);

            //region Inserts the project workers...

            for (ProjectWorker worker : project.getWorkers()) {
                values = new ContentValues();
                values.put(DBConsts.Table_ProjectWorkers.COLUMN_ProjectID, projectId);
                values.put(DBConsts.Table_ProjectWorkers.COLUMN_Start, worker.getStart().getTimeInMillis());
                if (worker.getEnd() != null)
                    values.put(DBConsts.Table_ProjectWorkers.COLUMN_End, worker.getEnd().getTimeInMillis());
                db.insert(DBConsts.Table_ProjectWorkers.TABLE_NAME, null, values);
            }

            //endregion

            db.setTransactionSuccessful();
            db.endTransaction();

            return projectId;
        }

        //region Active

        /**
         * Gets all the active projects from the database.
         *
         * @return Returns a list of active projects.
         */
        public ArrayList<Project> getAllActive() {
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Projects.TABLE_NAME, null, DBConsts.Table_Projects.COLUMN_ArchiveDate + " IS NULL", null, null, null, DBConsts.Table_Projects.COLUMN_Position);

            ArrayList<Project> projects = new ArrayList<>();
            while (cursor.moveToNext())
                projects.add(this.createProjectFromCursor(cursor));

            return projects;
        }

        //endregion

        //region Archive

        /**
         * Gets all the archive projects from the database.
         *
         * @return Returns a list of archive projects.
         */
        public ArrayList<Project> getAllArchive() {
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_Projects.TABLE_NAME, null, DBConsts.Table_Projects.COLUMN_ArchiveDate + " IS NOT NULL", null, null, null, DBConsts.Table_Projects.COLUMN_ArchiveDate + " DESC");

            ArrayList<Project> projects = new ArrayList<>();
            while (cursor.moveToNext())
                projects.add(this.createProjectFromCursor(cursor));

            return projects;
        }

        /**
         * Moves a project to archive.
         *
         * @param project The project to move archive.
         */
        public void moveToArchive(Project project) {
            if (project == null)
                throw new IllegalArgumentException("project");
            if (project.getId() < 1)
                throw new IllegalStateException("project: id (" + project.getId() + ")");

            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Projects.COLUMN_ArchiveDate, Calendar.getInstance().getTimeInMillis());
            db.update(DBConsts.Table_Projects.TABLE_NAME, values, DBConsts.Table_Projects.COLUMN_id + "=" + project.getId(), null);

            // Updates positions (Decreases all the bigger positions by one):
            db.execSQL("UPDATE " + DBConsts.Table_Projects.TABLE_NAME + " SET " + DBConsts.Table_Projects.COLUMN_Position + "=(" + DBConsts.Table_Projects.COLUMN_Position + " - 1) WHERE " + DBConsts.Table_Projects.COLUMN_Position + ">" + project.getPosition());

            db.setTransactionSuccessful();
            db.endTransaction();
        }

        /**
         * Returns a project to active.
         *
         * @param project The project to return active.
         */
        public void returnToActive(Project project) {
            if (project == null)
                throw new IllegalArgumentException("project");
            if (project.getId() < 1)
                throw new IllegalStateException("project: id (" + project.getId() + ")");

            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_Projects.COLUMN_Position, -1);
            values.putNull(DBConsts.Table_Projects.COLUMN_ArchiveDate);
            db.update(DBConsts.Table_Projects.TABLE_NAME, values, DBConsts.Table_Projects.COLUMN_id + "=" + project.getId(), null);

            // Updates positions (Increases all positions by one):
            db.execSQL("UPDATE " + DBConsts.Table_Projects.TABLE_NAME + " SET " + DBConsts.Table_Projects.COLUMN_Position + "=(" + DBConsts.Table_Projects.COLUMN_Position + " + 1)");

            db.setTransactionSuccessful();
            db.endTransaction();
        }

        //endregion

        //region Private Methods

        /**
         * Creates a project from the cursor.
         *
         * @param cursor The cursor to get the project from.
         * @return Returns the project created from the cursor.
         */
        private Project createProjectFromCursor(Cursor cursor) {
            int id = cursor.getInt(0);

            Calendar createDate = Calendar.getInstance();
            createDate.setTimeInMillis(cursor.getLong(6));

            Calendar archiveDate = null;
            if (!cursor.isNull(7)) {
                archiveDate = Calendar.getInstance();
                archiveDate.setTimeInMillis(cursor.getLong(7));
            }

            return new Project(id, cursor.getString(1), cursor.getString(2), cursor.getInt(3), new TimeSpan(cursor.getLong(4)), cursor.getInt(5), createDate, archiveDate, workers.getAllForProjectById(id));
        }

        //endregion

    }

    /**
     * Represents the database interaction with: "Project Workers".
     */
    public class ProjectWorkers {

        /**
         * Starts a new project worker.
         *
         * @param projectId The id of the project to start a new worker.
         */
        public ProjectWorker start(int projectId) {
            if (projectId < 1)
                throw new IllegalArgumentException("projectId");

            ProjectWorker worker = new ProjectWorker(projectId, Calendar.getInstance(), null);

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_ProjectWorkers.COLUMN_ProjectID, worker.getProjectId());
            values.put(DBConsts.Table_ProjectWorkers.COLUMN_Start, worker.getStart().getTimeInMillis());
            values.putNull(DBConsts.Table_ProjectWorkers.COLUMN_End);

            db.insert(DBConsts.Table_ProjectWorkers.TABLE_NAME, null, values);

            return worker;
        }

        /**
         * Ends a project worker.
         *
         * @param worker The project worker object to end.
         */
        public void end(ProjectWorker worker) {
            if (worker == null)
                throw new NullPointerException("worker");
            if (worker.getProjectId() < 1)
                throw new IllegalArgumentException("worker: projectId");

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_ProjectWorkers.COLUMN_End, worker.getEnd().getTimeInMillis());

            db.update(DBConsts.Table_ProjectWorkers.TABLE_NAME, values, DBConsts.Table_ProjectWorkers.COLUMN_ProjectID + "=" + worker.getProjectId() + " AND " + DBConsts.Table_ProjectWorkers.COLUMN_Start + "=" + worker.getStart().getTimeInMillis(), null);
        }

        /**
         * Edits the total track represented as a worker.
         *
         * @param projectId The id of the project to edit the total track.
         * @param trackEdit The time span object holds the track edit.
         */
        public ProjectWorker trackEdit(int projectId, TimeSpan trackEdit) {
            if (projectId < 1)
                throw new IllegalArgumentException("projectId");

            ProjectWorker worker = new ProjectWorker(projectId, Calendar.getInstance(), null, trackEdit);

            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBConsts.Table_ProjectWorkers.COLUMN_ProjectID, worker.getProjectId());
            values.put(DBConsts.Table_ProjectWorkers.COLUMN_Start, worker.getStart().getTimeInMillis());
            values.put(DBConsts.Table_ProjectWorkers.COLUMN_End, worker.getStart().getTimeInMillis());
            values.put(DBConsts.Table_ProjectWorkers.COLUMN_TrackEdit, worker.getTrackEdit().getTotalMilliseconds());

            db.insert(DBConsts.Table_ProjectWorkers.TABLE_NAME, null, values);

            return worker;
        }

        /**
         * Removes a project worker.
         *
         * @param worker The project worker object to remove.
         */
        public void remove(ProjectWorker worker) {
            if (worker == null)
                throw new NullPointerException("worker");
            if (worker.getProjectId() < 1)
                throw new IllegalArgumentException("worker: projectId");

            SQLiteDatabase db = helper.getWritableDatabase();

            db.delete(DBConsts.Table_ProjectWorkers.TABLE_NAME, DBConsts.Table_ProjectWorkers.COLUMN_ProjectID + "=" + worker.getProjectId() + " AND " + DBConsts.Table_ProjectWorkers.COLUMN_Start + "=" + worker.getStart().getTimeInMillis(), null);
        }

        /**
         * Gets all the project workers for a project by the project id.
         *
         * @param projectId The id of the project to get all the workers.
         * @return Returns the list of project workers.
         */
        public ArrayList<ProjectWorker> getAllForProjectById(int projectId) {
            if (projectId < 1)
                return null;

            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_ProjectWorkers.TABLE_NAME, null, DBConsts.Table_ProjectWorkers.COLUMN_ProjectID + "=" + projectId, null, null, null, DBConsts.Table_ProjectWorkers.COLUMN_Start + " DESC");

            ArrayList<ProjectWorker> workers = new ArrayList<>();
            while (cursor.moveToNext())
                workers.add(this.createProjectWorkerFromCursor(cursor));

            return workers;
        }

        /**
         * Gets all the running project workers.
         *
         * @return Returns a list of all the running project workers.
         */
        public ArrayList<ProjectWorker> getAllRunningWorkers() {
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor cursor = db.query(DBConsts.Table_ProjectWorkers.TABLE_NAME, null, DBConsts.Table_ProjectWorkers.COLUMN_End + " IS NULL", null, null, null, null);

            ArrayList<ProjectWorker> workers = new ArrayList<>();
            while (cursor.moveToNext())
                workers.add(this.createProjectWorkerFromCursor(cursor));

            return workers;
        }

        //region Private Methods

        /**
         * Creates a project worker from the cursor.
         *
         * @param cursor The cursor to get the project worker from.
         * @return Returns the project worker created from the cursor.
         */
        private ProjectWorker createProjectWorkerFromCursor(Cursor cursor) {
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(cursor.getLong(1));

            Calendar end = null;
            if (!cursor.isNull(2)) {
                end = Calendar.getInstance();
                end.setTimeInMillis(cursor.getLong(2));
            }

            TimeSpan trackEdit = null;
            if (!cursor.isNull(3))
                trackEdit = new TimeSpan(cursor.getLong(3));

            return new ProjectWorker(cursor.getInt(0), start, end, trackEdit);
        }

        //endregion

    }

    //endregion

}
