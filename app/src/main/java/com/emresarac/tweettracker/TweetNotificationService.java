package com.emresarac.tweettracker;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import static com.emresarac.tweettracker.NotificationChannel.CHANNEL_1_ID;

public class TweetNotificationService extends Service {

    /**
     *
     * Uygulamanın arkaplanda çalışması için bu servis oluşturuldu.
     * Girilen kullanıcı adına ait Twitter hesabının son tweetin getiriyor.
     * 2 Saniyede bir istek göndererek hesabın yeni tweet atıp atmadığı kontrol ediliyor.
     * Yeni tweet tespit edildiğinde servis telefona bildirim gönderiyor.
     *
     * **/

    //TrackerActivty'e veri göndermek için
    private LocalBroadcastManager localBroadcastManager;
    public final static String SERVICE_RESULT = "com.service.result";
    public final static String SERVICE_USERNAME = "com.service.username";
    public final static String SERVICE_DATE = "com.service.date";
    public final static String SERVICE_MESSAGE = "com.service.text";

    private NotificationManagerCompat notificationManager;
    private Context context;
    private Timer timer;

    private long sinceId;
    private String USERNAME;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = getApplicationContext();
        notificationManager = NotificationManagerCompat.from(context);
        USERNAME = intent.getStringExtra("@username"); //EditText verisi

        Toast.makeText(context, "Servis başlatıldı.", Toast.LENGTH_LONG).show();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                requestTweets();
            }
        }, 0, 2400);
        return START_STICKY;
    }

    public void requestTweets(){

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(getApplicationContext().getString(R.string.TWITTER_CONSUMER_KEY))
                .setOAuthConsumerSecret(getApplicationContext().getString(R.string.TWITTER_CONSUMER_SECRET))
                .setOAuthAccessToken(UserAuthManager.getDefault().getAccessToken().getToken())
                .setOAuthAccessTokenSecret(UserAuthManager.getDefault().getAccessToken().getTokenSecret());
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        //Son tweet'i çağırıyor.
        Paging paging = new Paging(1,1);
        if(sinceId > 0)
            paging.setSinceId(sinceId);

        try {
            List<Status> statuses = twitter.getUserTimeline(USERNAME,paging);
            for (Status status : statuses) {
                sinceId = status.getId();

                String username = status.getUser().getScreenName();
                String createdAt = status.getCreatedAt().toString();
                String text = status.getText();

                sendResult(username, createdAt, text);
                sendNotification(status.getCreatedAt().toString(), status.getText());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    //TrackerActivity'e verileri göndermek için
    private void sendResult(String username,String createdAt, String text) {
        Intent intent = new Intent(SERVICE_RESULT);
        if(username != null)
            intent.putExtra(SERVICE_USERNAME, username);
        if(createdAt != null)
            intent.putExtra(SERVICE_DATE, createdAt);
        if(text != null)
            intent.putExtra(SERVICE_MESSAGE, text);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void sendNotification(String title, String createdAt) {

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_finger_foreground)
                .setContentTitle("TTracker: "+title)
                .setContentText(createdAt)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(1, notification);

    }

    @Override
    public void onDestroy() {
        timer.cancel();
        Toast.makeText(context, "Servis durduruldu.", Toast.LENGTH_LONG).show();
    }

}
