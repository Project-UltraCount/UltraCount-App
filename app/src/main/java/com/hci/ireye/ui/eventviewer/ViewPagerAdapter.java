package com.hci.ireye.ui.eventviewer;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hci.ireye.ui.util.MyUtil;

import java.util.LinkedHashMap;

//
// Created by Lithops on 2020/6/4, 14:57.
//

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private LinkedHashMap<String, Fragment> mFragments = new LinkedHashMap<>();
    private FragmentManager mFragmentManager;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        mFragmentManager = fm;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return MyUtil.getValue(mFragments, position);
    }

    // workaround to make sure notifyDaraSetChanged() works
    // https://stackoverflow.com/questions/7263291/why-pageradapternotifydatasetchanged-is-not-updating-the-view
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public void addFragment(String fragmentId, Fragment fragment) {
        mFragments.put(fragmentId, fragment);
        notifyDataSetChanged();
    }

    public void removeFragment(String fragmentId) {
        mFragments.remove(fragmentId);
        notifyDataSetChanged();
    }

    public boolean hasFragment(String fragmentId) {
        return mFragments.containsKey(fragmentId);
    }

    public Fragment getFragment(String fragmentId) {
        return mFragments.get(fragmentId);
    }

    public void clearFragments() {
        mFragments.clear();
        notifyDataSetChanged();
    }
}
