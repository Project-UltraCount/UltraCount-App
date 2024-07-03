package com.hci.ireye.ui.adapter;

//
// Created by Lithops on 2020/7/18, 17:46.
//

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hci.ireye.R;
import com.hci.ireye.data.type.ISeekBarValueFormatter;

public class SettingsPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_SEPARATOR = 0;
    public static final int TYPE_SWITCH = 1;
    public static final int TYPE_SEEK_BAR = 2;

    private Context mContext;
    private IGetTab mIGetTab;

    public SettingsPageAdapter(Context context, IGetTab iGetTab) {
        mContext = context;
        mIGetTab = iGetTab;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SEPARATOR:
                return new SettingsPageSeparatorViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_setings_page_separator, parent, false));
            case TYPE_SWITCH:
                return new SettingsPageSwitchViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_settings_page_switch, parent, false));
            case TYPE_SEEK_BAR:
                return new SettingsPageSeekBarViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_settings_page_seek_bar, parent, false));
            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case TYPE_SEPARATOR:
                SettingsPageSeparatorViewHolder holder1 = (SettingsPageSeparatorViewHolder)holder;
                holder1.title.setText(mIGetTab.getTitle(position));
                break;
            case TYPE_SWITCH:
                SettingsPageSwitchViewHolder holder2 = (SettingsPageSwitchViewHolder)holder;
                holder2.title.setText(mIGetTab.getTitle(position));
                holder2.aSwitch.setOnCheckedChangeListener(mIGetTab.getSwitchListener(position));
                holder2.aSwitch.setChecked(mIGetTab.getSwitchChecked(position));
                holder2.img.setImageDrawable(mIGetTab.getImg(position));
                break;
            case TYPE_SEEK_BAR:
                final SettingsPageSeekBarViewHolder holder3 = (SettingsPageSeekBarViewHolder)holder;
                holder3.title.setText(mIGetTab.getTitle(position));
                holder3.img.setImageDrawable(mIGetTab.getImg(position));
                holder3.lower.setText(mIGetTab.getLowerBound(position));
                holder3.upper.setText(mIGetTab.getUpperBound(position));

                //compound listeners (one default, one given)
                holder3.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mIGetTab.getSeekBarListener(position).onProgressChanged(seekBar, progress, fromUser);
                        holder3.value.setText(mIGetTab.getSeekBarValueFormatter(position).format(progress, seekBar.getMin(), seekBar.getMax()));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mIGetTab.getSeekBarListener(position).onStartTrackingTouch(seekBar);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mIGetTab.getSeekBarListener(position).onStopTrackingTouch(seekBar);
                    }
                });

                holder3.seekBar.setProgress((int) (mIGetTab.getSeekBarInitialPercentage(position) * (holder3.seekBar.getMax() - holder3.seekBar.getMin()) + holder3.seekBar.getMin()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + getItemViewType(position));
        }
    }

    @Override
    public int getItemCount() {
        return mIGetTab.getSize();
    }

    @Override
    public int getItemViewType(int position) {
        return mIGetTab.getType(position);
    }

    class SettingsPageSwitchViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView img;
        private Switch aSwitch;
        public SettingsPageSwitchViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_settings_page_switch_title);
            img = itemView.findViewById(R.id.iv_settings_page_switch_img);
            aSwitch = itemView.findViewById(R.id.s_settings_page_switch_switch);
        }
    }

    class SettingsPageSeekBarViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView title, value, lower, upper;
        private SeekBar seekBar;
        public SettingsPageSeekBarViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_settings_page_seek_bar_img);
            title = itemView.findViewById(R.id.tv_settings_page_seek_bar_title);
            value = itemView.findViewById(R.id.tv_settings_page_seek_bar_value);
            lower = itemView.findViewById(R.id.tv_settings_page_seek_bar_lower_bound);
            upper = itemView.findViewById(R.id.tv_settings_page_seek_bar_upper_bound);
            seekBar = itemView.findViewById(R.id.sb_settings_page_seek_bar_seek_bar);
        }
    }

    class SettingsPageSeparatorViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        public SettingsPageSeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_settings_page_separator_title);
        }
    }

    public interface IGetTab {
        int getSize();
        int getType(int pos);

        //0,1,2
        String getTitle(int pos);
        //1,2
        Drawable getImg(int pos);
        //1
        CompoundButton.OnCheckedChangeListener getSwitchListener(int pos);
        boolean getSwitchChecked(int pos);
        //2
        SeekBar.OnSeekBarChangeListener getSeekBarListener(int pos);
        ISeekBarValueFormatter getSeekBarValueFormatter(int pos);
        double getSeekBarInitialPercentage(int pos);
        String getLowerBound(int pos);
        String getUpperBound(int pos);
    }
}
