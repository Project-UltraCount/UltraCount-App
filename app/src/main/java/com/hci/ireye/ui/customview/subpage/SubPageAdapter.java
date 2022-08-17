package com.hci.ireye.ui.customview.subpage;

//
// Created by Lithops on 2020/6/5, 8:23.
//

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hci.ireye.R;

public class SubPageAdapter extends RecyclerView.Adapter<SubPageAdapter.SubPageViewHolder> {

    private Context mContext;
    private IGetTab iGetTab;

    public SubPageAdapter(Context context, IGetTab iGetTab) {
        mContext = context;
        this.iGetTab = iGetTab;
    }

    @NonNull
    @Override
    public SubPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubPageViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_sub_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SubPageViewHolder holder, int position) {
        holder.window.setImg(iGetTab.getImg(position));
        holder.window.setTitle(iGetTab.getTitle(position));

        //remove current window from its parent first to avoid conflict
        if (iGetTab.getWindowView(position) != null && iGetTab.getWindowView(position).getParent() != null) {
            ((ViewGroup)iGetTab.getWindowView(position).getParent()).removeView(iGetTab.getWindowView(position));
        }
        holder.window.setWindowView(iGetTab.getWindowView(position));
    }

    @Override
    public int getItemCount() {
        return iGetTab.getSize();
    }

    class SubPageViewHolder extends RecyclerView.ViewHolder {
        private MyDropdownWindow window;

        SubPageViewHolder(View itemView) {
            super(itemView);
            window = itemView.findViewById(R.id.mdw_sub_page_window);
        }
    }

    public interface IGetTab {
        Drawable getImg(int pos);
        String getTitle(int pos);
        View getWindowView(int pos);
        int getSize();
    }

}
