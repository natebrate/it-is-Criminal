package com.bignerdranch.android.criminalintent;

import java.util.Date;
import java.util.UUID;

public class Crime {

    private UUID mId;
    private String mTitle = "Petty Crime";
    private Date mDate;
    private boolean mSolved = false;
    private boolean mArchived = false;
    private String mSuspect = "John Doe";
    private String mDetails = "N/A";
    private String mSolveDetails = "N/A";
    private float mLatitude = -999f;
    private float mLongitude = -999f;

    public Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public boolean isArchived() {
        return mArchived;
    }

    public void setArchived(boolean archived) {
        mArchived = archived;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public Float getLatitude() {
        return mLatitude;
    }

    public void setLatitude(Float latitude) {
        mLatitude = latitude;
    }

    public Float getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Float longitude) {
        mLongitude = longitude;
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    public String getThumbnailFilename() {
        return "TN_" + getId().toString() + ".jpg";
    }

    public String getDetails() {
        return mDetails;
    }

    public void setDetails(String details) {
        mDetails = details;
    }

    public String getSolveDetails() {
        return mSolveDetails;
    }

    public void setSolveDetails(String solveDetails) {
        mSolveDetails = solveDetails;
    }
}
