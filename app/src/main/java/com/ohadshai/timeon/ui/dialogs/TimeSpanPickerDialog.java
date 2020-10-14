package com.ohadshai.timeon.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.utils.TimeSpan;

import java.util.Calendar;

/**
 * Represents a time span picker dialog.
 * Created by Ohad on 9/18/2016.
 */
public class TimeSpanPickerDialog extends DialogFragment {

    //region Private Members

    /**
     * Holds the project name for the dialog.
     */
    private String _projectName;

    /**
     * Holds the time span to initialize the dialog.
     */
    private TimeSpan _time;

    /**
     * Holds a number picker in the dialog (hours / minutes / seconds).
     */
    private NumberPicker _numPickerHours, _numPickerMinutes, _numPickerSeconds;

    /**
     * Holds the positive result listener.
     */
    private PositiveResultListener _positiveResult;

    //endregion

    //region Events


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_time_span_picker, null);

        this.initControls(dialogView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        return builder.create();
    }

    //endregion

    //region Public API

    /**
     * Sets the time span picker dialog title.
     *
     * @param name The title to set.
     * @return Returns the TimeSpanPickerDialog object.
     */
    public TimeSpanPickerDialog setProjectName(String name) {
        this._projectName = name;
        return this;
    }

    /**
     * Sets the time span to initialize the dialog.
     *
     * @param time The time span to initialize.
     * @return Returns the TimeSpanPickerDialog object.
     */
    public TimeSpanPickerDialog setTimeSpan(TimeSpan time) {
        this._time = time;
        return this;
    }

    /**
     * Sets an event listener for a positive result from the dialog.
     *
     * @param listener The listener to set.
     */
    public void setOnPositiveResultListener(PositiveResultListener listener) {
        _positiveResult = listener;
    }

    //endregion

    //region Private Methods

    /**
     * Initializes all dialog view controls.
     *
     * @param view The view of the dialog.
     */
    private void initControls(View view) {

        Calendar timeFix = Calendar.getInstance();
        timeFix.setTimeInMillis(_time.getTotalMilliseconds());
        timeFix.set(Calendar.MILLISECOND, 0);

        _time = new TimeSpan(timeFix.getTimeInMillis());

        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button btnOK = (Button) view.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    TimeSpan newTime = TimeSpan.parse(_numPickerHours.getValue() + ":" + _numPickerMinutes.getValue() + ":" + _numPickerSeconds.getValue());
                    TimeSpan trackEdit = new TimeSpan(newTime.getTotalMilliseconds() - _time.getTotalMilliseconds());

                    // Checks if the time edited or not:
                    if (_time.getTotalMilliseconds() == newTime.getTotalMilliseconds()) {
                        Snackbar.make(getActivity().findViewById(R.id.coordinator), R.string.dialog_time_span_picker_time_not_edited, Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (_positiveResult != null)
                            _positiveResult.onPositiveResult(trackEdit); // Fires the event "onPositiveResult".
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    dismiss();
                }
            }
        });

        TextView txtProjectName = (TextView) view.findViewById(R.id.txtProjectName);
        txtProjectName.setText(_projectName);

        _numPickerHours = (NumberPicker) view.findViewById(R.id.numPickerHours);
        _numPickerHours.setMinValue(0);
        if (_time.getTotalHours() < 10000)
            _numPickerHours.setMaxValue(10000);
        else if (_time.getTotalHours() < 100000)
            _numPickerHours.setMaxValue(100000);
        else
            _numPickerHours.setMaxValue(1000000);

        _numPickerHours.setValue((int) _time.getTotalHours());

        _numPickerMinutes = (NumberPicker) view.findViewById(R.id.numPickerMinutes);
        _numPickerMinutes.setMinValue(0);
        _numPickerMinutes.setMaxValue(59);
        _numPickerMinutes.setValue((int) _time.getMinutes());

        _numPickerSeconds = (NumberPicker) view.findViewById(R.id.numPickerSeconds);
        _numPickerSeconds.setMinValue(0);
        _numPickerSeconds.setMaxValue(59);
        _numPickerSeconds.setValue((int) _time.getSeconds());

    }

    //endregion

    //region Inner Classes

    /**
     * Represents a callback for a positive result from the TimeSpanPickerDialog.
     */
    public interface PositiveResultListener {

        /**
         * Event occurs when a positive button in the dialog pressed.
         *
         * @param time The time set from the dialog.
         */
        void onPositiveResult(TimeSpan time);

    }

    //endregion

}
