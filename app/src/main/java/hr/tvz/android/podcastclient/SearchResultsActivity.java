package hr.tvz.android.podcastclient;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.ArrayList;

import hr.tvz.android.podcastclient.Model.Response;
import hr.tvz.android.podcastclient.Model.Result;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchResultsActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private SearchResultsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefresh;

    private ArrayList<Result> data2;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        mQuery = intent.getStringExtra("query");

        data2 = new ArrayList<>();

        mSwipeRefresh = findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateView();
            }
        });

        mRecyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        populateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery=query;
                populateView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    public void populateView() {
        mSwipeRefresh.setRefreshing(true);

        data2.clear();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SearchResultsAdapter(data2);
        mRecyclerView.setAdapter(mAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://listen-api.listennotes.com/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ListenApi listenApi = retrofit.create(ListenApi.class);

        Call<Response> call = listenApi.getResults(mQuery, 0, "podcast");

        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Code: " + response.code(), Toast.LENGTH_LONG).show();
                    mSwipeRefresh.setRefreshing(false);
                    return;
                }

                Response data = response.body();
                data2 = data.getResults();

                if (data2.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No results found.", Toast.LENGTH_LONG).show();
                }

                for (Result res : data2) {
                    res.description = res.getDescription_original();
                    res.mBookmarked = false;
                    res.reduceDescription();
                }

                mAdapter = new SearchResultsAdapter(data2);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);
                mSwipeRefresh.setRefreshing(false);

                mAdapter.setOnItemClickListener(new SearchResultsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        if (!data2.get(position).mExpanded) {
                            data2.get(position).restoreDescription();
                        } else {
                            data2.get(position).reduceDescription();
                        }
                        mAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onBookmarkClick(int position) {
                        ModelAdapter<Result> adapter = FlowManager.getModelAdapter(Result.class);
                        if (!data2.get(position).mBookmarked) {
                            data2.get(position).mBookmarked = true;
                            adapter.insert(data2.get(position));
                        } else {
                            data2.get(position).mBookmarked = false;
                            adapter.delete(data2.get(position));
                        }
                        mAdapter.notifyItemChanged(position);
                    }
                });

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }
}
