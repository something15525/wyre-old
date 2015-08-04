package com.somexapps.ripple.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.somexapps.ripple.R;
import com.somexapps.ripple.adapters.SongGridAdapter;
import com.somexapps.ripple.models.AccessToken;
import com.somexapps.ripple.api.LoginService;
import com.somexapps.ripple.models.Song;
import com.somexapps.ripple.services.MediaService;
import com.somexapps.ripple.services.ServiceGenerator;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

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
public class MainActivity extends AppCompatActivity {
    /**
     * Begin view bindings
     */
    @Bind(R.id.activity_main_grid_view) GridView mSongGrid;
    @Bind(R.id.activity_main_test_url_call) Button mGetUrlButton;
    /**
     * End view bindings
     */

    private final String redirectUri = "your://redirecturi";

    /**
     * Holder for the list of songs.
     */
    private ArrayList<Song> mSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        ButterKnife.bind(this);

        // Refresh list of music
        refreshMusic();

        // Set up adapter and attach
        final SongGridAdapter adapter = new SongGridAdapter(this, mSongs);
        mSongGrid.setAdapter(adapter);

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

        mGetUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(LoginService.LOGIN_URL +
                                "connect?client_id=" + getString(R.string.soundcloud_client_id) +
                                "&client_secret=" + getString(R.string.soundcloud_client_secret) +
                                "&redirect_uri=" + redirectUri +
                                "&response_type=code" +
                                "&display=popup")
                );

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Used to grab URI from Oauth2 login
        Uri uri = getIntent().getData();

        if (uri != null && uri.toString().startsWith(redirectUri)) {
            // Grab the auth code
            final String code = uri.getQueryParameter("code");

            // Make sure we got a code
            if (code != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Get the access token
                        LoginService loginService = ServiceGenerator.createService(
                                LoginService.class, LoginService.BASE_URL,
                                getString(R.string.soundcloud_client_id),
                                getString(R.string.soundcloud_client_secret));
                        final AccessToken accessToken = loginService.getAccessToken(
                                getString(R.string.soundcloud_client_id),
                                getString(R.string.soundcloud_client_secret),
                                code, "authorization_code", redirectUri, "");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Login Successful")
                                        .setMessage("Access Token: " + accessToken.getAccessToken()
                                                + "\nScope: " + accessToken.getScope())
                                        .setNeutralButton(android.R.string.ok, null)
                                        .show();
                            }
                        });
                    }
                }).start();
            } else if (uri.getQueryParameter("error") != null) {
                // TODO: Log out error
            }
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
