package com.dasyel.dasyelwillems_pset6.models;

import com.google.firebase.database.Exclude;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * MovieLists represent a list of movies with a name
 */
public class MovieList {
    private ArrayList<Movie> movieArrayList;
    private String name;
    private HashSet<String> idSet;

    // Empty constructor for firebase functionality
    public MovieList(){}

    // Json constructor for OMDB functionality
    public MovieList(JSONArray jsonArray, String name){
        this.name = name;
        movieArrayList = new ArrayList<>();
        idSet = new HashSet<>();
        for(int i = 0; i < jsonArray.length(); i++){
            Movie movie;
            try{
                movie = new Movie(jsonArray.getJSONObject(i));
            } catch (JSONException ignored){
                continue;
            }
            // Only add series and movies
            if(movie.getType().equals("movie") || movie.getType().equals("series")) {
                this.addMovie(movie);
            }
        }
    }

    // Normal constructor for manual instantiation
    public MovieList(ArrayList<Movie> movieArrayList, String name){
        if(movieArrayList == null){
            movieArrayList = new ArrayList<>();
        }
        this.movieArrayList = movieArrayList;
        this.name = name;
        this.idSet = new HashSet<>();
        for(Movie movie : movieArrayList){
            idSet.add(movie.getId());
        }
    }

    // ---------- START GETTERS ----------\\
    public String getName() {
        return name;
    }

    public ArrayList<Movie> getMovieArrayList() {
        return movieArrayList;
    }

    @Exclude
    public HashSet<String> getIdSet(){
        return idSet;
    }
    // ---------- END GETTERS ----------\\

    // Add a movie to the list
    public void addMovie(Movie movie){
        if(this.contains(movie)){
            return;
        }
        this.movieArrayList.add(movie);
        this.idSet.add(movie.getId());
    }

    // Remove a movie from the list
    public void removeMovie(Movie movie){
        if(!this.contains(movie)){
            return;
        }
        this.movieArrayList.remove(movie);
        this.idSet.remove(movie.getId());
    }

    // Check if this MovieList contains a certain movie
    public boolean contains(Movie movie){
        return idSet.contains(movie.getId());
    }

    // Flush this movieList and refill it with updated data
    public void refresh(MovieList movieList){
        this.movieArrayList.clear();
        this.movieArrayList.addAll(movieList.getMovieArrayList());
        this.idSet.clear();
        this.idSet.addAll(movieList.getIdSet());
    }
}
