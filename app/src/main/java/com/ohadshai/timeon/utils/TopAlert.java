package com.ohadshai.timeon.utils;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Represents a top alert (based on the Snackbar).
 * Created by Ohad on 9/17/2016.
 */
public class TopAlert {

    //region Private Members

    /**
     * Holds the snackbar the TopAlert is based on.
     */
    private Snackbar _snackbar;

    //endregion

    //region C'tors

    /**
     * C'tor (Private)
     * Initializes a new instance of a top alert, based on the Snackbar.
     *
     * @param snackbar The Snackbar the TopAlert is based on.
     */
    private TopAlert(@NonNull Snackbar snackbar) {
        this._snackbar = snackbar;
    }

    //endregion

    //region Public API

    /**
     * Makes a new TopAlert.
     *
     * @param view           The coordinator owner of the TopAlert.
     * @param floatingLayout The floating layout (with behaviour), which is also in the coordinator.
     * @param resId          The string resource id that holds the message of the TopAlert to show.
     * @return Returns a new TopAlert object.
     */
    public static TopAlert make(@NonNull View view, @NonNull final ViewGroup floatingLayout, @StringRes int resId) {
        return make(view, floatingLayout, view.getResources().getString(resId), Snackbar.LENGTH_LONG);
    }

    /**
     * Makes a new TopAlert.
     *
     * @param view           The coordinator owner of the TopAlert.
     * @param floatingLayout The floating layout (with behaviour), which is also in the coordinator.
     * @param message        The message of the TopAlert to show.
     * @return Returns a new TopAlert object.
     */
    public static TopAlert make(@NonNull View view, @NonNull final ViewGroup floatingLayout, @NonNull String message) {
        return make(view, floatingLayout, message, Snackbar.LENGTH_LONG);
    }

    /**
     * Makes a new TopAlert.
     *
     * @param view           The coordinator owner of the TopAlert.
     * @param floatingLayout The floating layout (with behaviour), which is also in the coordinator.
     * @param message        The message of the TopAlert to show.
     * @return Returns a new TopAlert object.
     */
    public static TopAlert make(@NonNull View view, @NonNull final ViewGroup floatingLayout, @NonNull String message, int duration) {
        Snackbar sb = Snackbar.make(view, message, duration);

        final View sbView = sb.getView();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) sbView.getLayoutParams();
        params.gravity = Gravity.TOP;
        sbView.setLayoutParams(params);

        sb.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                floatingLayout.setPadding(0, 0, 0, 0);
                ViewCompat.setTranslationY(floatingLayout, 0); // Resets to the normal TranslationY.
                ViewCompat.setTranslationZ(floatingLayout, 0); // Resets to the normal depth.
            }
        });
        return new TopAlert(sb);
    }

    /**
     * Sets the TopAlert text.
     *
     * @param text The text to set the TopAlert.
     * @return Returns the TopAlert object.
     */
    public TopAlert setText(@NonNull String text) {
        this._snackbar.setText(text);
        return this;
    }

    /**
     * Sets how long the TopAlert will be shown.
     *
     * @param duration The duration the TopAlert will be shown.
     * @return Returns the TopAlert object.
     */
    public TopAlert setDuration(int duration) {
        this._snackbar.setDuration(duration);
        return this;
    }

    /**
     * Sets an action to be displayed in the TopAlert.
     *
     * @param text     The text of the action to display.
     * @param listener A callback to be invoked when the action is clicked.
     * @return Returns the TopAlert object.
     */
    public TopAlert setAction(@NonNull String text, @NonNull View.OnClickListener listener) {
        this._snackbar.setAction(text, listener);
        return this;
    }

    /**
     * Sets the text color of the action (specified in the setAction(CharSequence, View.OnClickListener)).
     *
     * @param color The text color of the action.
     * @return Returns the TopAlert object.
     */
    public TopAlert setActionTextColor(int color) {
        this._snackbar.setActionTextColor(color);
        return this;
    }

    /**
     * Adds a callback to the TopAlert events.
     *
     * @param callback The callback to add.
     * @return Returns the TopAlert object.
     */
    public TopAlert addCallback(@NonNull Snackbar.Callback callback) {
        this._snackbar.addCallback(callback);
        return this;
    }

    /**
     * Dismisses the TopAlert.
     */
    public void dismiss() {
        this._snackbar.dismiss();
    }

    /**
     * Shows the TopAlert to the UI.
     */
    public void show() {
        this._snackbar.show();
    }

    //endregion

}