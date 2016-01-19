package com.somexapps.wyre.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.somexapps.wyre.R;
import com.somexapps.wyre.adapters.SongGridAdapter;
import com.somexapps.wyre.api.SoundCloudClient;
import com.somexapps.wyre.api.SoundCloudUserResult;
import com.somexapps.wyre.api.activities.ActivitiesResult;
import com.somexapps.wyre.api.activities.CollectionResult;
import com.somexapps.wyre.api.tracks.SoundCloudTrackResult;
import com.somexapps.wyre.models.AccessToken;
import com.somexapps.wyre.models.Song;
import com.somexapps.wyre.services.AudioPlugChangeReceiver;
import com.somexapps.wyre.services.MediaService;
import com.somexapps.wyre.services.ServiceGenerator;
import com.somexapps.wyre.utils.ConnectionUtils;
import com.somexapps.wyre.utils.Constants;
import com.somexapps.wyre.utils.RequestCodeGenerator;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    // Used for read external storage permission
    private final static int READ_EXTERNAL_STORAGE_REQUEST_CODE = RequestCodeGenerator.getFreshInt();

    /**
     * Begin view bindings
     */
    @Bind(R.id.activity_main_grid_view) GridView mSongGrid;
    @Bind(R.id.activity_main_toolbar) Toolbar appToolbar;
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

    private AccountHeader appDrawerHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register receivers
        registerReceivers();

        // Bind views
        ButterKnife.bind(this);

        // Set up toolbar
        setSupportActionBar(appToolbar);

        // Build out drawer
        buildDrawer(appToolbar);

        // Refresh list of music
        setUpPermissions();
    }

    private void buildDrawer(Toolbar appToolbar) {
        // Set up account header
        appDrawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.primary_dark)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(getString(R.string.account_header_drawer_switch_account_title))
                                .withIcon(getResources().getDrawable(android.R.drawable.btn_plus))
                                .withSelectable(false)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile iProfile, boolean b) {
                        // Show login activity
                        return true;
                    }
                })
                .build();

        // Set up the drawer
        new DrawerBuilder()
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
    }

    private void registerReceivers() {
        // Register receiver for headset state change
        registerReceiver(
                new AudioPlugChangeReceiver(),
                new IntentFilter(Intent.ACTION_HEADSET_PLUG)
        );
    }

    // TODO: This is a placeholder, until proper onboarding is implemented.
    private void setUpPermissions() {
        // Check if we have read permission for music query
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            // TODO: Might need to fix this for lower than 16
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_REQUEST_CODE
            );
        } else {
            // Refresh local music
            setUpUi();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            // Make sure we got permissions results back
            if (permissions.length > 0
                    && grantResults.length > 0) {
                if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // We got permission, refresh music
                        setUpUi();
                    } else {
                        // TODO: Show error dialog
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

        // Make sure cursor was created successfully
        if (cursor != null) {
            while (cursor.moveToNext()) {
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

    private void setUpUi() {
        // Refresh music
        refreshMusic();

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
}
