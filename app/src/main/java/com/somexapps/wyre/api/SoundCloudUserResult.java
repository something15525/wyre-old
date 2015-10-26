package com.somexapps.wyre.api;

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
public class SoundCloudUserResult {
    private int id;
    private String permalink;
    private String username;
    private String uri;
    private String permalink_url;
    private String avatar_url;
    private String country;
    private String full_name;
    private String city;
    private String description;
    // TODO: discogs-name
    // TODO: myspace-name
    private String website;
    // TODO: website-title
    private boolean online;
    private int track_count;
    private int playlist_count;
    private int followers_count;
    private int followings_count;
    private int public_favorites_count;
    // TODO: avatar_data?
    private String plan;
    private int private_tracks_count;

    public int getId() {
        return id;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getUsername() {
        return username;
    }

    public String getUri() {
        return uri;
    }

    public String getPermalink_url() {
        return permalink_url;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public String getCountry() {
        return country;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getCity() {
        return city;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public boolean isOnline() {
        return online;
    }

    public int getTrack_count() {
        return track_count;
    }

    public int getPlaylist_count() {
        return playlist_count;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public int getFollowings_count() {
        return followings_count;
    }

    public int getPublic_favorites_count() {
        return public_favorites_count;
    }

    public String getPlan() {
        return plan;
    }

    public int getPrivate_tracks_count() {
        return private_tracks_count;
    }
}
