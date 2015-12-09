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

    private AccountHeader appDrawerHeader;

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

        // Bind views
        ButterKnife.bind(this);

        // Refresh list of music
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
}
