package com.negusoft.compoundadapter.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.negusoft.compoundadapter.R;
import com.negusoft.compountadapter.recyclerview.AdapterGroup;
import com.negusoft.compountadapter.recyclerview.AdapterPosition;

import java.util.Random;

/** Adapter representing a node in a tree and allows adding/removing child nodes. */
public class TreeNodeAdapter extends AdapterGroup {

    // The depth of the node, it will determine the indentation
    private final int mDepth;

    // Text to be displayed for the node
    private final String mName;

    private NodeItemAdapter mNodeItemAdapter;
    private TreeNodeAdapter mParentNode;

    private ItemClickListener mListener;

    private boolean mSelected = false;

    public TreeNodeAdapter(String name, ItemClickListener listener) {
        this(0, name, listener);
    }

    TreeNodeAdapter(int depth, String name, ItemClickListener listener) {
        super();
        mDepth = depth;
        mName = name;
        mListener = listener;
        mNodeItemAdapter = new NodeItemAdapter();
        addAdapter(mNodeItemAdapter);
    }

    public TreeNodeAdapter addNode(String name) {
        TreeNodeAdapter node = new TreeNodeAdapter(mDepth + 1, name, mListener);
        addAdapter(node);

        node.mParentNode = this;

        return node;
    }

    public void addSibling(String name) {
        if (mParentNode == null)
            return;
        mParentNode.addNode(name);
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
        mNodeItemAdapter.notifyItemChanged(0);
    }

    public void delete() {
        if (mParentNode == null)
            return;
        mParentNode.removeAdapter(this);
    }

    public TreeNodeAdapter getParentNode() {
        return mParentNode;
    }

    public interface ItemClickListener {
        void onNodeSelected(TreeNodeAdapter node, TreeNodeAdapter parentNode, int index);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        View selectableView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            selectableView = itemView.findViewById(R.id.selectable);
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

        public void setClickListener(View.OnClickListener listener) {
            textView.setOnClickListener(listener);
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
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Context c = holder.textView.getContext();

            holder.setDepth(mDepth);
            holder.setText(mName);
            holder.setClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onNodeSelected(TreeNodeAdapter.this, getParentNode(), position);
                }
            });
            holder.selectableView.setSelected(mSelected);
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

}
