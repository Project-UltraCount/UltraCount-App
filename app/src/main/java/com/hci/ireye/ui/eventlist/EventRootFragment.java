package com.hci.ireye.ui.eventlist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.hci.ireye.R;

public class EventRootFragment extends Fragment {


    private EventListFragment mEventListFragment;
    private FrameLayout mFrame;

    public EventRootFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_root, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFrame = view.findViewById(R.id.fl_event_root);

        mEventListFragment = new EventListFragment();

        getChildFragmentManager().beginTransaction().add(R.id.fl_event_root, mEventListFragment).addToBackStack(null).commit();
    }
}