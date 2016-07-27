package com.negusoft.compoundadapter.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.negusoft.compoundadapter.R;
import com.negusoft.compoundadapter.adapter.HeaderAdapter;
import com.negusoft.compoundadapter.adapter.SampleDataAdapter;
import com.negusoft.compountadapter.recyclerview.AdapterGroup;

import org.w3c.dom.Text;

public class AdapterGroupWithHeaderFragment extends Fragment {

    public static AdapterGroupWithHeaderFragment newInstance() {
        return new AdapterGroupWithHeaderFragment();
    }

    private RecyclerView mRecyclerView;
    private AdapterGroup mAdapterGroup;
    private SampleDataAdapter mSampleDataAdapter;
    private HeaderAdapter mHeaderAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.recyclerview, container, false);

        Context c = getActivity().getApplicationContext();
        mRecyclerView = ((RecyclerView)result.findViewById(R.id.recyclerview));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(c));

        mHeaderAdapter = new HeaderAdapter(getString(R.string.sample_list_title));
        mSampleDataAdapter = new SampleDataAdapter();
        mAdapterGroup = new AdapterGroup();
        mAdapterGroup.addAdapter(mHeaderAdapter);
        mAdapterGroup.addAdapter(mSampleDataAdapter);
        mRecyclerView.setAdapter(mAdapterGroup);

        return result;
    }
}
