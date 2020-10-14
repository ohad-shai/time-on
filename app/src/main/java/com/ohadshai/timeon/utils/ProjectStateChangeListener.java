package com.ohadshai.timeon.utils;

import com.ohadshai.timeon.entities.Project;

/**
 * Represents an interface holds event occurs when a project state changes.
 * Created by Ohad on 3/11/2017.
 */
public interface ProjectStateChangeListener {

    /**
     * Occurs when a project is returned to active.
     *
     * @param project The returned to active.
     */
    public void onProjectActivated(Project project);

    /**
     * Occurs when a project is moved to the archive.
     *
     * @param project The moved to the archive.
     */
    public void onProjectArchived(Project project);

}
