package com.hci.ireye.ui.eventviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hci.ireye.R;
import com.hci.ireye.data.AutoRefresher;
import com.hci.ireye.data.NotificationMaker;
import com.hci.ireye.data.aliyun.EventsManager;
import com.hci.ireye.data.util.ThreadUtil;
import com.hci.ireye.ui.customview.MyCustomDialog;
import com.hci.ireye.ui.eventviewer.eventcharts.EventChartsFragment;
import com.hci.ireye.ui.eventviewer.eventstats.EventStatsFragment;
import com.hci.ireye.ui.util.MyUtil;

import java.text.DateFormat;
import java.time.Duration;

public class EventViewerFragment extends Fragment {


    private AppBarLayout mAblBar;
    private TextView mTvCount, mTvDesription, mTvTime;
    private Button mBtnStartCircle;

    private LinearLayout mOngoingLabel;

    private ViewPager mVpHolder;
    private ViewPagerAdapter mVpAdapter;
    private EventStatsFragment mStatsFragment;
    private EventChartsFragment mChartsFragment;

    private RadioGroup mRgSelector;

    private SwipeRefreshLayout mSrlRefresh;

    private FloatingActionButton mFabStopEvent;

    public NotificationMaker mNotificationMaker;

    private AutoRefresher autoRefresher;

    private SharedPreferences mStatusPrefs, mUserPrefs;

    private EventsManager.CountingEvent mEvent; // the event whose data is to be displayed


    public static EventViewerFragment newInstance(EventsManager.CountingEvent event) {
        EventViewerFragment fragment = new EventViewerFragment();
        Bundle bundle = new Bundle();

        bundle.putSerializable("event", event);

        fragment.setArguments(bundle);
        return fragment;
    }

    public EventViewerFragment() {
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
        return inflater.inflate(R.layout.fragment_event_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAblBar = view.findViewById(R.id.abl_main);
        mTvCount = view.findViewById(R.id.tv_main_count);
        mTvDesription = view.findViewById(R.id.tv_main_description);
        mBtnStartCircle = view.findViewById(R.id.btn_main_start_circle);
        mOngoingLabel = view.findViewById(R.id.ll_ongoing_label);
        mTvTime = view.findViewById(R.id.tv_main_time);
        mVpHolder = view.findViewById(R.id.vp_main_holder);
        mSrlRefresh = view.findViewById(R.id.srl_main_refresh);
        mRgSelector = view.findViewById(R.id.rg_event_viewer_selector);
        mFabStopEvent = view.findViewById(R.id.fab_event_viewer_halt);

        mStatusPrefs = getActivity().getSharedPreferences(getString(R.string.status_prefs), Context.MODE_PRIVATE);
        mUserPrefs = getActivity().getSharedPreferences(getString(R.string.user_prefs), Context.MODE_PRIVATE);

        mEvent = (EventsManager.CountingEvent) getArguments().getSerializable("event");


        // display event name
        mTvCount.setText(mEvent.getEventName());

        // custom animation of collapsing effects
        mAblBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                float startRatio = 0.4f;
                float animationRatio = (1 / (1 - startRatio)) * Math.abs((float) verticalOffset / appBarLayout.getTotalScrollRange()) - (startRatio / (1 - startRatio));
                if (animationRatio < 0) animationRatio = 0;

                mTvDesription.setAlpha(1 - animationRatio);
                mTvTime.setAlpha(1 - animationRatio);
                mBtnStartCircle.setAlpha(1 - animationRatio);
//                mTvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20 - 5 * animationRatio);

                //we do not animate color - it is a bit difficult
                if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) { //collapsed
                    mTvCount.setTextColor(getResources().getColor(R.color.colorWhite));
                } else { //expanding/expanded
                    mTvCount.setTextColor(getResources().getColor(R.color.colorMyTheme));
                }
            }
        });

        // start ongoing dot animation
        ImageView dot = mOngoingLabel.findViewById(R.id.iv_ongoing_dot);
        AnimationDrawable animationDrawable = (AnimationDrawable) dot.getBackground();
        animationDrawable.start();

        if (!mEvent.isOngoing()) {
            mOngoingLabel.setVisibility(View.INVISIBLE);
            mFabStopEvent.setVisibility(View.INVISIBLE);
        } else {
            mOngoingLabel.setVisibility(View.VISIBLE);
            mFabStopEvent.setVisibility(View.VISIBLE);
        }

        //---------------------------

        mVpHolder.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //disable refresh when navigating between pages.
                if (!mSrlRefresh.isRefreshing()) {
                    if (positionOffsetPixels != 0) mSrlRefresh.setEnabled(false);
                    else mSrlRefresh.setEnabled(true);
                }
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mRgSelector.check(R.id.rb_event_viewer_selector_stats);
                        break;
                    case 1:
                        mRgSelector.check(R.id.rb_event_viewer_selector_charts);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mRgSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_event_viewer_selector_stats:
                        mVpHolder.setCurrentItem(0, true);
                        break;
                    case R.id.rb_event_viewer_selector_charts:
                        mVpHolder.setCurrentItem(1, true);
                        break;
                }
            }
        });
        //set up notification channel
        mNotificationMaker = new NotificationMaker(getContext());

        mSrlRefresh.setColorSchemeColors(getResources().getColor(R.color.colorMyTheme));


//        mSrlRefresh.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
//            @Override
//            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
//                return false;
//            }
//        });
        mSrlRefresh.setOnRefreshListener(this::update);

        mSrlRefresh.setRefreshing(true);
        update();

        setupViewPager();

        setupTimer();

        getActivity().getSupportFragmentManager().beginTransaction().show(mStatsFragment).show(mChartsFragment).commitAllowingStateLoss();

        mFabStopEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyCustomDialog(getContext())
                        .setTitle(getString(R.string.alert))
                        .setMsg(getString(R.string.alert_stop_event))
                        .setCancel(getString(R.string.cancel), null)
                        .setConfirm(getString(R.string.confirm), new MyCustomDialog.IOnConfirmListener() {
                            @Override
                            public void onConfirm(MyCustomDialog dialog) {
                                ThreadUtil.runOnThread(() -> {
                                    mEvent.stop(getContext());
                                    ThreadUtil.runOnUIThread(() -> mSrlRefresh.setRefreshing(true));
                                });
                            }
                        }).show();

            }
        });
    }

    void update() {
        ThreadUtil.runOnThread(() -> {
            final EventsManager.CountingDataSet newData = mEvent.getCountingDataSet(getContext());
            ThreadUtil.runOnUIThread(() -> {
                mTvDesription.setText(newData.getTotalNetflow() + "");
                mStatsFragment.update(newData);
                mChartsFragment.update(newData);
                mSrlRefresh.setRefreshing(false);

                if (mEvent.isOngoing()) {
                    mOngoingLabel.setVisibility(View.VISIBLE);
                    mFabStopEvent.setVisibility(View.VISIBLE);
                } else {
                    mOngoingLabel.setVisibility(View.INVISIBLE);
                    mFabStopEvent.setVisibility(View.INVISIBLE);

                }

                if (mUserPrefs.getBoolean(getString(R.string.sp_notification), true)) {
                    mNotificationMaker.makeCurrentPresent(newData.getTotalNetflow());
                }
                mStatusPrefs.edit()
                        .putLong(getString(R.string.sp_last_refresh), System.currentTimeMillis() / 1000)
                        .apply();

            });
        });
    }


    private Thread timerThread;

    public void setupTimer() {
        if (!(timerThread == null) && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        timerThread = ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                long startTime = mEvent.getStartTime() * 1000L;
                long last = 0;
                while (!timerThread.isInterrupted()) {
                    final long passed = System.currentTimeMillis() - startTime;
                    if (passed >= last + 1000) {
                        ThreadUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                DateFormat format = DateFormat.getTimeInstance();
                                Duration duration = Duration.ofMillis(passed);
                                mTvTime.setText(String.format("%d:%02d:%02d:%02d", passed / 1000 / 86400, passed / 1000 % 86400 / 3600, passed / 1000 % 3600 / 60, passed / 1000 % 60));
                            }
                        });
                        last = passed;
                    }
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        timerThread.interrupt();
//        autoRefresher.stop();

    }


    void setupViewPager() {
        mVpAdapter = new ViewPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mVpHolder.setOffscreenPageLimit(3);
        mStatsFragment = EventStatsFragment.newInstance(mEvent);
        mChartsFragment = EventChartsFragment.newInstance(mEvent);
        mVpAdapter.addFragment("EventViewerStats", mStatsFragment);
        mVpAdapter.addFragment("EventViewerCharts", mChartsFragment);

        mVpHolder.setAdapter(mVpAdapter);
    }


//    public void refresh() {
//        if (!mSrlRefresh.isRefreshing()) {
//            mSrlRefresh.setRefreshing(true);
//            mOnRefreshListener.onRefresh();
//        }
//    }

}