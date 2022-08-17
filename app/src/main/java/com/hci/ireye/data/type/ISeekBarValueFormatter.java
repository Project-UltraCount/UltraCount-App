package com.hci.ireye.data.type;

//
// Created by Lithops on 2020/7/19, 9:32.
//

public interface ISeekBarValueFormatter {
    String format(int progress, int minProgress, int maxProgress);
}
