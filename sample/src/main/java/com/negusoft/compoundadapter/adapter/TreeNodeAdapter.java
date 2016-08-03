package com.negusoft.compoundadapter.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.negusoft.compoundadapter.R;
import com.negusoft.compountadapter.recyclerview.AdapterGroup;

/** Adapter representing a node in a tree and allows adding/removing child nodes. */
public class TreeNodeAdapter extends AdapterGroup {

    // The depth of the node, it will determine the indentation
    private final int mDepth;

    // Text to be displayed for the node
    private final String mName;

    public TreeNodeAdapter(String name) {
        this(0, name);
    }

    TreeNodeAdapter(int depth, String name) {
        super();
        mDepth = depth;
        mName = name;
        addAdapter(new NodeItemAdapter());
    }

    public TreeNodeAdapter addNode(String name) {
        TreeNodeAdapter node = new TreeNodeAdapter(mDepth + 1, name);
        addAdapter(node);

        return node;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }

        public void setDepth(int depth) {
            Resources r = textView.getContext().getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
            textView.setPaddingRelative((int)(px * (depth + 1)), 0, 0, 0);
        }

        public void setText(String text) {
            textView.setText(text);
        }
    }

    private class NodeItemAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setDepth(mDepth);
            holder.setText(mName);
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

}
