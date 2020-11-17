package com.example.criminalintent1;

import java.util.*;

public class Crime {
    private UUID mId;
    private String mTitle = "Petty Crime";
    private Date mDate;
    private String mCrimes;
    private boolean mSolved = false;
    private String mDetails = "Details Unavailabe";

    private String mSuspect;

    public Crime(){
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public String getmCrimes() {
        return mCrimes;
    }

    public void setmCrimes(String mCrime) {
        this.mCrimes = mCrime;
    }

    public UUID getmId() {
        return mId;
    }

    public void setmId(UUID mId) {
        this.mId = mId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Date getmDate() {
        return mDate;
    }

    public void setmDate(Date mDate) {
        this.mDate = mDate;
    }

    public boolean ismSolved() {
        return mSolved;
    }

    public void setmSolved(boolean mSolved) {
        this.mSolved = mSolved;
    }

    public String getmSuspect() {
        return mSuspect;
    }

    public void setmSuspect(String mSuspect) {
        this.mSuspect = mSuspect;
    }

    public String getPhotoFilename() {
        return "IMG_" + getmId().toString() + ".jpg";
    }

    public void setTitle(String valueOf) {
    }
}
