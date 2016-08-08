package com.negusoft.compoundadapter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.negusoft.compoundadapter.R;
import com.negusoft.compoundadapter.data.Samples;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter displaying a static list of items
 */
public class StaticDataAdapter extends RecyclerView.Adapter<StaticDataAdapter.ViewHolder> {

    public interface ItemSelectedListener {
        public void onItemSelected(String value);
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(android.R.id.text1);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), textView.getText(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        void setText(String text) {
            textView.setText(text);
        }

        String getText() {
            return textView.getText().toString();
        }
    }

    private ItemSelectedListener mItemSelectedListener;

    public void setItemSelectedListener(ItemSelectedListener listener) {
        mItemSelectedListener = listener;
    }

    @Override
    public StaticDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_content, parent, false);
        return new StaticDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final StaticDataAdapter.ViewHolder holder, int position) {
        holder.setText(Samples.VALUES[position]);
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemSelectedListener != null)
                    mItemSelectedListener.onItemSelected(holder.getText());
            }
        });
    }

    @Override
    public int getItemCount() {
        return Samples.VALUES.length;
    }
}
