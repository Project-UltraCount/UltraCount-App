package com.hci.ireye.ui.util;

//
// Created by Lithops on 2020/6/25, 20:14.
//

import android.content.Context;
import android.util.TypedValue;

import java.util.Date;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MyUtil {
//    static public long truncateTimeToMin(long timeInSeconds) {
//        return timeInSeconds;
////        return timeInSeconds - new Date(timeInSeconds).getSeconds();
//    }

    static public long getStartOfInterval(long current, long start, long interval) {
        return (current - start) / interval * interval + start;
    }

    static public float compressTimeInSeconds(long time, long offsetInSeconds) {
        return (float)(time - offsetInSeconds);
    }

    static public long extractTimeInSeconds(float compressedTime, long offsetInSeconds) {
        return (long)compressedTime + offsetInSeconds;
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

    static public <K, V> K getKey(SortedMap<K, V> map, int index) {
        K[] keys = (K[])map.keySet().toArray();
        if (index < 0) index += map.size();
        return keys[index];
    }

    static public <K, V> V getValue(Map<K, V> map, int index) {
        V[] values = (V[])map.values().toArray();
        if (index < 0) index += map.size();
        if (index >= map.size()) return null;
        return values[index];
    }


    static public int dpToPx(int dp, Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp, // dp
                context.getResources().getDisplayMetrics()
        );
    }
    public static void main(String[] args) {
        TreeMap<String, Integer> map = new TreeMap<>();

        map.put("rr", 3);
        map.put("rx", 30);
        map.put("rjhw", 31);

        System.out.println(getValue(map, 2));
    }
}
