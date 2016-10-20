package com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces;

import com.dasyel.dasyelwillems_pset6.models.MovieList;

import java.util.ArrayList;

/**
 * This interface is needed to get callbacks from the FirebaseDbManager on
 * requesting movieList data
 */
public interface MovieListRequester {

    // This method gets called to give the empty lists with their respective names
    void receiveMovieLists(ArrayList<MovieList> movieListArrayList);

    // This method gets called whenever a movieList updates on the database
    void receiveMovieList(MovieList movieList);
}
