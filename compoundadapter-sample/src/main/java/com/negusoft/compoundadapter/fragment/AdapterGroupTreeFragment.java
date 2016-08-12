package com.negusoft.compoundadapter.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.negusoft.compoundadapter.R;
import com.negusoft.compoundadapter.adapter.TreeNodeAdapter;
import com.negusoft.compountadapter.recyclerview.AdapterGroup;
import com.negusoft.compountadapter.recyclerview.AdapterPosition;

import java.util.Random;

public class AdapterGroupTreeFragment extends Fragment {

    private static final int INITIAL_NODE_COUNT = 2;
    private static final int INITIAL_NODE_CHILDREN_COUNT = 3;

    public static AdapterGroupTreeFragment newInstance() {
        return new AdapterGroupTreeFragment();
    }

    private RecyclerView mRecyclerView;
    private AdapterGroup mAdapterGroup;
    private TreeNodeAdapter mTreeNodeAdapter;

    private TreeNodeAdapter mSelectedNode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.tree_fragment, container, false);

        Context c = getActivity().getApplicationContext();
        mRecyclerView = ((RecyclerView)result.findViewById(R.id.recyclerview));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(c));
        getActivity().setTitle(R.string.sample_adapter_group_tree);

        // Listeners
        result.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedNode == null)
                    return;
                mSelectedNode.delete();
                mSelectedNode = null;
            }
        });
        result.findViewById(R.id.newChild).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedNode == null)
                    return;
                mSelectedNode.addNode(getRandomNodeName());
            }
        });
        result.findViewById(R.id.newSibling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedNode == null)
                    return;
                mSelectedNode.addSibling(getRandomNodeName());
            }
        });

        // Initialize the adapter
        mTreeNodeAdapter = new TreeNodeAdapter(getString(R.string.sample_list_title), mListener);

        for (int i=0; i<INITIAL_NODE_COUNT; i++) {
            TreeNodeAdapter node = mTreeNodeAdapter.addNode(getRandomNodeName());
            for (int j=0; j<INITIAL_NODE_CHILDREN_COUNT; j++) {
                node.addNode(getRandomNodeName());
            }
        }

        mAdapterGroup = new AdapterGroup();
        mAdapterGroup.addAdapter(mTreeNodeAdapter);
        mRecyclerView.setAdapter(mAdapterGroup);

        return result;
    }

    private String getRandomNodeName() {
        return String.format("Item %d", new Random().nextInt(100));
    }

    TreeNodeAdapter.ItemClickListener mListener = new TreeNodeAdapter.ItemClickListener() {
        @Override
        public void onNodeSelected(TreeNodeAdapter node, TreeNodeAdapter parentNode, int index) {
            if (mSelectedNode != null) {
                mSelectedNode.setSelected(false);
            }
            node.setSelected(true);
            mSelectedNode = node;
        }
    };
}
