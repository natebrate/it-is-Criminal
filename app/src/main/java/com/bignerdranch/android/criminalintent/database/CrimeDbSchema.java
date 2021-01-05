package com.bignerdranch.android.criminalintent.database;

public class CrimeDbSchema {
    public static final class CrimeTable {
        public static final String NAME = "crimes";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String ARCHIVED = "archived";
            public static final String SUSPECT = "suspect";
            public static final String DETAILS = "details";
            public static final String SOLVEDETAILS = "solveDetails";
            public static final String v2_LATITUDE = "latitude";
            public static final String v2_LONGITUDE = "longitude";
        }
    }
}