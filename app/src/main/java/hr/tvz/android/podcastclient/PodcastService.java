package hr.tvz.android.podcastclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaSessionManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import hr.tvz.android.podcastclient.Model.Episode;

import static hr.tvz.android.podcastclient.Database.CHANNEL_ID;

public class PodcastService extends Service {
    private static final String MEDIA_PAUSE = "pause_playing";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";


    private MediaSessionManager mManager;
    private MediaSessionCompat mediaSession;
    private MediaController mController;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaPlayer mPlayer = null;
    private Episode mEpisode = null;


    private BroadcastReceiver playReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.puljic.MP_PLAY".equals(intent.getAction())) {
                Log.e("PLAY", "RECIEVED");
                resume();
            }
        }
    };

    private BroadcastReceiver pauseReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.puljic.MP_PAUSE".equals(intent.getAction())) {
                Log.e("PAUSE", "RECIEVED");
                pause();
            }
        }
    };

    private BroadcastReceiver stopReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.puljic.MP_STOP".equals(intent.getAction())) {
                Log.e("STOP", "RECIEVED");
                stop();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filterPlay = new IntentFilter("com.puljic.MP_PLAY");
        IntentFilter filterPause = new IntentFilter("com.puljic.MP_PAUSE");
        IntentFilter filterStop = new IntentFilter("com.puljic.MP_STOP");

        registerReceiver(playReciever, filterPlay);
        registerReceiver(pauseReciever, filterPause);
        registerReceiver(stopReciever, filterStop);
        //mReceiver = new MyReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mEpisode = SQLite.select().from(Episode.class).querySingle();

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                }
            });
        } else {
            stopPlayer();
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                }
            });
        }
        try {
            mPlayer.setDataSource(mEpisode.getAudio());
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mPlayer.start();
            }
        });


        Intent pauseIntent = new Intent("com.puljic.MP_PAUSE");
        //pauseIntent.putExtra("action", "pause");
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent("com.puljic.MP_PLAY");
        //pauseIntent.putExtra("action", "play");
        PendingIntent playPending = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent("com.puljic.MP_STOP");
        //pauseIntent.putExtra("action", "stop");
        PendingIntent stopPending = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Podcast Service")
                .setContentText(mEpisode.getTitle())
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(null)
                .addAction(R.drawable.ic_play, "Play", playPending)
                .addAction(R.drawable.ic_pause, "Pause", pausePending)
                .addAction(R.drawable.ic_stop, "Stop", stopPending)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .setSubText(mEpisode.getTitle())
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread
        //stopSelf();

        return START_NOT_STICKY;
    }


    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    public void resume() {
        if (mPlayer != null) {
            mPlayer.start();
        }
    }

    public void stop() {
        stopPlayer();
    }


    private void stopPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();

            ModelAdapter<Episode> adapter = FlowManager.getModelAdapter(Episode.class);
            adapter.delete(mEpisode);
            Intent refreshIntent = new Intent("com.puljic.MP_REFRESH");
            refreshIntent.putExtra("id", mEpisode.getId());
            sendBroadcast(refreshIntent);
            Log.e("REFRESH", "BROADCASTED");
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
        unregisterReceiver(playReciever);
        unregisterReceiver(pauseReciever);
        unregisterReceiver(stopReciever);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
