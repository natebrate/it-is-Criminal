package com.bignerdranch.android.criminalintent.database;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.bignerdranch.android.criminalintent.Crime;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrimeBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "crimeBase.db";

    public CrimeBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CrimeTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                CrimeTable.Cols.UUID + ", " +
                CrimeTable.Cols.TITLE + ", " +
                CrimeTable.Cols.DATE + ", " +
                CrimeTable.Cols.DETAILS + ", " +
                CrimeTable.Cols.SOLVED + ", " +
                CrimeTable.Cols.SOLVEDETAILS + ", " +
                CrimeTable.Cols.ARCHIVED + ", " +
                CrimeTable.Cols.v2_LATITUDE + ", " +
                CrimeTable.Cols.v2_LONGITUDE + ", " +
                CrimeTable.Cols.SUSPECT +
                ")"
        );
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("alter table " + CrimeTable.NAME + " add column " +
                    CrimeTable.Cols.v2_LATITUDE + " FLOAT DEFAULT -999");
            db.execSQL("alter table " + CrimeTable.NAME + " add column " +
                    CrimeTable.Cols.v2_LONGITUDE + " FLOAT DEFAULT -999");
        }
    }

}