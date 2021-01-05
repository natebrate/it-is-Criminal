package com.bignerdranch.android.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.bignerdranch.android.criminalintent.Crime;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.Date;
import java.util.UUID;

import static com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable.*;

public class CrimeCursorWrapper extends CursorWrapper {

    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(Cols.UUID));
        String title = getString(getColumnIndex(Cols.TITLE));
        long date = getLong(getColumnIndex(Cols.DATE));
        int isSolved = getInt(getColumnIndex(Cols.SOLVED));
        int isArchived = getInt(getColumnIndex(Cols.ARCHIVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        String details = getString(getColumnIndex(Cols.DETAILS));
        String solveDetails = getString(getColumnIndex(Cols.SOLVEDETAILS));
        float latitude = getFloat(getColumnIndex(Cols.v2_LATITUDE));
        float longitude = getFloat(getColumnIndex(Cols.v2_LONGITUDE));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setArchived(isArchived != 0);
        crime.setSuspect(suspect);
        crime.setDetails(details);
        crime.setSolveDetails(solveDetails);
        crime.setLatitude(latitude);
        crime.setLongitude(longitude);

        return crime;
    }
}
