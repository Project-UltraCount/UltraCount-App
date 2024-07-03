package com.hci.ireye.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hci.ireye.R;
import com.hci.ireye.data.AutoRefresh;
import com.hci.ireye.data.aliyun.DataGetterOSS;
import com.hci.ireye.data.NotificationMaker;
import com.hci.ireye.data.type.ArduinoEntry;
import com.hci.ireye.ui.adapter.ViewPagerAdapter;
import com.hci.ireye.ui.fragment.ChartsFragment;
import com.hci.ireye.ui.fragment.DevicesFragment;
import com.hci.ireye.ui.fragment.SettingsFragment;
import com.hci.ireye.ui.fragment.StatsFragment;
import com.hci.ireye.util.ApacheFtpUtil;
import com.hci.ireye.util.ThreadUtil;
import com.hci.ireye.util.ToastUtil;
import com.hci.ireye.widget.MyCustomDialog;

import java.io.IOException;
import java.text.DateFormat;
import java.time.Duration;
import java.util.List;
import java.util.SortedMap;

public class MainInterfaceActivity extends AppCompatActivity implements StatsFragment.IOnUpdateStats{

    private AppBarLayout mAblBar;
    private TextView mTvCount, mTvDesription, mTvTime;
    private Button mBtnStartCircle;

    private BottomNavigationView mBnvMenu;

    private ViewPager mVpHolder;
    private ViewPagerAdapter mVpAdapter;
    private StatsFragment statsFragment;
    private ChartsFragment chartsFragment;
    private DevicesFragment devicesFragment;
    private SettingsFragment settingsFragment;

    private SwipeRefreshLayout mSrlRefresh;

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener;

    public NotificationMaker mNotificationMaker;

    private SharedPreferences mStatusPrefs, mUserPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);

        mAblBar = findViewById(R.id.abl_main);
        mTvCount = findViewById(R.id.tv_main_count);
        mTvDesription = findViewById(R.id.tv_main_description);
        mBtnStartCircle = findViewById(R.id.btn_main_start_circle);
        mTvTime = findViewById(R.id.tv_main_time);
        mBnvMenu = findViewById(R.id.bnv_main_menu);
        mVpHolder = findViewById(R.id.vp_main_holder);
        mSrlRefresh = findViewById(R.id.srl_main_refresh);

        mStatusPrefs = getSharedPreferences(getString(R.string.status_prefs), MODE_PRIVATE);
        mUserPrefs = getSharedPreferences(getString(R.string.user_prefs), MODE_PRIVATE);

        // custom animation of collapsing effects
        mAblBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                float startRatio = 0.4f;
                float animationRatio = (1 / (1 - startRatio)) * Math.abs((float)verticalOffset / appBarLayout.getTotalScrollRange()) - (startRatio / (1 - startRatio));
                if (animationRatio < 0) animationRatio = 0;

                mTvDesription.setAlpha(1 - animationRatio);
                mTvTime.setAlpha(1 - animationRatio);
                mBtnStartCircle.setAlpha(1 - animationRatio);
                mTvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50 - 20 * animationRatio);

                //we do not animate color - it is a bit difficult
                if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) { //collapsed
                    mTvCount.setTextColor(getResources().getColor(R.color.colorWhite));
                } else { //expanding/expanded
                    mTvCount.setTextColor(getResources().getColor(R.color.colorMyTheme));
                }
            }
        });

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
                mBnvMenu.setSelectedItemId(mBnvMenu.getMenu().getItem(position).getItemId());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //disable pop up tooltip on long click
        for (int i = 0; i < mBnvMenu.getMenu().size(); ++i) {
            findViewById(mBnvMenu.getMenu().getItem(i).getItemId()).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
        }

        mBnvMenu.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                refresh();
            }
        });

        mBnvMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                TooltipCompat.setTooltipText(findViewById(item.getItemId()), null);
                switch (item.getItemId()) {
                    case R.id.i_menu_stats:
                        mVpHolder.setCurrentItem(0, true);
                        break;
                    case R.id.i_menu_charts:
                        mVpHolder.setCurrentItem(1, true);
                        break;
                    case R.id.i_menu_devices:
                        mVpHolder.setCurrentItem(2, true);
                        break;
                    case R.id.i_menu_settings:
                        mVpHolder.setCurrentItem(3, true);
                }
                //must return true, or the icon will not be highlighted.
                return true;
            }
        });

        //set up notification channel
        mNotificationMaker = new NotificationMaker(this);

        mSrlRefresh.setColorSchemeColors(getResources().getColor(R.color.colorMyTheme));
        mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mBtnStartCircle.setClickable(false);
                update();

                mStatusPrefs.edit()
                        .putLong(getString(R.string.sp_last_refresh), System.currentTimeMillis())
                        .apply();
            }
        };
        mSrlRefresh.setOnRefreshListener(mOnRefreshListener);

        setupViewPager();

        //resume
        if (mStatusPrefs.getBoolean(getString(R.string.sp_ongoing), false)) {
            resume();
        } else {
            paused();
        }

        //press to start/finish counting activity
        mBtnStartCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity ongoing
                if (mStatusPrefs.getBoolean(getString(R.string.sp_ongoing), false)) {
                    MyCustomDialog dialog = new MyCustomDialog(MainInterfaceActivity.this);
                    dialog.setTitle(getString(R.string.alert))
                            .setMsg(getString(R.string.stop_counting_msg))
                            .setCancel(getString(R.string.cancel), null)
                            .setConfirm(getString(R.string.confirm), new MyCustomDialog.IOnConfirmListener() {
                                @Override
                                public void onConfirm(MyCustomDialog dialog) {
                                    stop();
                                }
                            }).show();
                } else {//activity not started
                    start();
                }
            }
        });

        //auto-refresh, added by lithops on 2020/7/17
        new AutoRefresh(this).run();

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode ==KeyEvent.KEYCODE_BACK) {
            MyCustomDialog dialog = new MyCustomDialog(MainInterfaceActivity.this);
            dialog.setTitle(getString(R.string.alert))
                    .setMsg(getString(R.string.log_off_msg))
                    .setCancel(getString(R.string.cancel), null)
                    .setConfirm(getString(R.string.confirm), new MyCustomDialog.IOnConfirmListener() {
                        @Override
                        public void onConfirm(MyCustomDialog dialog) {
                            finish();
                        }
                    }).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mVpAdapter.removeAllFragments();
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ApacheFtpUtil.ftpsClient.logout();
                    ApacheFtpUtil.ftpsClient.disconnect();
                    ToastUtil.makeToast(MainInterfaceActivity.this, getString(R.string.disconnected_prompt));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //stop counting
    void stop() {
        mStatusPrefs.edit().putBoolean(getString(R.string.sp_ongoing), false).apply();
        mStatusPrefs.edit().putLong(getString(R.string.sp_start_time), 0).apply();
        mTvDesription.setVisibility(View.INVISIBLE);
        mTvTime.setVisibility(View.INVISIBLE);
        mTvCount.setText(getString(R.string.start));

        statsFragment.stop();
        chartsFragment.stop();
        devicesFragment.stop();
        getSupportFragmentManager().beginTransaction().hide(statsFragment).hide(chartsFragment).commitAllowingStateLoss();
    }

    //start counting
    void start() {
        mStatusPrefs.edit().putBoolean(getString(R.string.sp_ongoing), true).apply();
        mTvDesription.setVisibility(View.VISIBLE);
        mTvTime.setVisibility(View.VISIBLE);
        mTvCount.setText("0");
        mTvTime.setText("0:00:00:00");
        setupTimer();

        mStatusPrefs.edit().putLong(getString(R.string.sp_start_time), System.currentTimeMillis()).apply();


        //todo change: 此处应initialise Fragments
        statsFragment.init();
        chartsFragment.init();
        devicesFragment.init();
        getSupportFragmentManager().beginTransaction().show(statsFragment).show(chartsFragment).commitAllowingStateLoss();

        //automatically refresh(call update()) when starting
        refresh();
    }

    //called when app opened and counting not started
    void paused() {
        mTvDesription.setVisibility(View.INVISIBLE);
        mTvTime.setVisibility(View.INVISIBLE);
        mTvCount.setText(getString(R.string.start));
        getSupportFragmentManager().beginTransaction().hide(statsFragment).hide(chartsFragment).commitAllowingStateLoss();

    }

    //called when app opened and counting is ongoing
    void resume() {
        mTvDesription.setVisibility(View.VISIBLE);
        mTvTime.setVisibility(View.VISIBLE);
        mTvCount.setText("0");
        setupTimer();

        //automatically refresh(call update()) when starting
        refresh();
    }

    //update using downloaded data
    void update() {
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                final SortedMap<String, List<ArduinoEntry>> newData = DataGetterOSS.getDataFromOSS(MainInterfaceActivity.this);
                ThreadUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        int currentPresent = statsFragment.update(newData);
                        devicesFragment.update(newData);
                        chartsFragment.update(newData);
                        mSrlRefresh.setRefreshing(false);
                        mBtnStartCircle.setClickable(true);

                        if (mUserPrefs.getBoolean(getString(R.string.sp_notification), true)) {
                            mNotificationMaker.makeCurrentPresent(currentPresent);
                        }
                    }
                });
            }
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
                long startTime = mStatusPrefs.getLong(getString(R.string.sp_start_time), 0);
                long last = 0;
                while(!timerThread.isInterrupted() && mStatusPrefs.getBoolean(getString(R.string.sp_ongoing), false)) {
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

    void setupViewPager() {
        mVpAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mVpHolder.setOffscreenPageLimit(3);
        statsFragment = new StatsFragment();
        chartsFragment = new ChartsFragment();
        devicesFragment = new DevicesFragment();
        settingsFragment = new SettingsFragment();
        mVpAdapter.addFragment(statsFragment);
        mVpAdapter.addFragment(chartsFragment);
        mVpAdapter.addFragment(devicesFragment);
        mVpAdapter.addFragment(settingsFragment);

        mVpHolder.setAdapter(mVpAdapter);
    }

//    void informStopToFrgments() {
//        mVpAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//
//        mVpHolder.setAdapter(mVpAdapter);
//    }

    public void refresh() {
        if (!mSrlRefresh.isRefreshing()) {
            mSrlRefresh.setRefreshing(true);
            mOnRefreshListener.onRefresh();
        }
    }

    @Override
    public void onUpdate(int newPresent) {
        mTvCount.setText(newPresent + "");
    }
}
