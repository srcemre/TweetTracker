package com.emresarac.tweettracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.emresarac.tweettracker.R.color.colorGreen;
import static com.emresarac.tweettracker.R.color.colorOrange;

public class TrackerActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiver;

    private boolean BUTTON_CLICK_FLAG = true;

    private Button    btnLogout, btnTrackProfile;
    private TextView  tvUsername, tvServiceInfo;
    private ImageView ivUserProfilePic;
    private ListView  lvTweetsList;
    private EditText  etProfileName;

    List<String> tweetList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        tvUsername    = findViewById(R.id.tv_username);
        tvServiceInfo = findViewById(R.id.tv_serviceInfo);
        lvTweetsList  = findViewById(R.id.lv_tweetslist);

        etProfileName    = findViewById(R.id.et_profilename);
        ivUserProfilePic = findViewById(R.id.iv_userprofilepicture);
        tvUsername.setText(UserAuthManager.getDefault().getUserName());
        Picasso.get().load(UserAuthManager.getDefault().getUserProfileImgUrl()).into(ivUserProfilePic);

        final Intent notificationServiceIntent = new Intent(TrackerActivity.this, TweetNotificationService.class);

        btnTrackProfile = findViewById(R.id.btn_trackprofile);
        btnTrackProfile.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                String username = parseUsername(etProfileName.getText().toString());
                if (!username.isEmpty()) {
                    if (btnClickStatus()) {
                        btnTrackProfile.setText("FINISH TRACKING");
                        tvServiceInfo.setText("SERVICE ONLINE");
                        tvServiceInfo.setTextColor(getResources().getColor(colorGreen));

                        notificationServiceIntent.putExtra("@username", username);
                        startService(notificationServiceIntent);
                    } else {
                        btnTrackProfile.setText("START TRACKING");
                        tvServiceInfo.setText("SERVICE OFFLINE");
                        etProfileName.setClickable(true);

                        tvServiceInfo.setTextColor(getResources().getColor(colorOrange));
                        stopService(notificationServiceIntent);
                    }
                } else
                    Toast.makeText(getApplicationContext(), "Geçerli @Username giriniz.", Toast.LENGTH_LONG).show();
            }
        });

        btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(notificationServiceIntent);
                logoutToTwitter();
            }
        });

        //TweetNotificationService'den gelen tweet verileri
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String username  = intent.getStringExtra(TweetNotificationService.SERVICE_USERNAME);
                String createdAt = intent.getStringExtra(TweetNotificationService.SERVICE_DATE);
                String text      = intent.getStringExtra(TweetNotificationService.SERVICE_MESSAGE);

                tweetList.add("@" + username + "\n" + createdAt + "\nTweet:" + text);
                ArrayAdapter<String> tweets = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_listview, R.id.textView, tweetList);
                lvTweetsList.setAdapter(tweets);
            }
        };
    }

    public void logoutToTwitter() {
        UserAuthManager.getDefault().logout();
        this.startActivity(new Intent(this, MainActivity.class));
    }

    public String parseUsername(String str) {
        str = str.replace("@", "");
        return str;
    }

    public boolean btnClickStatus() {
        if (BUTTON_CLICK_FLAG) {
            BUTTON_CLICK_FLAG = false;
            return true;
        } else {
            BUTTON_CLICK_FLAG = true;
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(TweetNotificationService.SERVICE_RESULT));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }



}