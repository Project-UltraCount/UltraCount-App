package com.hci.ireye.data.type;

//
// Created by Lithops on 2020/6/6, 23:33.
//

import androidx.annotation.NonNull;

public class ArduinoEntry implements Comparable<ArduinoEntry> {
    public long time;
    public int count;

    @Override
    public int compareTo(@NonNull ArduinoEntry o) {
        return time < o.time ? -1 : 1;
    }

    public ArduinoEntry(ArduinoEntry that) {
        time = that.time;
        count = that.count;
    }

    public ArduinoEntry(long time, int count) {
        this.time = time;
        this.count = count;
    }
}
