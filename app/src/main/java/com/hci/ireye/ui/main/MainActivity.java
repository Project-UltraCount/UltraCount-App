package com.hci.ireye.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hci.ireye.R;
import com.hci.ireye.ui.deviceviewer.DeviceViewerFragment;
import com.hci.ireye.ui.eventlist.EventRootFragment;
import com.hci.ireye.ui.eventviewer.ViewPagerAdapter;
import com.hci.ireye.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBnvMenu;
    private ViewPager mVpHolder;
    private ViewPagerAdapter mVpAdapter;
    private DeviceViewerFragment deviceViewerFragment;
    private EventRootFragment eventRootFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBnvMenu = findViewById(R.id.bnv_main_menu);
        mVpHolder = findViewById(R.id.vp_main_holder);


        //disable pop up tooltip on long click
        for (int i = 0; i < mBnvMenu.getMenu().size(); ++i) {
            findViewById(mBnvMenu.getMenu().getItem(i).getItemId()).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
        }


        mBnvMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                TooltipCompat.setTooltipText(findViewById(item.getItemId()), null);
                switch (item.getItemId()) {
                    case R.id.i_menu_events:
                        mVpHolder.setCurrentItem(0, true);
                        break;
                    case R.id.i_menu_devices:
                        mVpHolder.setCurrentItem(1, true);
                        break;
                    case R.id.i_menu_settings:
                        mVpHolder.setCurrentItem(2, true);
                }
                //must return true, or the icon will not be highlighted.
                return true;
            }
        });

        mVpHolder.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mBnvMenu.setSelectedItemId(R.id.i_menu_events);
                        break;
                    case 1:
                        mBnvMenu.setSelectedItemId(R.id.i_menu_devices);
                        break;
                    case 2:
                        mBnvMenu.setSelectedItemId(R.id.i_menu_settings);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mVpAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mVpHolder.setOffscreenPageLimit(3);

        eventRootFragment = new EventRootFragment();
        deviceViewerFragment = new DeviceViewerFragment();
        settingsFragment = new SettingsFragment();

        mVpAdapter.addFragment("event", eventRootFragment);
        mVpAdapter.addFragment("device", deviceViewerFragment);
        mVpAdapter.addFragment("settings", settingsFragment);
        mVpHolder.setAdapter(mVpAdapter);
    }
}