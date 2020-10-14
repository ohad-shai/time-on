package com.ohadshai.timeon.utils;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.ui.activities.MainActivity;

/**
 * Represents a behavior for a layout that shrinks below {@link AppBarLayout}, when is shown.
 * Created by Ohad on 2/25/2017.
 */
public class ShrinkBelowAppBarBehavior extends CoordinatorLayout.Behavior<View> {

    //region Private Members

    /**
     * Holds the action bar height in PX.
     */
    private int _actionBarHeight;

    /**
     * Holds the status bar height in PX.
     */
    private int _statusBarHeight;

    //endregion

    //region C'tor

    /**
     * Initializes a new instance of a behavior for a layout that shrinks below {@link AppBarLayout}, when is shown.
     *
     * @param context The context owner.
     * @param attrs   The attribute set.
     */
    public ShrinkBelowAppBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        _actionBarHeight = Utils.UI.getActionBarHeight(context);
        _statusBarHeight = Utils.UI.getStatusBarHeight(context);
    }

    //endregion

    //region Events

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return (dependency instanceof AppBarLayout);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (MainActivity.IS_IN_SELECTION_MODE) {
            // For API 21+, status bar is included in the translation:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                child.setPadding(0, 0, 0, _actionBarHeight);
                ViewCompat.setTranslationY(child, _actionBarHeight);
            } else {
                // For below API 21, status bar is not included in the translation:
                child.setPadding(0, 0, 0, _actionBarHeight);
                ViewCompat.setTranslationY(child, _actionBarHeight);
            }
        } else if (dependency.getId() == R.id.appBarMain) {
            // For API 21+, status bar is included in the translation:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final int translation = dependency.getBottom() - _statusBarHeight;
                child.setPadding(0, 0, 0, translation);
                ViewCompat.setTranslationY(child, translation);
            } else {
                // For below API 21, status bar is not included in the translation:
                child.setPadding(0, 0, 0, dependency.getBottom());
                ViewCompat.setTranslationY(child, dependency.getBottom());
            }
        }

        return true;
    }

    //endregion

}
