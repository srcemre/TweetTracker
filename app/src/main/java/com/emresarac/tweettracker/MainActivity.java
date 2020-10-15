
package com.emresarac.tweettracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity {

    //Twitter oauth urls
    private final static String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    private final static String TWITTER_CALLBACK_URL = "scheme://t4jsample";
    private final static String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

    //Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;
    private static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);

        final Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginToTwitter();
            }
        });

        if (!isTwitterLoggedInAlready())
            getUserAccessToken();

    }

    private void getUserAccessToken() {

        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
            String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
            try {
                //User bilgilerini ve Access Token bilgilerini saklıyor
                UserAuthManager.getDefault().setAccessTokens(twitter.getOAuthAccessToken(requestToken, verifier));
                UserAuthManager.getDefault().setUserInfos(twitter);

                this.startActivity(new Intent(MainActivity.this, TrackerActivity.class));

            } catch (Exception e) {
                Log.e("Twitter Login Error", "E: " + e.getMessage());
            }
        }
    }

    private void loginToTwitter() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(this.getString(R.string.TWITTER_CONSUMER_KEY));
            builder.setOAuthConsumerSecret(this.getString(R.string.TWITTER_CONSUMER_SECRET));
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            Log.d("TwitterLoginObject", twitter + "");

            try {
                requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isTwitterLoggedInAlready() {
        return sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }
}




