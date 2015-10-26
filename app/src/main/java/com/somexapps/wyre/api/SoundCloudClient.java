package com.somexapps.wyre.api;

import com.somexapps.wyre.api.activities.ActivitiesResult;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

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
public interface SoundCloudClient {
    /**
     * The base url for the SoundCloud API.
     */
    String BASE_URL = "https://api.soundcloud.com";

    /**
     * Grabs the SoundCloud user's information and save it in a result
     * @param oauthToken The authorization code to identify the user.
     * @param callback The method to be called once the result containing
     *                 the user's information is returned.
     */
    @GET("/me")
    void getUser(
            @Query("oauth_token") String oauthToken,
            Callback<SoundCloudUserResult> callback
    );

    @GET("/me/activities")
    void getActivities(
            @Query("oauth_token") String oauthToken,
            @Query("limit") int limit,
            Callback<ActivitiesResult> callback
    );
}
