package com.somexapps.ripple.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.somexapps.ripple.R;
import com.somexapps.ripple.models.Song;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Copyright 2014, 2015 Michael Limb, paulruiz
 * Code adapted from:
 * https://github.com/PaulTR/AndroidDemoProjects/blob/master/MediaSessionwithMediaStyleNotification/app/src/main/java/com/ptrprograms/mediasessionwithmediastylenotification/MediaPlayerService.java
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
public class MediaService extends Service implements MediaPlayer.OnPreparedListener {
    private final String TAG = MediaService.class.getSimpleName();

    public final static String ACTION_PLAY = "action_play";
    public final static String ACTION_PAUSE = "action_pause";
    public final static String ACTION_STOP = "action_stop";
    public final static String ACTION_NEXT = "action_next";
    public final static String ACTION_PREVIOUS = "action_previous";

    public final static String EXTRA_SONG_TO_PLAY = "song_to_play";
    public final static String EXTRA_SONG_LIST = "song_list";

    private final static String MEDIA_SESSION_TAG = "media_session_tag";
    private final static int MEDIA_NOTIFICATION_REQUEST_CODE = 1;

    /**
     * The list of songs in the queue.
     */
    private ArrayList<Song> songsList;

    /**
     * The data for the currently playing song.
     */
    private Song playingSong;

    /**
     * The index of the playing song in the list of songs
     */
    private int playingSongIndex;

    /**
     * Boolean to tell whether or not the MediaPlayer is paused.
     */
    private boolean isPaused = false;

    /**
     * The component name used by the media session for identifying the controlling class
     */
    private final static ComponentName mediaSessionComponentName = new ComponentName(
            "com.somexapps.ripple",
            "com.somexapps.ripple.services.MediaService"
    );

    private MediaControllerCompat mediaController;
    //private MediaSessionManager mediaSessionManager;

    /**
     * The session for the service
     */
    private MediaSessionCompat mediaSession;

    /**
     * The media player for the service.
     */
    private MediaPlayer mediaPlayer;

    /**
     * Audio manager, used for querying device state and such.
     */
    //private AudioManager audioManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Make sure our controls have been initialized
        if (mediaSession == null) {
            initMediaSessions();
        }

        // Pass the intent to handling function
        handleIntent(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaSessions() {
        // Initialize player
        mediaPlayer = new MediaPlayer();

        // Add listener to handle completion of song
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Send intent to play next song in MediaService list
                Intent nextSong = new Intent(getApplicationContext(), MediaService.class);
                nextSong.setAction(ACTION_NEXT);
                startService(nextSong);
            }
        });

        // Set up session and controller
        // Create the media session
        mediaSession = new MediaSessionCompat(
                this,
                MEDIA_SESSION_TAG,
                mediaSessionComponentName,
                null
        );

        // Set up controller
        try {
            mediaController = new MediaControllerCompat(
                    getApplicationContext(), mediaSession.getSessionToken());
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to set up media controller: " + e.getMessage());
        }

        // Set up callbacks for media session
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.d(TAG, "Playing media");
                buildNotification(generateAction(android.R.drawable.ic_media_pause,
                        getString(R.string.media_service_notification_pause), ACTION_PAUSE));
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d(TAG, "Pausing media");
                buildNotification(generateAction(android.R.drawable.ic_media_play,
                        getString(R.string.media_service_notification_play), ACTION_PLAY));
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.d(TAG, "Skipping to next song");

                // Update the notification with the next song info
                buildNotification(generateAction(android.R.drawable.ic_media_pause,
                        getString(R.string.media_service_notification_pause), ACTION_PAUSE));
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.d(TAG, "Skipping to previous song");

                // Update the notification with the previous song info
                buildNotification(generateAction(android.R.drawable.ic_media_pause,
                        getString(R.string.media_service_notification_pause), ACTION_PAUSE));
            }

            @Override
            public void onFastForward() {
                super.onFastForward();
            }

            @Override
            public void onRewind() {
                super.onRewind();
            }

            @Override
            public void onStop() {
                super.onStop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }

            @Override
            public void onSetRating(RatingCompat rating) {
                super.onSetRating(rating);
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            int songToPlay = -1;
            if (intent.getExtras() != null) {
                // Get list of songs
                songsList = intent.getParcelableArrayListExtra(EXTRA_SONG_LIST);

                // See if song to play was passed
                songToPlay = intent.getExtras().getInt(EXTRA_SONG_TO_PLAY, -1);
            }

            switch (intent.getAction()) {
                case ACTION_PLAY:
                    // Play the song
                    playSong(songToPlay);

                    // Notify transport controls
                    mediaController.getTransportControls().play();

                    break;
                case ACTION_STOP:
                    // Notify transport controls
                    mediaController.getTransportControls().stop();

                    break;
                case ACTION_PAUSE:
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isPaused = true;
                    }

                    // Notify transport controls
                    mediaController.getTransportControls().pause();

                    break;
                case ACTION_NEXT:
                    // Switch to next song if one exists
                    if (playingSongIndex + 1 < songsList.size()) {
                        // Go to next song in list
                        playingSongIndex++;

                        // Play the song
                        playSong(playingSongIndex);
                    }

                    // Notify transport controls
                    mediaController.getTransportControls().skipToNext();

                    break;
                case ACTION_PREVIOUS:
                    // Switch to previous song if one exists
                    if (playingSongIndex - 1 >= 0) {
                        // Go to previous song in list
                        playingSongIndex--;

                        // Play the song
                        playSong(playingSongIndex);
                    }

                    // Notify transport controls
                    mediaController.getTransportControls().skipToPrevious();

                    break;
            }
        }
    }

    private void playSong(int indexOfSongToPlay) {
        Song toPlay = null;
        if (indexOfSongToPlay != -1) {
            // Save the index
            playingSongIndex = indexOfSongToPlay;

            toPlay = songsList.get(indexOfSongToPlay);
        }

        if (toPlay != null) {
            // Make sure song is not paused and song we're trying to play is a different one
            if (!isPaused ||
                    !toPlay.getData().equals(playingSong.getData())) {
                // Get URI for media
                // Save playing song uri for later reference
                playingSong = toPlay;

                // Reset the media player if not null, otherwise recreate it
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                } else {
                    mediaPlayer = new MediaPlayer();
                }

                // Try to prepare the media for playing
                try {
                    // Set the data source from the grabbed URI
                    mediaPlayer.setDataSource(playingSong.getData());

                    // Prepare asynchronously, listener will get called after preparation
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    // TODO: Handle this error
                    // Print it to logcat
                    Log.e(TAG, e.getMessage());
                }
            } else {
                mediaPlayer.start();
                isPaused = false;
            }
        } else if (isPaused) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    private void buildNotification(android.support.v4.app.NotificationCompat.Action action) {
        // Initialize style
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();

        // Build intent/pending intent
        Intent intent = new Intent(getApplicationContext(), MediaService.class);
        intent.setAction(ACTION_STOP);
        // TODO: Should this have 0 as the flag?
        PendingIntent pendingIntent = PendingIntent.getService(
                getApplicationContext(),
                MEDIA_NOTIFICATION_REQUEST_CODE,
                intent,
                0
        );

        // Get notification manager
        final NotificationManagerCompat managerCompat =
                NotificationManagerCompat.from(getApplicationContext());

        // Create media notification
        final android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(playingSong.getTitle())
                .setContentText(playingSong.getArtist())
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        // Check to see if we have a next or previous song to play
        boolean previousDisabled = false;
        boolean nextDisabled = false;

        if (songsList.size() == 1) {
            previousDisabled = true;
            nextDisabled = true;
        } else if (playingSongIndex == 0) {
            previousDisabled = true;
        } else if (playingSongIndex == songsList.size() - 1) {
            nextDisabled = true;
        }

        // Add previous action if there is previous media
        if (!previousDisabled) {
            builder.addAction(generateAction(android.R.drawable.ic_media_previous,
                    getString(R.string.media_service_notification_prev), ACTION_PREVIOUS));
        }

        // Add play/pause action
        builder.addAction(action);

        // Add next action if there is next media
        if (!nextDisabled) {
            builder.addAction(generateAction(android.R.drawable.ic_media_next,
                    getString(R.string.media_service_notification_next), ACTION_NEXT));
        }

        // Show buttons based on state
        if (previousDisabled && nextDisabled) {
            style.setShowActionsInCompactView(0);
        } else if (previousDisabled || nextDisabled) {
            style.setShowActionsInCompactView(0, 1);
        } else {
            style.setShowActionsInCompactView(0, 1, 2);
        }

        /**
         * Check if we are going to a play state by checking to see if the middle
         * action is getting set to Pause
         */
        if (action.getTitle().equals(getString(R.string.media_service_notification_pause))) {
            builder.setOngoing(true);
        } else {
            builder.setOngoing(false);
        }

        // Check if we have a image path
        if (playingSong.getAlbumArtPath() != null &&
                playingSong.getAlbumArtPath().startsWith("http")) {
            // Set up image target
            Target iconTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // Set icon
                    builder.setLargeIcon(bitmap);

                    // Update notification
                    managerCompat.notify(MEDIA_NOTIFICATION_REQUEST_CODE, builder.build());
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    // do nothing
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // do nothing
                }
            };

            // Start the image load
            Picasso
                    .with(getApplicationContext())
                    .load(Uri.parse(playingSong.getAlbumArtPath()))
                    .into(iconTarget);
        }

        // Update the notification
        managerCompat.notify(MEDIA_NOTIFICATION_REQUEST_CODE, builder.build());
    }

    private android.support.v4.app.NotificationCompat.Action generateAction(
            int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(
                getApplicationContext(),
                MEDIA_NOTIFICATION_REQUEST_CODE,
                intent,
                0
        );

        return new android.support.v4.app.NotificationCompat.Action.Builder(
                icon, title, pendingIntent).build();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Play the song
        mediaPlayer.start();

        // Let transport controls know we're playing to build the notification
        mediaController.getTransportControls().play();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Release media session
        if (mediaSession != null) {
            mediaSession.release();
        }

        // Release media player
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Release the music player if it's created
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
