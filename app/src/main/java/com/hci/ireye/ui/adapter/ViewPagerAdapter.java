package com.hci.ireye.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


//
// Created by Lithops on 2020/6/4, 14:57.
//

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList = new ArrayList<>();
    private FragmentManager mFragmentManager;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        mFragmentManager = fm;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    public void removeAllFragments() {
        for (Fragment fragment : mFragmentList) {
            mFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }


}
