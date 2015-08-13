# Ripple

A simple-to-use media player with inspiration from Material Design

## Build Instructions

This project uses Gradle for it's build system, so Android Studio would be your best bet for easy compilation.
The download link for Android Studio can be found [here](http://developer.android.com/sdk/index.html).

After downloading Android Studio, open up the cloned repository and you should be ready to go, with a couple caveats:

1. You'll need to register for an SoundCloud API key [here](http://soundcloud.com/you/apps/new).
2. After getting your client id and secret, place them in your strings.xml as follows:

```
<resources>
    <string name="soundcloud_client_id" translatable="false">YOUR_CLIENT_ID</string>
    <string name="soundcloud_client_secret" translatable="false">YOUR_CLIENT_SECRET</string>
</resources>
```
3. You should be good to go, build away!

## License
```
Copyright 2015 Michael Limb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
