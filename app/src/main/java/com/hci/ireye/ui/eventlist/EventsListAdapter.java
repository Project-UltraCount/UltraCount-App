package com.hci.ireye.ui.eventlist;

//
// Created by Lithops on 2022/6/2, 18:38.
//

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hci.ireye.R;

import java.text.DateFormat;
import java.util.Date;

public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.EventsWindowViewHolder> {

    private Context mContext;
    private IGetEntry mIGetEntry;

    public EventsListAdapter(Context context, EventsListAdapter.IGetEntry iGetEntry) {
        super();
        mContext = context;
        mIGetEntry = iGetEntry;
    }

    @NonNull
    @Override
    public EventsListAdapter.EventsWindowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventsListAdapter.EventsWindowViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_eventlist_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EventsListAdapter.EventsWindowViewHolder holder, int position) {
        holder.eventName.setText(mIGetEntry.getEventName(position));
        holder.startTime.setText(
                DateFormat.getDateTimeInstance().format(new Date(mIGetEntry.getStartTime(position) * 1000L))
        );
        holder.card.setOnClickListener(mIGetEntry.getClickListener(position));
    }

    @Override
    public int getItemCount() {
        return mIGetEntry.getSize();
    }



    class EventsWindowViewHolder extends RecyclerView.ViewHolder {
        private TextView eventName, startTime;
        private CardView card;
        EventsWindowViewHolder(View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.tv_event_name);
            startTime = itemView.findViewById(R.id.tv_event_start_time);
            card = itemView.findViewById(R.id.card_event);
        }
    }


    public interface IGetEntry {
        String getEventName(int pos);
        int getStartTime(int pos);
        int getSize();
        View.OnClickListener getClickListener(int pos);
    }
}