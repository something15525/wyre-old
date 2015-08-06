package com.somexapps.ripple.services;

import android.util.Base64;

import com.somexapps.ripple.api.AccessTokenResult;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Copyright 2015 Michael Limb, Marcus Pöhls
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
public class ServiceGenerator {
    private ServiceGenerator() {
        // don't instantiate this class
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        return createService(serviceClass, baseUrl, null, null);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl,
                                      String username, String password) {
        // Create builder for rest adapter with baseUrl and client
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setClient(new OkClient(new OkHttpClient()));

        // Check if username and password were passed
        if (username != null && password != null) {
            final String credentials = username + ":" + password;

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    String encoded = "Basic " + Base64.encodeToString(credentials.getBytes(),
                            Base64.NO_WRAP);

                    request.addHeader("Accept", "application/json");
                    request.addHeader("Authorization", encoded);
                }
            });
        }

        // Create adapter from builder
        RestAdapter adapter = builder.build();

        // Create new adapter with the passed class as the interface.
        return adapter.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl,
                                      final AccessTokenResult accessToken) {
        // Create builder for rest adapter with baseUrl and client
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setClient(new OkClient(new OkHttpClient()));

        // Check if username and password were passed
        if (accessToken != null) {
            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Accept", "application/json");
                    request.addHeader("Authorization",
                            accessToken.getScope() + " " + accessToken.getAccessToken());
                }
            });
        }

        // Create adapter from builder
        RestAdapter adapter = builder.build();

        // Create new adapter with the passed class as the interface.
        return adapter.create(serviceClass);
    }
}
