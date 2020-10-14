package com.ohadshai.timeon.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Represents a behavior for a layout that shrinks below {@link TopAlert}, when is shown.
 * Created by Ohad on 2/25/2017.
 */
public class ShrinkBelowTopAlertBehavior extends CoordinatorLayout.Behavior<View> {

    //region C'tor

    public ShrinkBelowTopAlertBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //endregion

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float translation = Math.min(0, dependency.getTranslationY() - dependency.getHeight()) * -1;
        ViewCompat.setTranslationZ(child, 50);
        child.setPadding(0, (int) translation, 0, 0);
        return true;
    }

}
