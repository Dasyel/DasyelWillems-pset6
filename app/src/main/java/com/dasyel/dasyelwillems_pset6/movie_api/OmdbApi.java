package com.dasyel.dasyelwillems_pset6.movie_api;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dasyel.dasyelwillems_pset6.models.Movie;
import com.dasyel.dasyelwillems_pset6.models.MovieList;
import com.dasyel.dasyelwillems_pset6.movie_api.interfaces.IdSearch;
import com.dasyel.dasyelwillems_pset6.movie_api.interfaces.NameSearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.dasyel.dasyelwillems_pset6.models.Contract.SEARCH_RESULTS_LIST;

/**
 * This is a wrapper around the OMDB functionality
 */
public class OmdbApi {
    private static final String URL = "http://www.omdbapi.com/?<T>=<QUERY>&y=&plot=full&r=json";
    private static final String PAGE = "&page=";
    private static final String QUERY_KEYWORD = "<QUERY>";
    private static final String TYPE_KEYWORD = "<T>";
    private static final String ID_KEYWORD = "i";
    private static final String SEARCH_KEYWORD = "s";
    private static final String ERROR_MSG = "Something went wrong, please try again";

    // Request movies by a search query, this will callback whenever data is received
    public void getMoviesByName(String query, final NameSearch ns, final Context context,
                                final int requestId) {
        RequestManager.getInstance(context).getRequestQueue().cancelAll(context);
        String url;
        if(requestId > ns.getBaseRequestId()){
            url = queryFixer(query, SEARCH_KEYWORD, requestId - ns.getBaseRequestId());
        } else {
            url = queryFixer(query, SEARCH_KEYWORD);
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        MovieList movieList;
                        try {
                            if(response.getString("Response").equals("False")) {
                                ns.receiveMovieListObject(
                                        new MovieList(new ArrayList<Movie>(),
                                                SEARCH_RESULTS_LIST), requestId);
                                return;
                            }
                            JSONArray jsonArray = response.getJSONArray("Search");
                            movieList = new MovieList(jsonArray, SEARCH_RESULTS_LIST);
                        } catch (JSONException e){
                            Toast t = Toast.makeText(context, ERROR_MSG, Toast.LENGTH_SHORT);
                            t.show();
                            return;
                        }
                        ns.receiveMovieListObject(movieList, requestId);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast t = Toast.makeText(context, ERROR_MSG, Toast.LENGTH_SHORT);
                        t.show();
                    }
                });

        RequestManager.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    // Request a movie by its imdbID, this will callback when the data comes in
    public void getMovieByID(String query, final IdSearch is, final Context context,
                             final int requestId) {
        RequestManager.getInstance(context).getRequestQueue().cancelAll(context);
        String url = queryFixer(query, ID_KEYWORD);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("Response").equals("False")){
                                throw new JSONException("");
                            }
                        } catch (JSONException e){
                            Toast t = Toast.makeText(context, ERROR_MSG, Toast.LENGTH_SHORT);
                            t.show();
                            return;
                        }

                        Movie m = new Movie(response);
                        is.receiveMovieObject(m, requestId);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast t = Toast.makeText(context, ERROR_MSG, Toast.LENGTH_SHORT);
                        t.show();
                    }
                });

        RequestManager.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    // Helper function to create the needed url for a query
    private static String queryFixer(String query, String type, int page){
        StringBuilder sb = new StringBuilder(URL);
        String new_query = "";
        String[] queryArray = query.trim().split(" ");
        if(queryArray.length > 1) {
            for (String word : query.split(" ")) new_query += word + "+";
            new_query = new_query.substring(0, new_query.length()-1);
        } else {
            new_query = query.trim();
        }
        int start = sb.indexOf(QUERY_KEYWORD);
        int end = start + QUERY_KEYWORD.length();
        sb.replace(start, end, new_query);
        start = sb.indexOf(TYPE_KEYWORD);
        end = start + TYPE_KEYWORD.length();
        sb.replace(start, end, type);
        if(page > 0){
            sb.append(PAGE).append(page+1);
        }
        return sb.toString();
    }

    private static String queryFixer(String query, String type){
        return queryFixer(query, type, 0);
    }
}
