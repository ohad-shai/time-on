package com.ohadshai.timeon.db;

/**
 * Holds all the constants for the database.
 * Created by Ohad on 9/10/2016.
 */
public class DBConsts {

    //region General Constants

    /**
     * Holds a constant for the database name.
     */
    public final static String DB_NAME = "TimeOnDB.db";

    /**
     * Holds a constant for the database version.
     */
    public final static int DB_VERSION = 1;

    //endregion

    /**
     * Holds all the constants for the "Projects" table.
     */
    public class Table_Projects {

        /**
         * Holds a constant for the table name "Projects".
         */
        public final static String TABLE_NAME = "[Projects]";

        /**
         * Holds a constant for the [Project Id] column name, in the "Projects" table.
         */
        public final static String COLUMN_id = "[_id]";

        /**
         * Holds a constant for the [Project Name] column name, in the "Projects" table.
         */
        public final static String COLUMN_Name = "[Name]";

        /**
         * Holds a constant for the [Project Description] column name, in the "Projects" table.
         */
        public final static String COLUMN_Description = "[Description]";

        /**
         * Holds a constant for the [Project Color] column name, in the "Projects" table.
         */
        public final static String COLUMN_Color = "[Color]";

        /**
         * Holds a constant for the [Project Total Tracking Time] column name, in the "Projects" table.
         */
        public final static String COLUMN_TotalTrack = "[TotalTrack]";

        /**
         * Holds a constant for the [Project Position] column name, in the "Projects" table.
         */
        public final static String COLUMN_Position = "[Position]";

        /**
         * Holds a constant for the [Project Create Date] column name, in the "Projects" table.
         */
        public final static String COLUMN_CreateDate = "[CreateDate]";

        /**
         * Holds a constant for the [Project Archive Date] column name, in the "Projects" table.
         */
        public final static String COLUMN_ArchiveDate = "[ArchiveDate]";

    }

    /**
     * Holds all the constants for the "ProjectWorkers" table.
     */
    public class Table_ProjectWorkers {

        /**
         * Holds a constant for the table name "ProjectWorkers".
         */
        public final static String TABLE_NAME = "[ProjectWorkers]";

        /**
         * Holds a constant for the [Project Id] column name, in the "ProjectWorkers" table.
         */
        public final static String COLUMN_ProjectID = "[ProjectID]";

        /**
         * Holds a constant for the [Project Worker Start Time] column name, in the "ProjectWorkers" table.
         */
        public final static String COLUMN_Start = "[Start]";

        /**
         * Holds a constant for the [Project Worker End Time] column name, in the "ProjectWorkers" table.
         */
        public final static String COLUMN_End = "[End]";

        /**
         * Holds a constant for the [Project Worker Track Edit] column name, in the "ProjectWorkers" table.
         */
        public final static String COLUMN_TrackEdit = "[TrackEdit]";

    }

}
