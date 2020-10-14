package com.ohadshai.timeon.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Represents a helper for database interactions.
 * Created by Ohad on 9/8/2016.
 */
class DBHelper extends SQLiteOpenHelper {

    /**
     * C'tor
     * Initializes a new instance of a helper for database interactions.
     *
     * @param context The context of the DBHelper owner.
     * @param name    The name of the database.
     * @param factory
     * @param version The version of the database.
     */
    DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String tableProjects = "CREATE TABLE " + DBConsts.Table_Projects.TABLE_NAME + " (" +
                DBConsts.Table_Projects.COLUMN_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DBConsts.Table_Projects.COLUMN_Name + " TEXT NOT NULL, " +
                DBConsts.Table_Projects.COLUMN_Description + " TEXT, " +
                DBConsts.Table_Projects.COLUMN_Color + " INTEGER, " +
                DBConsts.Table_Projects.COLUMN_TotalTrack + " INTEGER DEFAULT 0, " +
                DBConsts.Table_Projects.COLUMN_Position + " INTEGER, " +
                DBConsts.Table_Projects.COLUMN_CreateDate + " INTEGER NOT NULL, " +
                DBConsts.Table_Projects.COLUMN_ArchiveDate + " INTEGER DEFAULT NULL, " +
                " UNIQUE (" + DBConsts.Table_Projects.COLUMN_Name + ") " +
                "); ";

        // Needs FK from: [Projects]:
        final String tableProjectWorkers = "CREATE TABLE " + DBConsts.Table_ProjectWorkers.TABLE_NAME + " (" +
                DBConsts.Table_ProjectWorkers.COLUMN_ProjectID + " INTEGER, " +
                DBConsts.Table_ProjectWorkers.COLUMN_Start + " INTEGER NOT NULL, " +
                DBConsts.Table_ProjectWorkers.COLUMN_End + " INTEGER, " +
                DBConsts.Table_ProjectWorkers.COLUMN_TrackEdit + " INTEGER, " +
                " FOREIGN KEY (" + DBConsts.Table_ProjectWorkers.COLUMN_ProjectID + ") REFERENCES " + DBConsts.Table_Projects.TABLE_NAME + " (" + DBConsts.Table_Projects.COLUMN_id + ") ON DELETE CASCADE  " +
                "); ";

        sqLiteDatabase.execSQL(tableProjects);
        sqLiteDatabase.execSQL(tableProjectWorkers);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (!db.isReadOnly()) {
            // Enables foreign key constraints.
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

}
