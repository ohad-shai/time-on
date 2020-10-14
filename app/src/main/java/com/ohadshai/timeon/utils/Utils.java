package com.ohadshai.timeon.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ohadshai.timeon.R;

import java.util.Calendar;

/**
 * Represents utilities and general helpers.
 * Created by Ohad on 11/21/2016.
 */
public class Utils {

    /**
     * Represents utilities and helpers for the UI.
     */
    public static final class UI {

        /**
         * Gets the height of the status bar.
         *
         * @param context The context owner.
         * @return Returns the height of the status bar in PX.
         */
        public static int getStatusBarHeight(@NonNull Context context) {
            int result = 0;
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0)
                result = context.getResources().getDimensionPixelSize(resourceId);
            return result;
        }

        /**
         * Gets the height of the action bar.
         *
         * @param context The context owner.
         * @return Returns the height of the action bar in PX.
         */
        public static int getActionBarHeight(@NonNull Context context) {
            int actionBarHeight = 0;
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            return actionBarHeight;
        }

        /**
         * Shows an information {@link android.widget.Toast} with the text provided, to the {@link View} provided.
         *
         * @param view  The view to show the information toast.
         * @param resId The string resource id that holds the text to show in the information toast.
         */
        public static void showInformationToast(@NonNull View view, @StringRes int resId) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);

            Toast toast = Toast.makeText(view.getContext(), resId, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.START, (location[0] - (view.getWidth()) / 2), (location[1] + (view.getHeight() / 2)));
            toast.show();
        }

        /**
         * Appends a colored text to a {@link TextView}.
         *
         * @param tv    The {@link TextView} to append the colored text.
         * @param text  The text to append.
         * @param color The color of the text.
         */
        public static void appendColoredTextToTextView(@NonNull TextView tv, String text, int color) {
            int start = tv.getText().length();
            tv.append(text);
            int end = tv.getText().length();

            Spannable spannableText = (Spannable) tv.getText();
            spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
        }

    }

    /**
     * Represents utilities and helpers for colors.
     */
    public static final class Colors {

        /**
         * Holds a constant for the "Primary Color" integer value.
         */
        public static final int PRIMARY_COLOR = Color.rgb(238, 143, 80);

    }

    /**
     * Represents utilities and helpers for dates.
     */
    public static final class Dates {

        /**
         * Clears the time in the date (returns a new reference).
         *
         * @param date The {@link Calendar} date to clear the time.
         * @return Returns a new {@link Calendar} date, with the time cleared.
         */
        public static Calendar clearTime(Calendar date) {
            Calendar newDate = Calendar.getInstance();
            newDate.setTimeInMillis(date.getTimeInMillis());

            newDate.set(Calendar.HOUR_OF_DAY, 0);
            newDate.set(Calendar.MINUTE, 0);
            newDate.set(Calendar.SECOND, 0);
            newDate.set(Calendar.MILLISECOND, 0);

            return newDate;
        }

        /**
         * Returns a {@link String} represents a date in the correct format to display.
         *
         * @param date    The {@link Calendar} date to display.
         * @param context The context owner.
         * @return Returns a {@link String} represents a date in the correct format to display.
         */
        @SuppressLint("DefaultLocale")
        public static String display(Calendar date, @NonNull Context context) {
            Calendar today = clearTime(Calendar.getInstance());
            Calendar dateCleared = clearTime(date);

            if (today.getTimeInMillis() == dateCleared.getTimeInMillis())
                return context.getString(R.string.general_today);

            today.add(Calendar.DAY_OF_MONTH, -1); // Subtracts 1 day.

            if (today.getTimeInMillis() == dateCleared.getTimeInMillis())
                return context.getString(R.string.general_yesterday);

            return String.format("%1$tb %1$td, %1$tY", date);
        }

    }

}
