package com.dasyel.dasyelwillems_pset6.movie_api.interfaces;

import com.dasyel.dasyelwillems_pset6.models.MovieList;

/**
 * NameSearch is needed to get callbacks from OmdbApi when the search function is used
 */
public interface NameSearch {
    // This id is used to handle sequential calls for data
    int getBaseRequestId();

    // This method is called whenever new search results come in
    void receiveMovieListObject(MovieList movieList, int requestId);
}
