package com.hci.ireye.ui.eventlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hci.ireye.R;
import com.hci.ireye.data.aliyun.EventsManager;
import com.hci.ireye.data.util.ThreadUtil;
import com.hci.ireye.ui.eventviewer.EventViewerFragment;
import com.hci.ireye.ui.eventviewer.ViewPagerAdapter;
import com.hci.ireye.ui.newevent.NewEventActivity;

import java.util.ArrayList;


public class EventListFragment extends Fragment {

    Context mContext;


    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    private FloatingActionButton mFabNewEvent;

    private SwipeRefreshLayout mSrlRefresh;

    private RadioGroup eventListSelector;
    private EventListContainerFragment ongoingEventListContainerFragment, finishedEventListContainerFragment;


    public EventListFragment() {
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
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.vp_event_list);
        eventListSelector = view.findViewById(R.id.rg_event_list_selector);

        mFabNewEvent = view.findViewById(R.id.fab_event_list_new_event);

        mSrlRefresh = view.findViewById(R.id.srl_event_list_refresh);


        mFabNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NewEventActivity.class);
                startActivity(intent);


            }
        });

        eventListSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_event_list_selector_ongoing:
                        viewPager.setCurrentItem(0, true);
                        break;
                    case R.id.rb_event_list_selector_finished:
                        viewPager.setCurrentItem(1, true);
                }
            }
        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // disable swipe refresh when viewpager scrolled for c onflict resolution
                mSrlRefresh.setEnabled(positionOffsetPixels == 0);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        eventListSelector.check(R.id.rb_event_list_selector_ongoing);
                        break;
                    case 1:
                        eventListSelector.check(R.id.rb_event_list_selector_finished);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPagerAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        ongoingEventListContainerFragment = new EventListContainerFragment();
        finishedEventListContainerFragment = new EventListContainerFragment();


        viewPager.setAdapter(viewPagerAdapter);
        viewPagerAdapter.addFragment("ongoingEventList", ongoingEventListContainerFragment);

        viewPagerAdapter.addFragment("finishedEventList", finishedEventListContainerFragment);


        mSrlRefresh.setOnRefreshListener(this::update);

        mSrlRefresh.setRefreshing(true);
        update();
    }

    private void update() {

        ThreadUtil.runOnThread(() -> {
            ArrayList<EventsManager.CountingEvent> events = EventsManager.getInstance().getAllEvents(getContext());

            for (EventsManager.CountingEvent event : events) {
                getActivity().runOnUiThread(() -> {
                    EventsList el = event.isOngoing() ?
                            ongoingEventListContainerFragment.mEventList :
                            finishedEventListContainerFragment.mEventList;

                    el.addEntry(event.getEventId(), event.getEventName(), event.getStartTime(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventViewerFragment eventViewerFragment = EventViewerFragment.newInstance(event);

                            getParentFragment().getFragmentManager().beginTransaction().replace(R.id.fl_event_root, eventViewerFragment).addToBackStack(null).commit();
                        }
                    });
                });
            }
            mSrlRefresh.setRefreshing(false);
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}