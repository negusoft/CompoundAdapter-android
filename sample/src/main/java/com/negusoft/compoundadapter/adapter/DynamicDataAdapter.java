package com.negusoft.compoundadapter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.negusoft.compoundadapter.R;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter displaying a static list of items
 */
public class DynamicDataAdapter extends RecyclerView.Adapter<DynamicDataAdapter.ViewHolder> {

    public interface ItemSelectedListener {
        void onItemSelected(Item item);
    }

    public static class Item {
        public final String value;
        public Item(String value) {
            this.value = value;
        }
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        Item item;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(android.R.id.text1);
        }

        void setOntItemSelectedListener(final ItemSelectedListener listener) {
            if (listener == null) {
                textView.setOnClickListener(null);
            } else {
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemSelected(item);
                    }
                });
            }
        }

        void setItem(Item item) {
            this.item = item;
            textView.setText(item.value);
        }

        String getText() {
            return textView.getText().toString();
        }
    }

    private final List<Item> mItems;
    private ItemSelectedListener mItemSelectedListener;

    public DynamicDataAdapter() {
        mItems = new ArrayList<>(10);
    }

    public DynamicDataAdapter(List<String> values) {
        mItems = new ArrayList<>(values.size());
        for (String value : values) {
            mItems.add(new Item(value));
        }
    }

    public Item addItem(String value) {
        Item item = new Item(value);
        mItems.add(item);
        notifyItemInserted(mItems.size() - 1);

        return item;
    }

    public void removeItem(Item item) {
        int index = mItems.indexOf(item);
        if (index >= 0) {
            mItems.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void setItemSelectedListener(ItemSelectedListener listener) {
        mItemSelectedListener = listener;
    }

    @Override
    public DynamicDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_content, parent, false);
        return new DynamicDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DynamicDataAdapter.ViewHolder holder, int position) {
        holder.setItem(mItems.get(position));
        holder.setOntItemSelectedListener(mItemSelectedListener);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
