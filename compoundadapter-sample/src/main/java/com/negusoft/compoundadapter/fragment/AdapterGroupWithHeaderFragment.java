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
import android.widget.Toast;

import com.negusoft.compoundadapter.R;
import com.negusoft.compoundadapter.adapter.HeaderAdapter;
import com.negusoft.compoundadapter.adapter.StaticDataAdapter;
import com.negusoft.compoundadapter.data.Samples;
import com.negusoft.compountadapter.recyclerview.AdapterGroup;

/**
 * Simple AdapterGroup that contains a header with a single element and a list of static data.
 */
public class AdapterGroupWithHeaderFragment extends Fragment {

    public static AdapterGroupWithHeaderFragment newInstance() {
        return new AdapterGroupWithHeaderFragment();
    }

    private RecyclerView mRecyclerView;
    private AdapterGroup mAdapterGroup;
    private StaticDataAdapter mSampleDataAdapter;
    private HeaderAdapter mHeaderAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.recyclerview, container, false);

        Context c = getActivity().getApplicationContext();
        mRecyclerView = ((RecyclerView)result.findViewById(R.id.recyclerview));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(c));
        getActivity().setTitle(R.string.sample_adapter_group_with_header);

        mHeaderAdapter = new HeaderAdapter(getString(R.string.sample_list_title));
        mSampleDataAdapter = new StaticDataAdapter();
        mSampleDataAdapter.setItemSelectedListener(new StaticDataAdapter.ItemSelectedListener() {
            @Override
            public void onItemSelected(String value) {
                Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
            }
        });

        mAdapterGroup = new AdapterGroup();
        mAdapterGroup.addAdapter(mHeaderAdapter);
        mAdapterGroup.addAdapter(mSampleDataAdapter);

        mRecyclerView.setAdapter(mAdapterGroup);

        return result;
    }
}
