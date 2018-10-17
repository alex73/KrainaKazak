package org.alex73.android.dzietkam.util;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

public class EmptyRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder arg0, int arg1) {
    }
}
