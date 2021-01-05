package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.criminalintent.database.CrimeBaseHelper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "archived";

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (!PermissionManager.checkWriteStoragePermissions(getActivity())) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) view
                .findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            //mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI(0);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        updateActiveCrimeSubtitle();

        MenuItem subtitleItem = menu.findItem(R.id.show_archived);
        subtitleItem.setTitle(R.string.show_archived);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity
                        .newIntent(getActivity(), crime.getId());
                startActivity(intent);
                return true;
            case R.id.show_archived:
                Intent intentArchived = new Intent(getActivity(), ArchivedListActivity.class);
                startActivity(intentArchived);
                return true;
            case R.id.sort_by:
                final String[] sortBy = {"Title A-Z", "Title Z-A", "Date New-Old", "Date Old-New",
                        "Solved First", "Unsolved First"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Sort list by:");
                builder.setItems(sortBy, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        updateUI(item);
                        dialog.dismiss();
                    }
                }).show();
                return true;
            case R.id.export_csv:
                exportCSV();
                return true;
            case R.id.import_csv:
                importCSV();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void importCSV() {
    }


    private void exportCSV() {
        CrimeBaseHelper dbHelper = new CrimeBaseHelper(getContext());
        String dstCSVPath = Environment.getExternalStorageDirectory() + File.separator + "CriminalIntent" + File.separator;
        File exportCSV = new File(dstCSVPath);
        if (!exportCSV.exists())
        {
            exportCSV.mkdirs();
        }

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String strDate = dateFormat.format(date);
        File file = new File(exportCSV, "DB" + strDate + ".csv");
        try
        {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage("Saved CSV file: " + "\n\n" + dstCSVPath + file.getName());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        }
        catch(Exception sqlEx)
        {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }

    private void updateActiveCrimeSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateUI(final int sortBy) {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        Collections.sort(crimes, new Comparator<Crime>() {
            @Override
            public int compare(Crime a, Crime b) {
                switch (sortBy) {
                    case 0:
                        return a.getTitle().compareTo(b.getTitle());
                    case 1:
                        return b.getTitle().compareTo(a.getTitle());
                    case 2:
                        return a.getDate().compareTo(b.getDate());
                    case 3:
                        return b.getDate().compareTo(a.getDate());
                    case 4:
                        return Boolean.compare(b.isSolved(), a.isSolved());
                    case 5:
                        return Boolean.compare(a.isSolved(), b.isSolved());
                    default:
                        return a.getTitle().compareTo(b.getTitle());
                }
            }
        });

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        updateActiveCrimeSubtitle();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Crime mCrime;

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
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
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
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
