package com.ohadshai.timeon.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.ohadshai.timeon.utils.ListItem;
import com.ohadshai.timeon.utils.TimeSpan;

import java.util.Calendar;

/**
 * Represents a project worker for a project.
 * Created by Ohad on 9/9/2016.
 */
public class ProjectWorker extends ListItem implements Parcelable {

    //region Private Members

    /**
     * Holds the id of the project the worker belongs.
     */
    private int projectId;

    /**
     * Holds the start time of the project worker.
     */
    private Calendar start;

    /**
     * Holds the end time of the project worker.
     */
    private Calendar end;

    /**
     * Holds the time span of a track edit.
     */
    private TimeSpan trackEdit;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of a project worker for a project.
     */
    public ProjectWorker() {
    }

    /**
     * C'tor
     * Initializes a new instance of a project worker for a project.
     *
     * @param projectId The id of the project the worker belongs.
     * @param start     The start time of the project worker.
     * @param end       The end time of the project worker.
     */
    public ProjectWorker(int projectId, Calendar start, Calendar end) {
        this.projectId = projectId;
        this.start = start;
        this.end = end;
    }

    /**
     * C'tor
     * Initializes a new instance of a project worker for a project.
     *
     * @param projectId The id of the project the worker belongs.
     * @param start     The start time of the project worker.
     * @param end       The end time of the project worker.
     * @param trackEdit The time span of a track edit.
     */
    public ProjectWorker(int projectId, Calendar start, Calendar end, TimeSpan trackEdit) {
        this.projectId = projectId;
        this.start = start;
        this.end = end;
        this.trackEdit = trackEdit;
    }

    //endregion

    //region Public API

    /**
     * Gets the id of the project the worker belongs.
     *
     * @return Returns the id of the project the worker belongs.
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * Sets the id of the project the worker belongs.
     *
     * @param projectId The id of the project the worker belongs to set.
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * Gets the start time of the project worker.
     *
     * @return Returns the start time of the project worker.
     */
    public Calendar getStart() {
        return start;
    }

    /**
     * Sets the start time of the project worker.
     *
     * @param start The start time of the project worker to set.
     */
    public void setStart(Calendar start) {
        this.start = start;
    }

    /**
     * Gets the end time of the project worker.
     *
     * @return Returns the end time of the project worker.
     */
    public Calendar getEnd() {
        return end;
    }

    /**
     * Sets the end time of the project worker.
     *
     * @param end The end time of the project worker to set.
     */
    public void setEnd(Calendar end) {
        this.end = end;
    }

    /**
     * Gets the time span of a track edit.
     *
     * @return Returns the time span of a track edit.
     */
    public TimeSpan getTrackEdit() {
        return trackEdit;
    }

    /**
     * Sets the time span of a track edit.
     *
     * @param trackEdit The time span of a track edit to set.
     */
    public void setTrackEdit(TimeSpan trackEdit) {
        this.trackEdit = trackEdit;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_EVENT;
    }

    //endregion

    //region [Parcelable]

    protected ProjectWorker(Parcel in) {
        projectId = in.readInt();

        long startVal = in.readLong();
        if (startVal > -1) {
            start = Calendar.getInstance();
            start.setTimeInMillis(startVal);
        }

        long endVal = in.readLong();
        if (endVal > -1) {
            end = Calendar.getInstance();
            end.setTimeInMillis(endVal);
        }

        long trackEditVal = in.readLong();
        if (trackEditVal != 0)
            trackEdit = new TimeSpan(trackEditVal);
    }

    public static final Creator<ProjectWorker> CREATOR = new Creator<ProjectWorker>() {
        @Override
        public ProjectWorker createFromParcel(Parcel in) {
            return new ProjectWorker(in);
        }

        @Override
        public ProjectWorker[] newArray(int size) {
            return new ProjectWorker[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(projectId);

        if (start == null)
            dest.writeLong(-1);
        else
            dest.writeLong(start.getTimeInMillis());

        if (end == null)
            dest.writeLong(-1);
        else
            dest.writeLong(end.getTimeInMillis());

        if (trackEdit == null)
            dest.writeLong(0);
        else
            dest.writeLong(trackEdit.getTotalMilliseconds());
    }

    //endregion

}

