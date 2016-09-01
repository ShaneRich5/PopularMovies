package com.shane.popularmovies.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Shane on 8/30/2016.
 */
public class Movie implements Serializable {
    private String id;
    @SerializedName("original_title")
    private String title;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("video_count")
    private int voteCount;
    private String overview;
    @SerializedName("vote_average")
    private int voteAverage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public int getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(int voteAverage) {
        this.voteAverage = voteAverage;
    }
}
