package com.shane.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

        String id = getMovieIdFromIntent();
        String url = Constants.MOVIE_URL + "/" + id;
        loadMovieDataFromApi(url);
    }

    @OnClick(R.id.fab_favourite)
    public void onFavouriteFabClicked(View v){
        String message;

        if (lolCounter <= 1)
            message = "Didn't finish yet :(";
        else if (lolCounter == 2)
            message = "Just wait nuh :/";
        else if (lolCounter == 3)
            message = "The sumn nah work -.-";
        else
            message = "Sigh...";

        lolCounter++;
        Snackbar.make(containerCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void loadMovieDataFromApi(@NonNull String url) {
        generateResultObservableFromUrl(url)
            .flatMap(this::convertResponseToMovieObservable)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(handleMovieSubscription());
    }

    private Subscriber<Movie> handleMovieSubscription() {
        return new Subscriber<Movie>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "movie:completed");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "movie:error", e);
            }

            @Override
            public void onNext(Movie movie) {
                Log.i(TAG, "movie:next " + movie.getTitle());
            }
        };
    }

    private Observable<Movie> convertResponseToMovieObservable(@NonNull Response response) {
        return Observable.create(subscriber -> {
            try {
                if (response.isSuccessful()) throw new IOException();

                final Movie movie = extractMovieFromResponse(response);
                subscriber.onNext(movie);
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    private Movie extractMovieFromResponse(@NonNull Response response) throws IOException {
        String responseAsString = response.body().string();
        Gson gson = new Gson();

        JsonObject jsonPayload = new JsonParser().parse(responseAsString).getAsJsonObject();
        return gson.fromJson(jsonPayload, Movie.class);
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

    private String getMovieIdFromIntent() throws AssertionError {
        final Intent movieIntent = getIntent();

        if (movieIntent != null && movieIntent.hasExtra(Constants.MOVIE_ID))
            return movieIntent.getStringExtra(Constants.MOVIE_ID);

        throw new AssertionError("Unable to find id");
    }
}
