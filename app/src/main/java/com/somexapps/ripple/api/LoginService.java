package com.somexapps.ripple.api;

import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Copyright 2015 Michael Limb
 * Inspired from https://futurestud.io/blog/oauth-2-on-android-with-retrofit/
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
public interface LoginService {
    String LOGIN_URL = "https://soundcloud.com/";
    String BASE_URL = "https://api.soundcloud.com/";

    @POST("/oauth2/token")
    AccessTokenResult getAccessToken(@Query("client_id") String clientId,
                               @Query("client_secret") String clientSecret,
                               @Query("code") String code,
                               @Query("grant_type") String grantType,
                               @Query("redirect_uri") String redirectUri,
                               @Body String body);
}
