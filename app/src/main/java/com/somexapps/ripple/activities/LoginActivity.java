package com.somexapps.ripple.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.somexapps.ripple.R;
import com.somexapps.ripple.api.LoginService;
import com.somexapps.ripple.api.AccessTokenResult;
import com.somexapps.ripple.models.AccessToken;
import com.somexapps.ripple.services.ServiceGenerator;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

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

    /**
     * Used for storing the parcelable AccessTokenResult when logged in with SoundCloud.
     */
    public static final String EXTRA_ACCESS_TOKEN_BUNDLE = "access_token_bundle";

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

                        // Post success
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setTitle("Login Successful")
                                        .setMessage("Access Token: " + accessTokenResult.getAccessToken()
                                                + "\nScope: " + accessTokenResult.getScope())
                                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
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
                                        })
                                        .show();
                            }
                        });
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
