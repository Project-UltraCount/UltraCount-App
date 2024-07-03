package com.hci.ireye.util;

//
// Created by Lithops on 2020/6/25, 20:14.
//

import java.util.Date;
import java.time.Duration;

public class MyUtil {
    static public long truncateTimeToMin(long timeInMillis) {
        //return timeInMillis;
        return timeInMillis / 1000 * 1000 - new Date(timeInMillis).getSeconds() * 1000;
    }

    static public long getStartOfInterval(long current, long start, long interval) {
        return (current - start) / interval * interval + start;
    }

    static public float compressTimeInMillis(long time, long offset) {
        return (float)((time - offset) / 1000);
    }

    static public long extractTimeInMillis(float compressedTime, long offset) {
        return (long)compressedTime * 1000 + offset;
    }


    static public long getValueByRatio(long aLower,long aUpper, long a, long bLower, long bUpper) {
        return (a - aLower) * (bUpper - bLower) / (aUpper - aLower) + bLower;
    }

    static public double getPercentage(double aLower, double aUpper, double a) {
        return (a - aLower) / (aUpper - aLower);
    }

    static public String formatDurationString(Duration duration) {
        return duration.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").replaceAll("(M)", "min").toLowerCase();
    }

}
