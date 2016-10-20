package com.dasyel.dasyelwillems_pset6.movie_api.interfaces;

import com.dasyel.dasyelwillems_pset6.models.Movie;

/**
 * IdSearch is needed to get callbacks from OmdbApi on searchin movies by imdbID
 */
public interface IdSearch {

    // This method gets called when the movie data is retrieved from OMDB
    void receiveMovieObject(Movie movie, int RequestId);
}