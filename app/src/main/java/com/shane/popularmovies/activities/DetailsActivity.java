package com.shane.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.shane.popularmovies.Movie;
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
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DetailsActivity extends AppCompatActivity {
    public static final String TAG = DetailsActivity.class.getName();

    @BindView(R.id.toolbar) Toolbar toolbar;

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        Log.i(TAG, "id=" + getMovieIdFromIntent());
//        loadMovieData();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void loadMovieData() {
        String idEndpoint = "/" + getMovieIdFromIntent();

        Observable.create(new Observable.OnSubscribe<Response>() {
            @Override
            public void call(Subscriber<? super Response> subscriber) {
                try {
                    Request request = new Request.Builder().url(Constants.MOVIE_URL + idEndpoint).build();
                    Response response = client.newCall(request).execute();
                    subscriber.onNext(response);
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        })
        .flatMap((Func1<Response, Observable<Movie>>) response -> {
            Gson gson = new Gson();


            return null;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Movie>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Movie movie) {

            }
        });
    }

    private String getMovieIdFromIntent() throws AssertionError {
        final Intent movieIntent = getIntent();

        if (movieIntent != null && movieIntent.hasExtra(Constants.MOVIE_ID))
            return movieIntent.getStringExtra(Constants.MOVIE_ID);

        throw new AssertionError("Unable to find id");
    }
}
