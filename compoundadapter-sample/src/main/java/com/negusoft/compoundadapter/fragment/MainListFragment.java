package com.negusoft.compoundadapter.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.negusoft.compoundadapter.R;

public class MainListFragment extends Fragment {

    public static MainListFragment newInstance() {
        return new MainListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.main_content, container, false);
//        ((TextView)result.findViewById(android.R.id.text1)).setText("asdf");

        result.findViewById(R.id.adapterGroupWithHeader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, AdapterGroupWithHeaderFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return result;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
