package com.shane.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shane.popularmovies.Movie;
import com.shane.popularmovies.R;
import com.shane.popularmovies.activities.DetailsActivity;
import com.shane.popularmovies.constants.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Shane on 8/30/2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    public static final String TAG = MovieAdapter.class.getName();

    private List<Movie> movies;
    private Context context;

    public MovieAdapter(Context context) {
        this.context = context;
        movies = new ArrayList<>();
    }

    public void addMovies(List<Movie> newMovies) {
        movies.addAll(newMovies);
        notifyDataSetChanged();
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cell_movie, parent, false);
        return new MovieViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        final Movie movie = movies.get(position);
        String posterImagePath = Constants.IMAGE_URL + movie.getPosterPath();
        loadMoviePosterIntoView(posterImagePath, holder);
        addOnClickListenerToMovie(movie, holder);
    }

    private void addOnClickListenerToMovie(Movie movie, MovieViewHolder holder) {
        holder.frameLayoutContainer.setOnClickListener(view -> {
            Intent movieDetailsIntent = new Intent(context, DetailsActivity.class);
            movieDetailsIntent.putExtra(Constants.MOVIE_ID, movie.getId());
            context.startActivity(movieDetailsIntent);
        });
    }

    private void loadMoviePosterIntoView(String posterImagePath, MovieViewHolder holder) {
        Glide.with(context).load(posterImagePath)
                .thumbnail(0.5f).crossFade()
                .error(R.drawable.ic_error_outline_black_48dp)
                .placeholder(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.posterImageView);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.frame_container) FrameLayout frameLayoutContainer;
        @BindView(R.id.image_poster) ImageView posterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
