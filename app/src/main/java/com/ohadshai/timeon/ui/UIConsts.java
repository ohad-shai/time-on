package com.ohadshai.timeon.ui;

/**
 * Holds all the constants for the UI.
 * Created by Ohad on 11/17/2016.
 */
public class UIConsts {

    /**
     * Holds a constant for the application package name.
     */
    public static final String APP_PACKAGE_NAME = "com.ohadshai.timeon";

    /**
     * Holds all the constants for the Fragments.
     */
    public interface Fragments {

        /**
         * Holds a constant for the dialog fragment tag: "TimeSpanPickerDialog".
         */
        public static final String TIME_SPAN_PICKER_DIALOG_TAG = "time_span_picker_dialog_tag";

        /**
         * Holds a constant for the dialog fragment tag: "AppAboutDialog".
         */
        public static final String PROJECT_MORE_INFO_DIALOG_TAG = "project_more_info_dialog_tag";

        /**
         * Holds a constant for the dialog fragment tag: "Rate App".
         */
        public static final String RATE_APP_DIALOG_TAG = "rate_app_dialog_tag";

        /**
         * Holds a constant for the dialog fragment tag: "AboutDialog".
         */
        public static final String APP_ABOUT_DIALOG_TAG = "app_about_dialog_tag";

    }

    /**
     * Holds all the constants for the SharedPreferences.
     */
    public interface Preferences {

        /**
         * Holds a constant for the preference key: "Display Format".
         */
        public static final String DISPLAY_FORMAT_KEY = "pref_display_format_key";

        /**
         * Holds a constant for the preference value: "Maximized Display Format (Full Screen)".
         */
        public static final int DISPLAY_FORMAT_MAXIMIZED_VALUE = 1;

        /**
         * Holds a constant for the "Rate app" preference key.
         */
        public static final String RATE_APP_KEY = "pref_rate_app_key";

        /**
         * Holds a constant for the "About app" preference key.
         */
        public static final String ABOUT_APP_KEY = "pref_about_app_key";

    }

    /**
     * Holds all the constants for bundles.
     */
    public interface Bundles {

        /**
         * Holds a constant for a key name: "Project" (intended to send a project object).
         */
        public static final String PROJECT_KEY = "project_key";

        /**
         * Holds a constant for a key name: "Projects List" (intended to send a list of project object).
         */
        public static final String PROJECTS_LIST_KEY = "projects_list_key";

        /**
         * Holds a constant for a key name: "Project Id" (intended to send a project id).
         */
        public static final String PROJECT_ID_KEY = "project_id_key";

        /**
         * Holds a constant for a key name: "Project Position" (intended to send a project position).
         */
        public static final String PROJECT_POSITION_KEY = "project_position_key";

        /**
         * Holds a constant for a key name: "Color" (intended to send a color integer value).
         */
        public static final String COLOR_KEY = "color_key";

        /**
         * Holds all the constants that indicates a mode (like create, read, update, delete, select, etc...).
         */
        public interface MODE {

            /**
             * Holds a constant for the key name.
             */
            public static final String KEY_NAME = "mode";

            /**
             * Holds a constant for the "Mode" value: "Unspecified".
             */
            public static final int UNSPECIFIED = 0;

            /**
             * Holds a constant for the "Mode" value: "Create".
             */
            public static final int CREATE = 1;

            /**
             * Holds a constant for the "Mode" value: "Read".
             */
            public static final int READ = 2;

            /**
             * Holds a constant for the "Mode" value: "Update".
             */
            public static final int UPDATE = 3;

            /**
             * Holds a constant for the "Mode" value: "Delete".
             */
            public static final int DELETE = 4;

            /**
             * Holds a constant for the "Mode" value: "Select".
             */
            public static final int SELECT = 5;

        }

    }

    /**
     * Holds all the constants for request codes.
     */
    public interface RequestCodes {

        /**
         * Holds the request code for the "Project Create" activity.
         */
        public final static int CREATE_PROJECT_REQUEST_CODE = 2117;

        /**
         * Holds the request code for the "Project Edit" activity.
         */
        public final static int EDIT_PROJECT_REQUEST_CODE = 2217;

    }

}
