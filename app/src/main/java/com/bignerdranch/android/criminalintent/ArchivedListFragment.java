package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArchivedListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "archived";

    private RecyclerView mArchiveRecyclerView;
    private ArchiveAdapter mArchiveAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archived_list, container, false);

        mArchiveRecyclerView = (RecyclerView) view
                .findViewById(R.id.archived_recycler_view);
        mArchiveRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            //mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.fragment_archived_list, menu);
        updateArchivedSubtitle();

        MenuItem subtitleItem = menu.findItem(R.id.show_active);
        subtitleItem.setTitle(R.string.show_active);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_active:
                getActivity().onBackPressed();
                return true;
            case R.id.sort_by:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateArchivedSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int archiveCount = crimeLab.getArchives().size();
        String subtitle = getString(R.string.archive_format, archiveCount);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> archives = crimeLab.getArchives();

        if (mArchiveAdapter == null) {
            mArchiveAdapter = new ArchiveAdapter(archives);
            mArchiveRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
            mArchiveRecyclerView.setAdapter(mArchiveAdapter);
        } else {
            mArchiveAdapter.setCrimes(archives);
            mArchiveAdapter.notifyDataSetChanged();
        }

        updateArchivedSubtitle();
    }

    private class ArchiveHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Crime mCrime;

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        public ArchiveHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_archived, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = ArchivedPagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
        }
    }

    private class ArchiveAdapter extends RecyclerView.Adapter<ArchiveHolder> {

        private List<Crime> mCrimes;

        public ArchiveAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public ArchiveHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ArchiveHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ArchiveHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }
}
