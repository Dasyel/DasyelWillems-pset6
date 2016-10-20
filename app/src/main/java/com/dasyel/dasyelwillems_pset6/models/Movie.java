package com.dasyel.dasyelwillems_pset6.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A movie object represents a movie with many details of the omdb api
 */
public class Movie {
    private String title, year, released, runtime, genre, director, writer, actors,
            plot, poster, rating, id, type;

    // Empty constructor for firebase functionality
    public Movie(){}

    // Json constructor for the OMDB API
    public Movie(JSONObject jsonObject){
        title = jsonGetStringOrEmpty(jsonObject, "Title");
        year = jsonGetStringOrEmpty(jsonObject, "Year");
        released = jsonGetStringOrEmpty(jsonObject, "Released");
        runtime = jsonGetStringOrEmpty(jsonObject, "Runtime");
        genre = jsonGetStringOrEmpty(jsonObject, "Genre");
        director = jsonGetStringOrEmpty(jsonObject, "Director");
        writer = jsonGetStringOrEmpty(jsonObject, "Writer");
        actors = jsonGetStringOrEmpty(jsonObject, "Actors");
        plot = jsonGetStringOrEmpty(jsonObject, "Plot");
        poster = jsonGetStringOrEmpty(jsonObject, "Poster");
        rating = jsonGetStringOrEmpty(jsonObject, "imdbRating");
        id = jsonGetStringOrEmpty(jsonObject, "imdbID");
        type = jsonGetStringOrEmpty(jsonObject, "Type");
    }

    // Normal constructor for possible manual instantiation
    public Movie(String title, String year, String released, String runtime,
                 String genre, String director, String writer, String actors, String plot,
                 String poster, String rating, String id, String type){
        this.title = title;
        this.year = year;
        this.released = released;
        this.runtime = runtime;
        this.genre = genre;
        this.director = director;
        this.writer = writer;
        this.actors = actors;
        this.plot = plot;
        this.poster = poster;
        this.rating = rating;
        this.id = id;
        this.type = type;
    }

    // ---------- START GETTERS ----------\\
    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getReleased() {
        return released;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getGenre() {
        return genre;
    }

    public String getDirector() {
        return director;
    }

    public String getWriter() {
        return writer;
    }

    public String getActors() {
        return actors;
    }

    public String getPlot() {
        return plot;
    }

    public String getPoster() {
        return poster;
    }

    public String getRating() {
        return rating;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    // ---------- END GETTERS ----------\\

    // Helper function to handle JsonExceptions
    private static String jsonGetStringOrEmpty(JSONObject jsonObject, String name){
        try {
            return jsonObject.getString(name);
        } catch (JSONException e){
            return "";
        }
    }
}
