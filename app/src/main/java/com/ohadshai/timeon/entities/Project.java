package com.ohadshai.timeon.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.ohadshai.timeon.utils.TimeSpan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Represents a project entity.
 * Created by Ohad on 9/9/2016.
 */
public class Project implements Parcelable {

    //region Private Members

    /**
     * Holds the id of the project.
     */
    private int id;

    /**
     * Holds the name of the project.
     */
    private String name;

    /**
     * Holds the description of the project.
     */
    private String description;

    /**
     * Holds the color of the project.
     */
    private int color;

    /**
     * Holds the total tracking time of the project.
     */
    private TimeSpan totalTrack;

    /**
     * Holds the position of the project.
     */
    private int position;

    /**
     * Holds the date the project created.
     */
    private Calendar createDate;

    /**
     * Holds the date the project moved to the archive.
     */
    private Calendar archiveDate;

    /**
     * Holds a list of project workers.
     */
    private ArrayList<ProjectWorker> workers;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of a project entity.
     */
    public Project() {
        workers = new ArrayList<>();
    }

    /**
     * C'tor
     * Initializes a new instance of a project entity.
     *
     * @param id          The id of the project.
     * @param name        The name of the project.
     * @param description The description of the project.
     * @param color       The color of the project.
     * @param totalTrack  The total tracking time of the project.
     * @param position    The position of the project.
     * @param createDate  The project creation date.
     * @param archiveDate The date the project moved to the archive.
     * @param workers     The list of workers belongs to the project.
     */
    public Project(int id, String name, String description, int color, TimeSpan totalTrack, int position, Calendar createDate, Calendar archiveDate, ArrayList<ProjectWorker> workers) {
        if (name == null || name.equals(""))
            throw new NullPointerException("name");

        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.totalTrack = totalTrack;
        this.position = position;
        this.createDate = createDate;
        this.archiveDate = archiveDate;

        if (workers == null)
            this.workers = new ArrayList<>();
        else
            this.workers = workers;
    }

    //endregion

    //region Public Static API

    /**
     * Makes a new project.
     *
     * @param name        The name of the project.
     * @param description The description of the project.
     * @param color       The color of the project.
     * @return Returns the created project object.
     */
    public static Project make(String name, String description, int color) {
        return new Project(0, name, description, color, new TimeSpan(0), 0, Calendar.getInstance(), null, null);
    }

    /**
     * Updates positions in a list of projects, when a project is inserted to the list.
     * (NOTE: Call before inserted).
     *
     * @param projects         The list of projects to update the positions.
     * @param positionToInsert The position of the project to insert.
     */
    public static void updateListPositionsForInsert(ArrayList<Project> projects, int positionToInsert) {
        for (Project project : projects) {
            if (project.getPosition() >= positionToInsert)
                project.setPosition(project.getPosition() + 1);
        }
    }

    /**
     * Updates positions in a list of projects, when a project is deleted from the list.
     * (NOTE: Call after deleted).
     *
     * @param projects        The list of projects to update the positions.
     * @param positionDeleted The position of the project deleted.
     */
    public static void updateListPositionsForDelete(ArrayList<Project> projects, int positionDeleted) {
        for (Project project : projects) {
            if (project.getPosition() > positionDeleted)
                project.setPosition(project.getPosition() - 1);
        }
    }

    /**
     * Updates positions in a list of projects, when a project is moved to the top of the list.
     * (NOTE: Call before moved to top).
     *
     * @param projects         The list of projects to update the positions.
     * @param positionMovedTop The position of the project moved to the top.
     */
    public static void updateListPositionsForMoveTop(ArrayList<Project> projects, int positionMovedTop) {
        for (Project project : projects) {
            if (project.getPosition() < positionMovedTop)
                project.setPosition(project.getPosition() + 1);
        }
    }

    //endregion

    //region Public API

    /**
     * Gets the id of the project.
     *
     * @return Returns the id of the project.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the project.
     *
     * @param id The id of the project to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the project.
     *
     * @return Returns the name of the project.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the project.
     *
     * @param name The name of the project to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the project.
     *
     * @return Returns the description of the project.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the project.
     *
     * @param description The description of the project to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the color of the project.
     *
     * @return Returns the color of the project.
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the color of the project.
     *
     * @param color The color of the project to set.
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Gets the total tracking time of the project.
     *
     * @return Returns the total tracking time of the project.
     */
    public TimeSpan getTotalTrack() {
        return totalTrack;
    }

    /**
     * Sets the total tracking time of the project.
     *
     * @param totalTrack The total tracking time to set.
     */
    public void setTotalTrack(TimeSpan totalTrack) {
        this.totalTrack = totalTrack;
    }

    /**
     * Gets the position of the project.
     *
     * @return Returns the position of the project.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position of the project.
     *
     * @param position The position to set.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Gets the project creation date.
     *
     * @return Returns the project creation date.
     */
    public Calendar getCreateDate() {
        return createDate;
    }

    /**
     * Sets the project creation date.
     *
     * @param createDate The project creation date to set.
     */
    public void setCreateDate(Calendar createDate) {
        this.createDate = createDate;
    }

    /**
     * Gets the date the project moved to the archive.
     *
     * @return Returns the date the project moved to the archive.
     */
    public Calendar getArchiveDate() {
        return archiveDate;
    }

    /**
     * Sets the date the project moved to the archive.
     *
     * @param archiveDate The date the project moved to the archive to set.
     */
    public void setArchiveDate(Calendar archiveDate) {
        this.archiveDate = archiveDate;
    }

    /**
     * Gets a list of all the project workers.
     *
     * @return Returns a list of all the project workers.
     */
    public ArrayList<ProjectWorker> getWorkers() {
        return workers;
    }

    /**
     * Sets a list of workers to the project.
     *
     * @param workers The list of project workers to set.
     */
    public void setWorkers(ArrayList<ProjectWorker> workers) {
        this.workers = workers;
    }

    //endregion

    //region [Parcelable]

    protected Project(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        color = in.readInt();
        totalTrack = new TimeSpan(in.readLong());
        position = in.readInt();

        long create = in.readLong();
        if (create > -1) {
            createDate = Calendar.getInstance();
            createDate.setTimeInMillis(create);
        }

        long archive = in.readLong();
        if (archive > -1) {
            archiveDate = Calendar.getInstance();
            archiveDate.setTimeInMillis(archive);
        }

        workers = in.createTypedArrayList(ProjectWorker.CREATOR);
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(color);
        dest.writeLong(totalTrack.getTotalMilliseconds());
        dest.writeInt(position);

        if (createDate == null)
            dest.writeLong(-1);
        else
            dest.writeLong(createDate.getTimeInMillis());

        if (archiveDate == null)
            dest.writeLong(-1);
        else
            dest.writeLong(archiveDate.getTimeInMillis());

        dest.writeTypedList(workers);
    }

    //endregion

    //region Inner Classes

    /**
     * Represents a comparator for a position property in a {@link Project} object.
     */
    public static class PositionComparator implements Comparator<Project> {

        public int compare(Project left, Project right) {
            return Integer.valueOf(left.position).compareTo(right.position);
        }

    }

    //endregion

}
