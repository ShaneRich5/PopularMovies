package com.shane.popularmovies.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.shane.popularmovies.R;
import com.shane.popularmovies.constants.Constants;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Response>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.v(TAG, "movies:onError", e);
            }

            @Override
            public void onNext(Response response) {
                if (!response.isSuccessful())
                    Log.e(TAG, "response unsuccessful");

                try {
                    String responseData = response.body().string();
                    Log.i(TAG, "response = " + responseData);
                } catch (IOException e) {
                    Log.e(TAG, "movies:io", e);
                }
            }
        });
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
