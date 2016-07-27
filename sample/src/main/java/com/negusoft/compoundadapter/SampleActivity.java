package com.negusoft.compoundadapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.negusoft.compoundadapter.fragment.AdapterGroupWithHeaderFragment;
import com.negusoft.compoundadapter.fragment.MainListFragment;

public class SampleActivity extends AppCompatActivity {

    private static String EXTRA_SAMPLE_TYPE = "EXTRA_SAMPLE_TYPE";

    public enum SampleType {
        ADAPTER_GROUP_WITH_HEADER
    }

    public static Intent makeIntent(Context c, SampleType sampleType) {
        Intent result = new Intent(c, SampleActivity.class)
                .putExtra(EXTRA_SAMPLE_TYPE, sampleType.name());
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            String typeName = getIntent().getStringExtra(EXTRA_SAMPLE_TYPE);
            SampleType type = SampleType.valueOf(typeName);
            addFragment(type);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addFragment(SampleType type) {
        switch (type) {
            case ADAPTER_GROUP_WITH_HEADER:
                addFragment(AdapterGroupWithHeaderFragment.newInstance());
                break;
        }
    }

    private void addFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content, fragment)
                .commit();
    }

}
