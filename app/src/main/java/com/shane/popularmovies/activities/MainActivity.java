package com.shane.popularmovies.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shane.popularmovies.R;
import com.shane.popularmovies.adapters.MovieAdapter;
import com.shane.popularmovies.constants.Constants;
import com.shane.popularmovies.models.Movie;
import com.shane.popularmovies.views.recyclers.EndlessRecyclerOnScrollListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();

    public static final String STATE_CURRENT_PAGE = "current_page";
    public static final String STATE_MOVIE_ID_LIST = "movie_id_list";

    @BindView(R.id.recycler_movies) RecyclerView moviesRecyclerView;
    @BindView(R.id.fab_scroll_reset) FloatingActionButton scrollResetFab;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private final OkHttpClient client = new OkHttpClient();
    private MovieAdapter movieAdapter;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupLayouts();

        if (savedInstanceState != null) restoreData(savedInstanceState);

        loadMovies(currentPage);
    }

    private void restoreData(Bundle inState) {
        currentPage = inState.getInt(STATE_CURRENT_PAGE);

        if (currentPage > 1)
            for (int page = 1; page < currentPage; page++)
                loadMovies(page);
    }

    private void setupLayouts() {
        setSupportActionBar(toolbar);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        moviesRecyclerView.setLayoutManager(gridLayoutManager);

        movieAdapter = new MovieAdapter(this);
        moviesRecyclerView.setAdapter(movieAdapter);

        moviesRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                loadMovies(currentPage);
                MainActivity.this.currentPage = currentPage;
            }
        });
    }

    @OnClick(R.id.fab_scroll_reset)
    public void onFabScrollResetClick(View v) {
        moviesRecyclerView.getLayoutManager().scrollToPosition(0);
    }

    private void loadMovies(int pageNumber) {
        final String url = generateUrl(pageNumber);
        Log.i(TAG, "url = " + url);

        generateResultObservableFromUrl(url)
                .flatMap(this::convertResponseToMovieList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleMoveListSubscription());
    }

    private Subscriber<List<Movie>> handleMoveListSubscription() {
        return new Subscriber<List<Movie>>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "moviesLoaded:completed");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "moviesLoaded:error", e);
            }

            @Override
            public void onNext(List<Movie> movies) {
                movieAdapter.addMovies(movies);
            }
        };
    }

    private Observable<List<Movie>> convertResponseToMovieList(@NonNull Response response) {
        return Observable.create(subscriber -> {
            if (!response.isSuccessful())
                subscriber.onError(new AssertionError("Response failed"));
            try {
                final List<Movie> movies = extractMoviesFromResponse(response);
                subscriber.onNext(movies);
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    private Observable<Response> generateResultObservableFromUrl(@NonNull String url) {
        return Observable.create(subscriber -> {
                try {
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
                    subscriber.onNext(response);
                } catch (IOException e) {
                    subscriber.onError(e);
                }
        });
    }

    private List<Movie> extractMoviesFromResponse(@NonNull Response response) throws IOException {
        List<Movie> moviesList = new ArrayList<>();

        final String RESULTS_NODE = "results";
        final Gson gson = new Gson();

        final ResponseBody responseBody = response.body();
        final String serializedJsonResponse = responseBody.string();

        final JsonObject jsonPayload = new JsonParser().parse(serializedJsonResponse).getAsJsonObject();
        final JsonArray moviesAsJsonArray = jsonPayload.getAsJsonArray(RESULTS_NODE);


        for (JsonElement movieElement : moviesAsJsonArray) {
            final Movie movie = gson.fromJson(movieElement, Movie.class);
            moviesList.add(movie);
        }

        return moviesList;
    }

    private String generateUrl(int page) {
        final String sortOrder = loadSortOrderFromPreference();
        final String apiKey = getString(R.string.themoviedb_api_key);

        return Constants.MOVIE_URL + "/" + sortOrder +
                "?api_key=" + apiKey +
                "&page=" + page;
    }

    private String loadSortOrderFromPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultSortOrder = getResources().getString(R.string.pref_sort_order_default);
        return preferences.getString(
                getString(R.string.pref_sort_order_key),
                defaultSortOrder);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<Movie> moviesList = movieAdapter.getMovies();
        ArrayList<String> movieIds = transformMoviesToIds(moviesList);

        outState.putInt(STATE_CURRENT_PAGE, currentPage);
        outState.putStringArrayList(STATE_MOVIE_ID_LIST, movieIds);
    }

    private ArrayList<String> transformMoviesToIds(List<Movie> moviesList) {
        ArrayList<String> ids = new ArrayList<>();

        for (Movie movie : moviesList) {
            ids.add(movie.getId());
        }

        return ids;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPage = savedInstanceState.getInt(STATE_CURRENT_PAGE);

        // TODO : use this to load old movies instead of reloading the entire list
        List<String> ids = savedInstanceState.getStringArrayList(STATE_MOVIE_ID_LIST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:

                return true;
            case R.id.action_setting:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
