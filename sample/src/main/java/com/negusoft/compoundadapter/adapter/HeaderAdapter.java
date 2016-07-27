package com.negusoft.compoundadapter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.negusoft.compoundadapter.R;

/**
 * Adapter with a single element representing a header
 */
public class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.ViewHolder> {

    static final class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(android.R.id.text1);
        }

        void setText(String text) {
            textView.setText(text);
        }
    }

    private String mText;

    public HeaderAdapter(String text) {
        super();
        mText = text;
    }

    public void setText(String text) {
        mText = text;
        notifyItemChanged(0);
    }

    @Override
    public HeaderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_header, parent, false);
        return new HeaderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HeaderAdapter.ViewHolder holder, int position) {
        holder.setText(mText);
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
