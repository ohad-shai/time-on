package com.ohadshai.timeon.utils;

/**
 * Represents a list item, defines the type of the item.
 * Created by Ohad on 3/12/2017.
 */
public abstract class ListItem {

    //region Constants

    /**
     * Holds a constant for an item type: "Header".
     */
    public static final int TYPE_HEADER = 0;

    /**
     * Holds a constant for an item type: "Event".
     */
    public static final int TYPE_EVENT = 1;

    //endregion

    //region Public API

    /**
     * Gets the type of the list item.
     *
     * @return Returns the type of the list item.
     */
    abstract public int getType();

    //endregion

}
