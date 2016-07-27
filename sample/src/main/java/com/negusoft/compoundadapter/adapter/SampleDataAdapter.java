package com.negusoft.compoundadapter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.negusoft.compoundadapter.R;

/**
 * RecyclerView adapter displaying a static list of items
 */
public class SampleDataAdapter extends RecyclerView.Adapter<SampleDataAdapter.ViewHolder> {

    private static final String[] VALUES = new String[] {
            "ONE",
            "TWO",
            "THREE",
            "FOUR",
            "FIVE",
            "SIX",
            "SEVEN",
            "EIGHT",
            "NINE"
    };

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

    @Override
    public SampleDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_content, parent, false);
        return new SampleDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SampleDataAdapter.ViewHolder holder, int position) {
        holder.setText(VALUES[position]);
    }

    @Override
    public int getItemCount() {
        return VALUES.length;
    }
}
