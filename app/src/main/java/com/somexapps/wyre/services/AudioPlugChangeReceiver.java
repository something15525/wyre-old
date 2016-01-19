package com.somexapps.wyre.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
public class AudioPlugChangeReceiver extends BroadcastReceiver {
    private static final String TAG = AudioPlugChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check for headset state
        if (intent.hasExtra("state")) {
            // Get state extra
            Bundle extras = intent.getExtras();
            int state = extras.getInt("state");

            // Check state
            switch (state) {
                case 0:
                    Log.d(TAG, "Headset unplugged.");

                    // Pause music
                    Intent pauseIntent = new Intent(context, MediaService.class);
                    pauseIntent.setAction(MediaService.ACTION_PAUSE);
                    context.startService(pauseIntent);

                    break;
                case 1:
                    Log.d(TAG, "Headset plugged in.");
                    break;
            }
        }
    }
}
