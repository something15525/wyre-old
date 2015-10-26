package com.somexapps.wyre.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.somexapps.wyre.R;
import com.somexapps.wyre.adapters.SongGridAdapter;
import com.somexapps.wyre.api.SoundCloudClient;
import com.somexapps.wyre.api.SoundCloudUserResult;
import com.somexapps.wyre.api.activities.ActivitiesResult;
import com.somexapps.wyre.api.activities.CollectionResult;
import com.somexapps.wyre.api.tracks.SoundCloudTrackResult;
import com.somexapps.wyre.models.AccessToken;
import com.somexapps.wyre.models.Song;
import com.somexapps.wyre.services.MediaService;
import com.somexapps.wyre.services.ServiceGenerator;
import com.somexapps.wyre.utils.ConnectionUtils;
import com.somexapps.wyre.utils.Constants;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit.RetrofitError;

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
public class WyreActivity extends AppCompatActivity {
    // Used for logging
    private final static String TAG = WyreActivity.class.getSimpleName();

    /**
     * Begin view bindings
     */
    @Bind(R.id.activity_main_grid_view) GridView mSongGrid;
    /**
     * End view bindings
     */

    /**
     * Holder for adapter to handle song grid.
     */
    private SongGridAdapter songGridAdapter;

    /**
     * Holder for the list of songs.
     */
    private ArrayList<Song> mSongs;

    /**
     * The client used to communicate with the SoundCloud API.
     */
    private SoundCloudClient soundCloudClient;

    /**
     * The AccessToken used for authenticated SoundCloud requests.
     */
    private AccessToken soundCloudAccessToken;

    private AccountHeader appDrawerHeader;
    private Drawer appDrawer;
    private boolean soundCloudSongsLoaded = false;

    private ArrayList<IProfile> appDrawerProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar appToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);

        setSupportActionBar(appToolbar);

        // Set up account header
        appDrawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.primary_dark)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(getString(R.string.account_header_drawer_switch_account_title))
                                .withIcon(getResources().getDrawable(android.R.drawable.btn_plus))
                                .setSelectable(false)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile iProfile, boolean b) {
                        // Show login activity
                        startActivity(new Intent(WyreActivity.this, LoginActivity.class));
                        return true;
                    }
                })
                .build();

        // Grab profiles
        appDrawerProfiles = appDrawerHeader.getProfiles();

        // Set up the drawer
        appDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(appToolbar)
                .withAccountHeader(appDrawerHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName("Listen")
                                .withIcon(
                                        new IconicsDrawable(this)
                                                .icon(FontAwesome.Icon.faw_music)
                                                .sizeDp(24)
                                )
                                .withIconTintingEnabled(true)
                )
                .build();

        // Bind views
        ButterKnife.bind(this);

        // Refresh list of music
        refreshMusic();

        // Start up SoundCloud client
        initSoundCloudService();

        // Make sure profiles are up to date
        updateProfiles();

        // Check if logged in
        if (getSharedPreferences(Constants.RIPPLE_PREFS_NAME, MODE_PRIVATE)
                .getBoolean(Constants.PREF_SOUND_CLOUD_LOGGED_IN, false)) {
            // TODO: Remove this, data concerns for now, need to cache this result
            if (ConnectionUtils.isOnWifi(this)) {
                // Refresh SoundCloud stream
                refreshSoundCloudTracks();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Please connect to WiFi to see your SoundCloud tracks.")
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }

        // Set up adapter and attach
        songGridAdapter = new SongGridAdapter(this, mSongs);
        mSongGrid.setAdapter(songGridAdapter);

        // Set up onClick listener for playing songs
        mSongGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Play the song
                Intent playIntent = new Intent(getApplicationContext(), MediaService.class);
                playIntent.putParcelableArrayListExtra(MediaService.EXTRA_SONG_LIST, mSongs);
                playIntent.putExtra(MediaService.EXTRA_SONG_TO_PLAY, position);
                playIntent.setAction(MediaService.ACTION_PLAY);
                startService(playIntent);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Make sure profiles are up to date
        updateProfiles();

        if (!soundCloudSongsLoaded) {
            // Check if logged in
            if (getSharedPreferences(Constants.RIPPLE_PREFS_NAME, MODE_PRIVATE)
                    .getBoolean(Constants.PREF_SOUND_CLOUD_LOGGED_IN, false)) {
                // TODO: Remove this, data concerns for now, need to cache this result
                if (ConnectionUtils.isOnWifi(this)) {
                    // Refresh SoundCloud stream
                    refreshSoundCloudTracks();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage("Please connect to WiFi to see your SoundCloud tracks.")
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                }
            }
        }
    }

    private void initSoundCloudService() {
        if (soundCloudClient == null) {
            soundCloudClient = ServiceGenerator.createService(
                    SoundCloudClient.class,
                    SoundCloudClient.BASE_URL
            );
        }
    }

    /**
     * Queries the SoundCloud API for tracks and updates the UI when finished parsing the data.
     */
    private void refreshSoundCloudTracks() {
        if (soundCloudClient != null &&
                soundCloudAccessToken != null) {
            soundCloudClient.getActivities(
                    soundCloudAccessToken.getAccessToken(),
                    50,
                    new retrofit.Callback<ActivitiesResult>() {
                        @Override
                        public void success(ActivitiesResult activitiesResult, retrofit.client.Response response) {
                            // Get the collections
                            List<CollectionResult> collectionResultList =
                                    activitiesResult.getCollection();

                            // Loop through each and add song
                            for (CollectionResult result : collectionResultList) {
                                // Get the track information
                                SoundCloudTrackResult trackResult = result.getOrigin();

                                // Create song from track information
                                if (trackResult.isStreamable()
                                        && trackResult.getStream_url() != null) {
                                    final Song newSong = new Song();
                                    newSong.setAlbumArtPath(trackResult.getArtwork_url());
                                    newSong.setArtist(trackResult.getUser().getUsername());
                                    newSong.setTitle(trackResult.getTitle());
                                    newSong.setDuration(Long.toString(trackResult.getDuration()));

                                    new OkHttpClient()
                                            .newCall(
                                                    new Request.Builder()
                                                            .url(trackResult.getStream_url() +
                                                                    "?client_id=" + getString(R.string.soundcloud_client_id))
                                                            .build()
                                            )
                                            .enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Request request, IOException e) {
                                                    Log.e(TAG, "Error grabbing stream URL: " + e.getMessage());
                                                }

                                                @Override
                                                public void onResponse(Response response) throws IOException {
                                                    if (response.request().httpUrl() != null) {
                                                        newSong.setData(
                                                                response.request().httpUrl().url().toString()
                                                        );
                                                    }

                                                    // Add to list
                                                    mSongs.add(newSong);

                                                    // Update list
                                                    if (songGridAdapter != null) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                songGridAdapter.notifyDataSetChanged();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                }

                                // Flag boolean as true
                                soundCloudSongsLoaded = true;
                            }
                        }

                        @Override
                        public void failure(final RetrofitError error) {
                            // Show error on UI thread
                            WyreActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(WyreActivity.this)
                                            .setMessage("Error retrieving your SoundCloud Stream: "
                                                    + error.getMessage() + ". Please logout and log" +
                                                    "back in.")
                                            .setNeutralButton(android.R.string.ok, null)
                                            .show();
                                }
                            });
                        }
                    }
            );
        }
    }

    /**
     * This method checks the Realm database for any access tokens, then updates the profiles based
     * on the result.
     */
    private void updateProfiles() {
        // Get realm instance and perform query
        Realm theRealm = Realm.getInstance(this);
        RealmQuery<AccessToken> accessQuery = new RealmQuery<>(theRealm, AccessToken.class);
        RealmResults<AccessToken> accessResults = accessQuery.findAll();

        // Make sure we have results, otherwise go back to placeholder
        if (accessResults.size() > 0) {
            // Get the first token (should only be one)
            soundCloudAccessToken = accessResults.first();

            // Grab string for oauthToken so we can cross threads with it.
            final String oauthToken = soundCloudAccessToken.getAccessToken();

            soundCloudClient.getUser(oauthToken, new retrofit.Callback<SoundCloudUserResult>() {
                @Override
                public void success(SoundCloudUserResult soundCloudUserResult,
                                    retrofit.client.Response response) {
                    // Get list of profiles (should be just one)
                    appDrawerProfiles = appDrawerHeader.getProfiles();

                    // Modify the first drawer profile
                    if (appDrawerProfiles.size() > 0) {
                        // Grab profile and modify
                        final IProfile profile = appDrawerProfiles.remove(0);
                        profile.setName(soundCloudUserResult.getFull_name());
                        profile.setEmail(soundCloudUserResult.getUsername());
                        profile.setIconBitmap(null);

                        // Get profile image
                        // TODO: Use picasso to load the profile image
                        new OkHttpClient()
                                .newCall(
                                        new Request.Builder()
                                                .url(soundCloudUserResult.getAvatar_url())
                                                .build())
                                .enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Request request, IOException e) {
                                        Log.e("TAG", "TAG");
                                    }

                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        Bitmap bitmap = BitmapFactory.decodeStream(
                                                response.body().byteStream()
                                        );

                                        if (bitmap != null) {
                                            // Set the bitmap to the profile
                                            profile.setIconBitmap(bitmap);

                                            updateOrAddToProfileList(profile);

                                            // Update profiles on UI thread
                                            WyreActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    appDrawerHeader.setProfiles(appDrawerProfiles);
                                                    appDrawerHeader.setActiveProfile(profile);
                                                }
                                            });
                                        }
                                    }
                                });

                        // Update in list
                        updateOrAddToProfileList(profile);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "Error grabbing user info: " + error.getMessage());
                }
            });
        } else {
            // Grab profile and modify to add account
            final IProfile profile = appDrawerProfiles.remove(0);
            profile.setName(getString(R.string.account_header_drawer_switch_account_title));
            profile.setEmail(null);
            profile.setIconBitmap(null);

            updateOrAddToProfileList(profile);
        }
    }

    private void updateOrAddToProfileList(final IProfile profileToUpdate) {
        // Find the profile in the list and update it if it exists
        int profileLocation = appDrawerProfiles.indexOf(profileToUpdate);
        if (profileLocation > -1) {
            // Update the profile in the list
            appDrawerProfiles.set(profileLocation, profileToUpdate);
        } else {
            // Add the profile to the list
            appDrawerProfiles.add(0, profileToUpdate);
        }

        // Create runnable to update UI
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Update profiles
                appDrawerHeader.setProfiles(appDrawerProfiles);
                appDrawerHeader.setActiveProfile(profileToUpdate);

                // Make sure AccountHeader selection is closed
                if (appDrawerHeader.isSelectionListShown()) {
                    appDrawerHeader.toggleSelectionList(WyreActivity.this);
                }
            }
        };

        // Update the UI on main thread
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            updateRunnable.run();
        } else {
            WyreActivity.this.runOnUiThread(updateRunnable);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    private void refreshMusic() {
        // Grab the local music store
        // Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        // Create list if doesn't exist
        if (mSongs == null) {
            mSongs = new ArrayList<>();
        }

        // Clear the list
        mSongs.clear();

        while(cursor.moveToNext()) {
            // Create new song
            Song currentSong = new Song();

            // Set data
            currentSong.setArtist(cursor.getString(1));
            currentSong.setTitle(cursor.getString(2));
            currentSong.setData(cursor.getString(3));
            currentSong.setDisplayName(cursor.getString(4));
            currentSong.setDuration(cursor.getString(5));

            // Add to the list
            mSongs.add(currentSong);
        }

        // Close the cursor
        cursor.close();
    }
}
