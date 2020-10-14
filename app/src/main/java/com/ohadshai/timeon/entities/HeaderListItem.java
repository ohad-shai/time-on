package com.ohadshai.timeon.entities;

import com.ohadshai.timeon.utils.ListItem;

import java.util.Calendar;

/**
 * Represents a header for a list of items, representing the datetime of the items.
 * Created by Ohad on 3/12/2017.
 */
public class HeaderListItem extends ListItem {

    //region Private Members

    /**
     * Holds the date the header is representing.
     */
    private Calendar _date;

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a header for a list of items, representing the datetime of the items.
     *
     * @param date The date the header is representing.
     */
    public HeaderListItem(Calendar date) {
        this._date = date;
    }

    //endregion

    //region Public API

    /**
     * Gets the date the header is representing.
     *
     * @return Returns the date the header is representing.
     */
    public Calendar getDate() {
        return _date;
    }

    /**
     * Sets the date the header is representing.
     *
     * @param date The date the header is representing to set.
     */
    public void setDate(Calendar date) {
        this._date = date;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_HEADER;
    }

    //endregion

}
