package hr.tvz.android.podcastclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.ArrayList;

import hr.tvz.android.podcastclient.Model.Episode;
import hr.tvz.android.podcastclient.Model.EpisodeHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EpisodesActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private EpisodesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefresh;
    private String mQuery;
    private ArrayList<Episode> mData;

    private BroadcastReceiver refreshReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.puljic.MP_REFRESH".equals(intent.getAction())) {
                String id = intent.getStringExtra("id");
                mAdapter.notifyDataSetChanged();
                for(Episode ep:mData){
                    if(id.equals(ep.getId())){
                        ep.mPlaying=false;
                        mAdapter.notifyDataSetChanged();
                    }
                }
                Log.e("DATASET", "REFRESHED");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodes);

        IntentFilter filterRefresh = new IntentFilter("com.puljic.MP_REFRESH");
        registerReceiver(refreshReciever, filterRefresh);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        mQuery = intent.getStringExtra("query");
        mData = new ArrayList<>();

        mSwipeRefresh = findViewById(R.id.swiperefresh_episodes);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateView();
            }
        });

        mRecyclerView = findViewById(R.id.recyclerView_episodes);
        mLayoutManager = new LinearLayoutManager(this);
        populateView();
    }

    public void populateView() {
        mSwipeRefresh.setRefreshing(true);

        mData.clear();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new EpisodesAdapter(mData);
        mRecyclerView.setAdapter(mAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://listen-api.listennotes.com/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ListenApi listenApi = retrofit.create(ListenApi.class);

        Call<EpisodeHolder> call = listenApi.getEpisodes(mQuery, "recent_first");

        call.enqueue(new Callback<EpisodeHolder>() {
            @Override
            public void onResponse(Call<EpisodeHolder> call, retrofit2.Response<EpisodeHolder> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
                    mSwipeRefresh.setRefreshing(false);
                    return;
                }

                EpisodeHolder data = response.body();
                mData = data.getEpisodes();

                if (mData.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No results found.", Toast.LENGTH_LONG).show();
                }

                for (Episode res : mData) {
                    res.descriptionOriginal = res.getDescription();
                    res.mPlaying = false;
                    res.reduceDescription();
                }

                mAdapter = new EpisodesAdapter(mData);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);
                mSwipeRefresh.setRefreshing(false);

                mAdapter.setOnItemClickListener(new EpisodesAdapter.OnItemClickListener() {

                    @Override
                    public void onItemClick(int position) {
                        if (!mData.get(position).mExpanded) {
                            mData.get(position).restoreDescription();
                        } else {
                            mData.get(position).reduceDescription();
                        }
                        mAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onPlay(int position) {
                        ModelAdapter<Episode> adapter = FlowManager.getModelAdapter(Episode.class);
                        ArrayList<Episode> fromBase = (ArrayList<Episode>) SQLite.select().from(Episode.class).queryList();
                        if (!mData.get(position).mPlaying) {
                            mData.get(position).mPlaying = true;

                            for(Episode ep : fromBase){
                                for(Episode dat:mData){
                                    if(dat.getId().equals(ep.getId())){
                                        dat.mPlaying=false;
                                    }
                                }
                                adapter.delete(ep);
                            }

                            adapter.insert(mData.get(position));
                            startService(mData.get(position).getAudio());
                        } else {
                            mData.get(position).mPlaying = false;
                            adapter.delete(mData.get(position));
                            stopService(mData.get(position).getAudio());
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onFailure(Call<EpisodeHolder> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(refreshReciever);

    }

    public void startService(String url){
        Intent serviceIntent = new Intent(this, PodcastService.class);
        //serviceIntent.putExtra("url", url);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService(String url){
        Intent serviceIntent = new Intent(this, PodcastService.class);
        stopService(serviceIntent);
    }
}
