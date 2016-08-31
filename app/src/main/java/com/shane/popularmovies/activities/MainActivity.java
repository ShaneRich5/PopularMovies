package com.shane.popularmovies.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shane.popularmovies.Movie;
import com.shane.popularmovies.R;
import com.shane.popularmovies.constants.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    @BindView(R.id.recycler_movies) RecyclerView moviesRecyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        loadMovies();
    }

    private void loadMovies() {
        final String url = generateUrl();
        Log.i(TAG, "url = " + url);

        Observable.create(new Observable.OnSubscribe<Response>() {
            @Override
            public void call(Subscriber<? super Response> subscriber) {
                try {
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
                    subscriber.onNext(response);
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        }).flatMap(response -> Observable.create(new Observable.OnSubscribe<List<Movie>>() {
            @Override
            public void call(Subscriber<? super List<Movie>> subscriber) {
                if(! response.isSuccessful())
                    subscriber.onError(new AssertionError("Response failed"));

                try {
                    final String RESULTS_NODE = "results";
                    final Gson gson = new Gson();

                    final ResponseBody responseBody = response.body();
                    final String serializedJsonResponse = responseBody.string();

                    final JsonObject jsonPayload = new JsonParser().parse(serializedJsonResponse).getAsJsonObject();
                    final JsonArray moviesAsJsonArray = jsonPayload.getAsJsonArray(RESULTS_NODE);

                    final List<Movie> movies = new ArrayList<>();

                    for (JsonElement movieElement : moviesAsJsonArray) {
                        final Movie movie = gson.fromJson(movieElement, Movie.class);
                        movies.add(movie);
                    }

                    subscriber.onNext(movies);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        }))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<List<Movie>>() {

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

            }
        }).unsubscribe();
    }

    private String generateUrl() {
        String sortOrder = loadSortOrderFromPreference();
        String apiKey = getString(R.string.themoviedb_api_key);

        return Constants.MOVIE_URL +
                "/3/movie/" + sortOrder +
                "?api_key=" + apiKey;
    }

    private String loadSortOrderFromPreference() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultSortOrder = getResources().getString(R.string.pref_sort_order_default);
        return preferences.getString(
                getString(R.string.pref_sort_order_key),
                defaultSortOrder);
    }
}
