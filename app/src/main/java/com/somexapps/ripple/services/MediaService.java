package com.somexapps.ripple.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.somexapps.ripple.R;

import java.io.IOException;

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

    public final static String EXTRA_MEDIA_URI = "media_uri";

    private final static String MEDIA_SESSION_TAG = "media_session_tag";
    private final static int MEDIA_NOTFICATION_REQUEST_CODE = 1;

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
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d(TAG, "Pausing media");
                buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
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
            switch (intent.getAction()) {
                case ACTION_PLAY:
                    // TODO: This will cause problems when trying to play other songs when paused.
                    if (!isPaused) {
                        // Get URI for media
                        // TODO: Check for extras
                        String mediaData = intent.getExtras().getString(EXTRA_MEDIA_URI);

                        // Reset the media player if not null, otherwise recreate it
                        if (mediaPlayer != null) {
                            mediaPlayer.reset();
                        } else {
                            mediaPlayer = new MediaPlayer();
                        }

                        // Try to prepare the media for playing
                        try {
                            // Set the data source from the grabbed URI
                            mediaPlayer.setDataSource(mediaData);

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
                    // Notify transport controls
                    mediaController.getTransportControls().skipToNext();

                    break;
                case ACTION_PREVIOUS:
                    // Notify transport controls
                    mediaController.getTransportControls().skipToPrevious();

                    break;
            }
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
                MEDIA_NOTFICATION_REQUEST_CODE,
                intent,
                0
        );

        // Create media notification
        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Song Title")
                .setContentText("Song Artist")
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        // Add button actions
        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.notify(MEDIA_NOTFICATION_REQUEST_CODE, builder.build());
    }

    private android.support.v4.app.NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(
                getApplicationContext(),
                MEDIA_NOTFICATION_REQUEST_CODE,
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

        // Set up the notification

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
