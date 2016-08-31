package com.shane.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.shane.popularmovies.R;
import com.shane.popularmovies.constants.Constants;
import com.shane.popularmovies.models.Movie;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @BindView(R.id.coordinator_container) CoordinatorLayout containerCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab_favourite) FloatingActionButton favouriteFab;

    private OkHttpClient client = new OkHttpClient();

    private int lolCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        Log.i(TAG, "id=" + getMovieIdFromIntent());
//        loadMovieData();

    }

    @OnClick(R.id.fab_favourite)
    public void onFavouriteFabClicked(View v){
        String message;

        if (lolCounter <= 1)
            message = "Didnt finish yet :(";
        else if (lolCounter == 2)
            message = "Just wait nuh :/";
        else if (lolCounter == 3)
            message = "The sumn nah work -.-";
        else
            message = "Sigh...";

        Snackbar.make(containerCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
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
