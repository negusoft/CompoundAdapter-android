package com.negusoft.compoundadapter.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.negusoft.compoundadapter.R;
import com.negusoft.compoundadapter.adapter.DynamicDataAdapter;
import com.negusoft.compoundadapter.data.Samples;
import com.negusoft.compountadapter.recyclerview.AdapterGroup;
import com.negusoft.compountadapter.recyclerview.SingleAdapter;

/**
 * Display an adapter to which items can be added and remove. It is wrapped in a AdapterGroup along
 * with a header adapter with one single element.
 */
public class AdapterGroupWithStableIds extends Fragment {

    public static AdapterGroupWithStableIds newInstance() {
        return new AdapterGroupWithStableIds();
    }

    private RecyclerView mRecyclerView;
    private AdapterGroup mAdapterGroup;
    private SingleAdapter mHeaderAdapter;
    private DynamicDataAdapter mDynamicDataAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.recyclerview, container, false);

        Context c = getActivity().getApplicationContext();
        mRecyclerView = ((RecyclerView)result.findViewById(R.id.recyclerview));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(c));
        getActivity().setTitle(R.string.sample_adapter_group_with_header);

        mHeaderAdapter = SingleAdapter.create(R.layout.item_sample_list_title);

        mDynamicDataAdapter = new DynamicDataAdapter();
        mDynamicDataAdapter.setItemSelectedListener(new DynamicDataAdapter.ItemSelectedListener() {
            @Override
            public void onItemSelected(DynamicDataAdapter.Item item) {
                mDynamicDataAdapter.removeItem(item, false);
                mDynamicDataAdapter.notifyDataSetChanged();
            }
        });

        mAdapterGroup = new AdapterGroup();
        mAdapterGroup.addAdapter(mHeaderAdapter);
        mAdapterGroup.addAdapter(mDynamicDataAdapter);
        mAdapterGroup.setHasStableIds(true);

        mRecyclerView.setAdapter(mAdapterGroup);

        return result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.adaptergroup_with_header, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            addItem();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addItem() {
        String value = Samples.getRandomSample();
        mDynamicDataAdapter.addItem(value);
        mDynamicDataAdapter.notifyDataSetChanged();
    }
}
