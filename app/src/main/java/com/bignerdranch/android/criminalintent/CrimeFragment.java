package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static android.net.Uri.fromFile;
import static android.widget.CompoundButton.*;

public class  CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_FILE = 3;

    private Crime mCrime;
    private TextView mCrimeStatus;
    private File mPhotoFile;
    private EditText mCrimeDetails;
    private TextView mSolveDetailsTV;
    private EditText mSolveDetails;
    private Button mDateButton;
    private CheckBox mSolvedCheckbox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageButton mFileChooserButton;
    private ImageView mPhotoView;
    private Spinner mTitleSpinner;


    public static CrimeFragment newInstance(UUID crimeId, String Caller) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        args.putString("Caller", Caller);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        getActivity().setTitle("Edit Details");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
        String Caller = getArguments().getString("Caller");
        if (Caller.equals("ACTIVE")) {menu.getItem(0).setVisible(false);}
        if (Caller.equals("ARCHIVED")) {menu.getItem(1).setVisible(false);}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.active_crime:
                AlertDialog.Builder active = new AlertDialog.Builder(getContext());
                active.setTitle(R.string.app_name);
                active.setMessage("Do you want to active the crime?");
                active.setIcon(R.drawable.ic_active);
                active.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCrime.setArchived(false);
                        CrimeLab.get(getActivity()).updateCrime(mCrime);
                        dialog.dismiss();
                        getActivity().onBackPressed();
                    }
                });
                active.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertActive = active.create();
                alertActive.show();
                return true;
            case R.id.archive_crime:
                AlertDialog.Builder archive = new AlertDialog.Builder(getContext());
                archive.setTitle(R.string.app_name);
                archive.setMessage("Do you want to archive the crime?");
                archive.setIcon(R.drawable.ic_archive);
                archive.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCrime.setArchived(true);
                        CrimeLab.get(getActivity()).updateCrime(mCrime);
                        dialog.dismiss();
                        getActivity().onBackPressed();
                    }
                });
                archive.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertArchive = archive.create();
                alertArchive.show();
                return true;
            case R.id.delete_crime:
                AlertDialog.Builder delete = new AlertDialog.Builder(getContext());
                delete.setTitle(R.string.app_name);
                delete.setMessage("Do you want to delete the crime?");
                delete.setIcon(R.drawable.ic_archive);
                delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        deleteInternalImageExternalImageThumbnail();
                        CrimeLab.get(getActivity()).deleteCrime(mCrime);
                        getActivity().onBackPressed();
                    }
                });
                delete.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDelete = delete.create();
                alertDelete.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        setHasOptionsMenu(true);

        mCrimeStatus = (TextView) v.findViewById(R.id.crime_status);
        if (mCrime.isArchived()) {mCrimeStatus.setText("Status: Archived");}
        if (!mCrime.isArchived()) {mCrimeStatus.setText("Status: Active");}

        mTitleSpinner = (Spinner) v.findViewById(R.id.crime_titles);
        List<String> rows = new ArrayList<String>();
        rows.add("Theft");
        rows.add("Robbery");
        rows.add("Suicide");
        rows.add("Homicide");
        rows.add("Kidnapping");
        rows.add("Arson");
        rows.add("Smuggling");
        rows.add("Petty Crime");
        rows.add("Felony");
        rows.add("Other");
        ArrayAdapter<String> daRow = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rows);
        daRow.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTitleSpinner.setAdapter(daRow);
        mTitleSpinner.setSelection(((ArrayAdapter)mTitleSpinner.getAdapter()).getPosition(mCrime.getTitle()));
        mTitleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 mCrime.setTitle(String.valueOf(mTitleSpinner.getSelectedItem()));
             }
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
             }
        });

        mCrimeDetails = (EditText) v.findViewById(R.id.crime_details);
        mCrimeDetails.setText(mCrime.getDetails());
        mCrimeDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setDetails(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolveDetailsTV = (TextView) v.findViewById(R.id.solve_textView);
        mSolveDetails = (EditText) v.findViewById(R.id.solve_details);
        mSolveDetails.setText(mCrime.getSolveDetails());
        mSolveDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setSolveDetails(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckbox.setChecked(mCrime.isSolved());
        if (mSolvedCheckbox.isChecked()) {
            mSolveDetails.setVisibility(VISIBLE);
            mSolveDetailsTV.setVisibility(VISIBLE);
            mSolveDetails.setText(mCrime.getSolveDetails());
        } else {
            mSolveDetails.setVisibility(INVISIBLE);
            mSolveDetailsTV.setVisibility(INVISIBLE);
        }
        mSolvedCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    mCrime.setSolved(true);
                    mSolveDetails.setVisibility(VISIBLE);
                    mSolveDetailsTV.setVisibility(VISIBLE);
                    mSolveDetails.setText(mCrime.getSolveDetails());
                } else {
                    mCrime.setSolved(false);
                    mSolveDetails.setVisibility(INVISIBLE);
                    mSolveDetailsTV.setVisibility(INVISIBLE);
                }
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                Intent reportIntent = new Intent(Intent.ACTION_SEND);
                reportIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"xucanhao@gmail.com"});
                reportIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                reportIntent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                reportIntent.putExtra(Intent.EXTRA_STREAM, uri);
                reportIntent.setType("image/jpeg");
                reportIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(reportIntent, getString(R.string.send_report)));
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mFileChooserButton = (ImageButton) v.findViewById(R.id.crime_filechooser);
        mFileChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickFile = new Intent(Intent.ACTION_PICK);
                pickFile.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                pickFile.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(pickFile , REQUEST_FILE);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile);

            getActivity().revokeUriPermission(uri,
                    FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
            exportExternalPicture();
        } else if (requestCode == REQUEST_FILE) {
            Uri selectedFile = data.getData();
            String imagePath = "";
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContext().getContentResolver().query(selectedFile,
                    filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imagePath = cursor.getString(columnIndex);
                cursor.close();
            }
            try {importImage(imagePath);} catch (IOException exception) {}

            updatePhotoView();
            exportExternalPicture();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved) + " " + mCrime.getSolveDetails();
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "yyyy/MM/dd, HH:mm";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String details = "Crime details: " + mCrime.getDetails();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, details, suspect, solvedString);
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private void exportExternalPicture() {
        String dstImagePath = Environment.getExternalStorageDirectory() + File.separator + "CriminalIntent" + File.separator;
        File dstImage = new File(dstImagePath);
        try {exportImage(mPhotoFile.getPath(), dstImage);} catch (IOException exception) {}

        Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
        String dstThumbPath = dstImagePath + ".thumbnail" + File.separator;
        File dstThumb = new File(dstThumbPath);
        exportThumb(bitmap, dstThumb);

/*        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage("Internal pic: " + mPhotoFile.getPath() + "\n\n" +
                "External pic: " + dstImagePath + mCrime.getPhotoFilename() + "\n\n" +
                "External thumb: " + dstThumbPath + mCrime.getThumbnailFilename());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();*/
    }

    private File importImage(String srcFile) throws IOException
    {
        File src = new File(srcFile);
        File expFile = new File(mPhotoFile.getPath());
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {e.printStackTrace();}

        try {inChannel.transferTo(0, inChannel.size(), outChannel);} finally {
            if (inChannel != null) inChannel.close(); if (outChannel != null) outChannel.close();}
        return expFile;
    }

    private File exportImage(String srcFile, File dst) throws IOException
    {
        if (!dst.exists()) {if (!dst.mkdir()) {return null;}}

        File src = new File(srcFile);
        File expFile = new File(dst.getPath() + File.separator + mCrime.getPhotoFilename());
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {e.printStackTrace();}

        try {inChannel.transferTo(0, inChannel.size(), outChannel);} finally {
            if (inChannel != null) inChannel.close(); if (outChannel != null) outChannel.close();}
        return expFile;
    }

    private File exportThumb(Bitmap bitmap, File dst)
    {
        if (!dst.exists()) {if (!dst.mkdir()) {return null;}}

        File thumbnailFile = new File(dst.getPath() + File.separator + mCrime.getThumbnailFilename());

        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(thumbnailFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {e.printStackTrace();}
        return thumbnailFile;
    }

    private void deleteInternalImageExternalImageThumbnail()
    {
        File internalImage = new File(mPhotoFile.getPath());
        internalImage.delete();

        String externalImagePath = Environment.getExternalStorageDirectory() + File.separator + "CriminalIntent" + File.separator;
        String externalThumbPath = externalImagePath + ".thumbnail" + File.separator;

        File externalImage = new File(externalImagePath + File.separator + mCrime.getPhotoFilename());
        File externalThumb = new File(externalThumbPath + File.separator + mCrime.getThumbnailFilename());
        externalImage.delete();
        externalThumb.delete();
    }

}

