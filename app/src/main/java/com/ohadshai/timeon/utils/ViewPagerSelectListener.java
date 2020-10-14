package com.ohadshai.timeon.utils;

/**
 * Represents a listener for a {@link android.support.v4.view.ViewPager} page (fragment) event that occurs when the page is selected.
 * Created by Ohad on 3/9/2017.
 */
public interface ViewPagerSelectListener {

    /**
     * Event occurs when the page is selected by the view pager.
     */
    void onPageSelected();

    /**
     * Event occurs when the page is unselected by the view pager.
     */
    void onPageUnselected();

}
