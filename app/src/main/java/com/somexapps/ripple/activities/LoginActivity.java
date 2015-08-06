package com.somexapps.ripple.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.somexapps.ripple.R;
import com.somexapps.ripple.api.AccessTokenResult;
import com.somexapps.ripple.api.LoginService;
import com.somexapps.ripple.models.AccessToken;
import com.somexapps.ripple.services.ServiceGenerator;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

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
public class LoginActivity extends AppCompatActivity {
    /**
     * View bindings
     */
    @Bind(R.id.activity_login_toolbar) Toolbar toolbar;
    @Bind(R.id.activity_login_button) Button loginButton;
    @Bind(R.id.activity_logout_button) Button logoutButton;

    /**
     * Used for saving/retrieving the AccessToken.
     */
    private Realm realmInstance;

    /**
     * Used for redirection back to this activity from the WebView after authentication.
     */
    private final String redirectUri = "ripple://login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up layout
        setContentView(R.layout.activity_login);

        // Bind views
        ButterKnife.bind(this);

        // Set up toolbar
        setSupportActionBar(toolbar);

        // Set up up button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Open realm instance
        realmInstance = Realm.getInstance(this);

        // Query for AccessToken
        RealmQuery<AccessToken> accessQuery = realmInstance.where(AccessToken.class);
        RealmResults<AccessToken> accessResults = accessQuery.findAll();

        // If we have results, disable login and enable logout
        if (accessResults.size() > 0) {
            loginButton.setEnabled(false);
            logoutButton.setEnabled(true);
        } else {
            loginButton.setEnabled(true);
            logoutButton.setEnabled(false);
        }

        // Set up login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to oauth2 for SoundCloud
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(LoginService.LOGIN_URL +
                                "connect?client_id=" + getString(R.string.soundcloud_client_id) +
                                "&client_secret=" + getString(R.string.soundcloud_client_secret) +
                                "&redirect_uri=" + redirectUri +
                                "&response_type=code" +
                                "&display=popup")
                );

                // Start intent and finish current activity
                startActivity(intent);
                finish();
            }
        });

        // Set up logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get new instance of realm since it's not thread-safe
                Realm localRealmInstance = Realm.getInstance(LoginActivity.this);
                localRealmInstance.beginTransaction();

                RealmResults<AccessToken> results =
                        localRealmInstance.where(AccessToken.class).findAll();

                for (int i = 0; i < results.size(); i++) {
                    // Remove each from Realm
                    results.get(i).removeFromRealm();
                }

                // End transaction
                localRealmInstance.commitTransaction();

                // End activity
                finish();
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
                        final AccessTokenResult accessTokenResult = loginService.getAccessToken(
                                getString(R.string.soundcloud_client_id),
                                getString(R.string.soundcloud_client_secret),
                                code, "authorization_code", redirectUri, "");

                        // Open up db instance
                        Realm theRealm = Realm.getInstance(LoginActivity.this);
                        theRealm.beginTransaction();

                        // Create access token
                        AccessToken accessToken = new AccessToken(
                                accessTokenResult.getAccessToken(),
                                accessTokenResult.getScope()
                        );

                        // Copy to realm
                        theRealm.copyToRealmOrUpdate(accessToken);

                        // Finish transaction
                        theRealm.commitTransaction();
                        theRealm.close();

                        // Close activity
                        finish();
                    }
                }).start();
            } else if (uri.getQueryParameter("errors") != null) {
                // TODO: Log out error
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Handle pressing of home button
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
