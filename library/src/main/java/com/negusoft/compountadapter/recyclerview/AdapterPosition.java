package com.negusoft.compountadapter.recyclerview;

import android.support.v7.widget.RecyclerView;

/** Simple data structure containing an Adapter and a position. */
public class AdapterPosition {

    public final RecyclerView.Adapter adapter;
    public final int position;

    AdapterPosition(RecyclerView.Adapter adapter, int position) {
        this.adapter = adapter;
        this.position = position;
    }
}
