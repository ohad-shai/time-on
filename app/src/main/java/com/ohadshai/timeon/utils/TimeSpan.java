package com.ohadshai.timeon.utils;

import android.nfc.FormatException;

import java.util.Calendar;

/**
 * Represents a time span.
 */
public class TimeSpan implements Comparable<TimeSpan>, java.io.Serializable, Cloneable {

    //region Public Constants

    /**
     * Constant for milliseconds unit and conversion.
     */
    public static final int MILLISECONDS = 1;

    /**
     * Constant for seconds unit and conversion.
     */
    public static final int SECONDS = MILLISECONDS * 1000;

    /**
     * Constant for minutes unit and conversion.
     */
    public static final int MINUTES = SECONDS * 60;

    /**
     * Constant for hours unit and conversion.
     */
    public static final int HOURS = MINUTES * 60;

    /**
     * Constant for days unit and conversion.
     */
    public static final int DAYS = HOURS * 24;

    /**
     * Represents the Maximum TimeSpan value.
     */
    public static final TimeSpan MAX_VALUE = new TimeSpan(Long.MAX_VALUE);

    /**
     * Represents the Minimum TimeSpan value.
     */
    public static final TimeSpan MIN_VALUE = new TimeSpan(Long.MIN_VALUE);

    /**
     * Represents the TimeSpan with a value of zero.
     */
    public static final TimeSpan ZERO = new TimeSpan(0L);

    //endregion

    //region Private Members

    /**
     * Constant serialized ID used for compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The time.
     */
    private long time = 0;

    //endregion

    //region C'tors

    /**
     * Initializes a new instance of TimeSpan based on the number of milliseconds entered.
     *
     * @param time The number of milliseconds for this TimeSpan.
     */
    public TimeSpan(long time) {
        this.time = time;
    }

    /**
     * Initializes a new TimeSpan object based on the unit and value entered.
     *
     * @param units The type of unit to use.
     * @param value The number of units to use.
     */
    public TimeSpan(int units, long value) {
        this.time = TimeSpan.toMilliseconds(units, value);
    }

    //endregion

    //region Public Static API

    /**
     * Subtracts two Date objects creating a new TimeSpan object.
     *
     * @param date1 Date to use as the base value.
     * @param date2 Date to subtract from the base value.
     * @return Returns a TimeSpan object representing the difference between the two Date objects.
     */
    public static TimeSpan subtract(Calendar date1, Calendar date2) {
        return new TimeSpan(date1.getTimeInMillis() - date2.getTimeInMillis());
    }

    /**
     * Compares two TimeSpan objects.
     *
     * @param first  The first TimeSpan to use in the compare.
     * @param second The second TimeSpan to use in the compare.
     * @return Returns a negative integer, zero, or a positive integer as the first TimeSpan is less than, equal to, or greater than the second TimeSpan.
     */
    public static int compare(TimeSpan first, TimeSpan second) {
        if (first.time == second.time)
            return 0;
        if (first.time > second.time)
            return +1;

        return -1;
    }

    /**
     * Parses a string to a TimeSpan object.
     *
     * @param s The string to parse to TimeSpan.
     * @return Returns a TimeSpan object parsed from the string.
     * @throws Exception
     */
    public static TimeSpan parse(String s) throws Exception {
        String str = s.trim();
        String[] st1 = str.split("\\.");
        long days = 0, millsec = 0, totMillSec = 0;
        String data = str;
        switch (st1.length) {
            case 1:
                data = str;
                break;
            case 2:
                if (st1[0].split(":").length > 1) {
                    millsec = Long.parseLong(st1[1]);
                    data = st1[0];
                } else {
                    days = Long.parseLong(st1[0]);
                    data = st1[1];
                }
                break;
            case 3:
                days = Long.parseLong(st1[0]);
                data = st1[1];
                millsec = Long.parseLong(st1[2]);
                break;
            default:
                throw new FormatException("Bad Format");

        }
        String[] st = data.split(":");
        switch (st.length) {
            case 1:
                totMillSec = Long.parseLong(str) * 24 * 60 * 60 * 1000;
                break;
            case 2:
                totMillSec = (Long.parseLong(st[0]) * 60 * 60 * 1000) + (Long.parseLong(st[1]) * 60 * 1000);
                break;
            case 3:
                totMillSec = (Long.parseLong(st[0]) * 60 * 60 * 1000) + (Long.parseLong(st[1]) * 60 * 1000) + (
                        Long.parseLong(st[2]) * 1000);
                break;
            case 4:
                totMillSec =
                        (Long.parseLong(st[0]) * 24 * 60 * 60 * 1000) + (Long.parseLong(st[1]) * 60 * 60 * 1000) + (
                                Long.parseLong(st[2]) * 60 * 1000) + (Long.parseLong(st[3]) * 1000);
                break;
            default:
                throw new FormatException("Bad Format/Overflow");
        }
        totMillSec += (days * 24 * 60 * 60 * 1000) + millsec;
        return new TimeSpan(totMillSec);
    }

    //endregion

    //region Public API

    /**
     * Returns a string representation of the object in the format of (hh:mm:ss / 00:00:00).
     *
     * @return Returns the string representation of this TimeSpan object.
     */
    public String toString() {
        // Checks if the time is minus:
        if (getTotalMilliseconds() < 0) {
            long hours = (long) getTotalHours() * -1;
            long minutes = getMinutes() * -1;
            long seconds = getSeconds() * -1;

            return (hours < 10 ? "- 0" + hours : "- " + hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
        } else {
            long hours = (long) getTotalHours();
            long minutes = getMinutes();
            long seconds = getSeconds();

            return (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
        }
    }

    /**
     * Returns a string representation of the object in the format.
     * "[-]d.hh:mm:ss.ff" where "-" is an optional sign for negative TimeSpan
     * values, the "d" component is days, "hh" is hours, "mm" is minutes, "ss"
     * is seconds, and "ff" is milliseconds
     *
     * @return Returns a string containing the number of milliseconds.
     */
    public String display() {
        StringBuffer sb = new StringBuffer();
        long millis = this.time;
        if (millis < 0) {
            sb.append("-");
            millis = -millis;
        }

        long day = millis / TimeSpan.DAYS;

        if (day != 0) {
            sb.append(day);
            sb.append("d.");
            millis = millis % TimeSpan.DAYS;
        }

        sb.append(millis / TimeSpan.HOURS);
        millis = millis % TimeSpan.HOURS;
        sb.append("h:");
        sb.append(millis / TimeSpan.MINUTES);
        millis = millis % TimeSpan.MINUTES;
        sb.append("m:");
        sb.append(millis / TimeSpan.SECONDS);
        sb.append("s");
        millis = millis % TimeSpan.SECONDS;
        if (millis != 0) {
            sb.append(".");
            sb.append(millis);
            sb.append("ms");
        }
        return sb.toString();
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. Comparison is based
     * on the number of milliseconds in this TimeSpan.
     *
     * @param o The Object to be compared.
     * @return Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    public int compareTo(TimeSpan o) {
        TimeSpan compare = (TimeSpan) o;
        if (this.time == compare.time)
            return 0;
        if (this.time > compare.time)
            return +1;

        return -1;
    }

    /**
     * Indicates whether some other object is "equal to" this one. Comparison is based on the number of milliseconds in this TimeSpan.
     *
     * @param obj The reference object with which to compare.
     * @return Returns true if the obj argument is a TimeSpan object with the exact same number of milliseconds, otherwise false.
     */
    public boolean equals(Object obj) {
        if (obj instanceof TimeSpan) {
            TimeSpan compare = (TimeSpan) obj;
            if (this.time == compare.time)
                return true;
        }
        return false;
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>. The method uses the same algorithm as found in the Long class.
     *
     * @return Returns a hash code value for this object.
     * @see Object#equals(Object)
     * @see java.util.Hashtable
     */
    public int hashCode() {
        return Long.valueOf(this.time).hashCode();
    }

    /**
     * Returns a clone of this TimeSpan.
     *
     * @return Returns a clone of this TimeSpan.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Indicates whether the value of the TimeSpan is positive or not.
     *
     * @return Returns true if the value of the TimeSpan is greater than zero, otherwise false.
     */
    public boolean isPositive() {
        return this.compareTo(TimeSpan.ZERO) > 0;
    }

    /**
     * Indicates whether the value of the TimeSpan is negative or not.
     *
     * @return Returns true if the value of the TimeSpan is less than zero, otherwise false.
     */
    public boolean isNegative() {
        return this.compareTo(TimeSpan.ZERO) < 0;
    }

    /**
     * Indicates whether the value of the TimeSpan is zero or not.
     *
     * @return Returns true if the value of the TimeSpan is equal to zero, otherwise false.
     */
    public boolean isZero() {
        return this.equals(TimeSpan.ZERO);
    }

    /**
     * Gets the number of milliseconds.
     *
     * @return Returns the number of milliseconds.
     */
    public long getMilliseconds() {
        return (((this.time % TimeSpan.HOURS) % TimeSpan.MINUTES) % TimeSpan.MILLISECONDS) / TimeSpan.MILLISECONDS;
    }

    /**
     * Gets the total number of milliseconds.
     *
     * @return Returns the total number of milliseconds.
     */
    public long getTotalMilliseconds() {
        return this.time;
    }

    /**
     * Gets the number of seconds (truncated).
     *
     * @return Returns the number of seconds.
     */
    public long getSeconds() {
        return ((this.time % TimeSpan.HOURS) % TimeSpan.MINUTES) / TimeSpan.SECONDS;
    }

    /**
     * Gets the total number of seconds including fractional seconds.
     *
     * @return Returns the total number of seconds.
     */
    public double getTotalSeconds() {
        return this.time / 1000.0d;
    }

    /**
     * Gets the number of minutes (truncated).
     *
     * @return Returns the number of minutes.
     */
    public long getMinutes() {
        return (this.time % TimeSpan.HOURS) / TimeSpan.MINUTES; // (this.time / 1000) / 60;
    }

    /**
     * Gets the total number of minutes (including fractional minutes).
     *
     * @return Returns the total number of minutes.
     */
    public double getTotalMinutes() {
        return (this.time / 1000.0d) / 60.0d;
    }

    /**
     * Gets the number of hours (truncated).
     *
     * @return Returns the number of hours.
     */
    public long getHours() {
        return ((this.time / 1000) / 60) / 60;
    }

    /**
     * Gets the total number of hours (including fractional hours).
     *
     * @return Returns the total number of hours.
     */
    public double getTotalHours() {
        return ((this.time / 1000.0d) / 60.0d) / 60.0d;
    }

    /**
     * Gets the number of days (truncated).
     *
     * @return Returns the number of days.
     */
    public long getDays() {
        return (((this.time / 1000) / 60) / 60) / 24;
    }

    /**
     * Gets the total number of days (including fractional days).
     *
     * @return Returns the total number of days.
     */
    public double getTotalDays() {
        return (((this.time / 1000.0d) / 60.0d) / 60.0d) / 24.0d;
    }

    /**
     * Adds a TimeSpan to this TimeSpan.
     *
     * @param timespan The TimeSpan to add to this TimeSpan.
     * @return Returns this TimeSpan object.
     */
    public TimeSpan add(TimeSpan timespan) {
        return add(TimeSpan.MILLISECONDS, timespan.time);
    }

    /**
     * Adds a number of units to this TimeSpan.
     *
     * @param units The type of unit to add to this TimeSpan.
     * @param value The number of units to add to this TimeSpan.
     * @return Returns this TimeSpan object.
     */
    public TimeSpan add(int units, long value) {
        this.time += TimeSpan.toMilliseconds(units, value);
        return this;
    }

    /**
     * Returns a TimeSpan whose value is the absolute value of this TimeSpan.
     *
     * @return Returns a TimeSpan whose value is the absolute value of this TimeSpan.
     */
    public TimeSpan duration() {
        return new TimeSpan(Math.abs(this.time));
    }

    /**
     * Returns a TimeSpan whose value is the negated value of this TimeSpan.
     *
     * @return Returns a TimeSpan whose value is the negated value of this TimeSpan.
     */
    public TimeSpan negate() {
        return new TimeSpan(-this.time);
    }

    /**
     * Subtracts a TimeSpan from this TimeSpan.
     *
     * @param timespan The TimeSpan to subtract from this TimeSpan.
     */
    public void subtract(TimeSpan timespan) {
        subtract(TimeSpan.MILLISECONDS, timespan.time);
    }

    /**
     * Subtracts a number of units from this TimeSpan.
     *
     * @param units The type of unit to subtract from this TimeSpan.
     * @param value The number of units to subtract from this TimeSpan.
     */
    public void subtract(int units, long value) {
        add(units, -value);
    }

    //endregion

    //region Private Methods

    /**
     * Converts to milliseconds.
     *
     * @param units The units to convert.
     * @param value The value to convert.
     * @return Returns the long value converted.
     */
    private static long toMilliseconds(int units, long value) {
        long millis;
        switch (units) {
            case TimeSpan.MILLISECONDS:
            case TimeSpan.SECONDS:
            case TimeSpan.MINUTES:
            case TimeSpan.HOURS:
            case TimeSpan.DAYS:
                millis = value * units;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized units: " + units);
        }
        return millis;
    }

    //endregion

}