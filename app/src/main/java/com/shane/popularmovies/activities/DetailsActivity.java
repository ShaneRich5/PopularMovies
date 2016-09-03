package com.shane.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shane.popularmovies.R;
import com.shane.popularmovies.constants.Constants;
import com.shane.popularmovies.exceptions.ApiException;
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
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.fab_favourite) FloatingActionButton favouriteFab;
    @BindView(R.id.text_overview) TextView overviewTextView;
    @BindView(R.id.image_poster) ImageView posterImageView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private OkHttpClient client = new OkHttpClient();

    private int lolCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String id = getMovieIdFromIntent();
        String url = generateUrl(id);
        loadMovieDataFromApi(url);
    }

    private String generateUrl(String id) {
        final String apiKey = getString(R.string.themoviedb_api_key);

        return Constants.MOVIE_URL + "/" + id +
                "?api_key=" + apiKey;
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
                Log.i(TAG, "movie:next");
                addMovieDataToViews(movie);
            }
        };
    }

    private void addMovieDataToViews(@NonNull Movie movie) {
        String posterUrl = Constants.IMAGE_URL + movie.getPosterPath();
        loadMoviePosterFromUrl(posterUrl);

        collapsingToolbarLayout.setTitle(movie.getTitle());
        overviewTextView.setText(movie.getOverview());
    }

    private void loadMoviePosterFromUrl(String posterUrl) {
        Glide.with(this).load(posterUrl)
                .thumbnail(0.5f).crossFade()
                .error(R.drawable.ic_error_outline_black_48dp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(posterImageView);
    }

    private Observable<Movie> convertResponseToMovieObservable(@NonNull Response response) {
        return Observable.create(subscriber -> {
            try {
                if (! response.isSuccessful()) throw new IOException();

                final Movie movie = extractMovieFromResponse(response);
                subscriber.onNext(movie);
                subscriber.onCompleted();
            } catch (IOException | ApiException e) {
                subscriber.onError(e);
            }
        });
    }

    private Movie extractMovieFromResponse(@NonNull Response response) throws IOException, ApiException {
        final String responseAsString = response.body().string();
        final Gson gson = new Gson();

        JsonObject jsonPayload = new JsonParser().parse(responseAsString).getAsJsonObject();

        if (jsonPayload.has(Constants.NODE_STATUS_CODE) && jsonPayload.has(Constants.NODE_STATUS_MESSAGE))
            extractErrorsFromJsonPayload(jsonPayload);

        return gson.fromJson(jsonPayload, Movie.class);
    }

    private void extractErrorsFromJsonPayload(JsonObject jsonPayload) throws ApiException {
        int statusCode = jsonPayload.get(Constants.NODE_STATUS_CODE).getAsInt();
        String statusMessage = jsonPayload.get(Constants.NODE_STATUS_MESSAGE).getAsString();

        Log.i(TAG, "status=" + statusCode + ", message=" + statusMessage);
        throw new ApiException(statusMessage);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
