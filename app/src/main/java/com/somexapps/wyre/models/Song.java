package com.somexapps.wyre.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Copyright 2015 Michael Limb
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Song implements Parcelable {
    private String mArtist;
    private String mTitle;
    private String mData;
    private String mDisplayName;
    private String mDuration;
    private String mAlbumArtPath;

    public Song() {
        // default constructor
    }

    public Song(Parcel source) {
        // Read it all back in
        mArtist = source.readString();
        mTitle = source.readString();
        mData = source.readString();
        mDisplayName = source.readString();
        mDuration = source.readString();
        mAlbumArtPath = source.readString();
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write all the strings
        dest.writeString(mArtist);
        dest.writeString(mTitle);
        dest.writeString(mData);
        dest.writeString(mDisplayName);
        dest.writeString(mDuration);
        dest.writeString(mAlbumArtPath);
    }

    @Override
    public int describeContents() {
        // do nothing here for now
        return 0;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }

    public String getAlbumArtPath() {
        return mAlbumArtPath;
    }

    public void setAlbumArtPath(String albumArtPath) {
        mAlbumArtPath = albumArtPath;
    }
}