package com.sofi.knittimer.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Default User on 22.5.2017.
 */

public final class ProjectContract {

    // Defines the table and column names for the database.

    public static final String CONTENT_AUTHORITY = "com.sofi.knittimer.data.projectprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PROJECTS = "projects";

    public static final class ProjectEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PROJECTS).build();

        public static final String TABLE_PROJECTS = "projects";

        public static final String _NAME = "name";
        public static final String _TIME_SPENT = "time_spent";
        public static final String _PERCENT_DONE = "percentage_done";

    }

}
