package com.dasyel.dasyelwillems_pset6.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.dasyel.dasyelwillems_pset6.MainActivity;
import com.dasyel.dasyelwillems_pset6.R;
import com.dasyel.dasyelwillems_pset6.models.Friend;
import com.dasyel.dasyelwillems_pset6.models.Movie;
import com.dasyel.dasyelwillems_pset6.models.MovieList;
import com.dasyel.dasyelwillems_pset6.movie_api.OmdbApi;
import com.dasyel.dasyelwillems_pset6.movie_api.RequestManager;
import com.dasyel.dasyelwillems_pset6.movie_api.interfaces.IdSearch;
import com.dasyel.dasyelwillems_pset6.persistence.SpManager;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.FirebaseDbManager;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces.FriendListRequester;

import java.util.ArrayList;

import static com.dasyel.dasyelwillems_pset6.models.Contract.SEARCH_RESULTS_LIST;
import static com.dasyel.dasyelwillems_pset6.models.Contract.SUGGESTIONS_LIST;

/**
 * Fragment to show the details of a movie and handles the adding and suggestions of movies
 */
public class MovieFragment extends Fragment implements IdSearch, FriendListRequester{
    private Movie movie;
    private View view;
    private FirebaseDbManager firebaseDbManager;
    private MainActivity context;
    private ArrayList<Friend> friendArrayList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (MainActivity) getActivity();
        SpManager spManager = SpManager.getInstance(context);
        firebaseDbManager = FirebaseDbManager.getInstance(context.user);
        friendArrayList = new ArrayList<>();

        String movieId = spManager.getCurrentMovie();

        // Request the movie from OMDB
        OmdbApi omdbApi = new OmdbApi();
        omdbApi.getMovieByID(movieId, this, context, 3000);

        // Start listening on friends list updates, which are needed for the add to list choices
        firebaseDbManager.getFriends(this);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_movie, container, false);
        final Button suggestButton = (Button) view.findViewById(R.id.suggest_button);
        final Button addToListButton = (Button) view.findViewById(R.id.to_list_button);

        // Set the onClickListener for the suggest button
        suggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieFragment.this.suggestMovie(v);
            }
        });

        // Set the onClickListener for the suggest button
        addToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieFragment.this.addMovieToList(v);
            }
        });
        return view;
    }

    // This method gets called when the OMDB request comes back with the movie
    @Override
    public void receiveMovieObject(Movie movie, int RequestId) {
        this.movie = movie;
        context.currentMovie = movie;
        context.setActionBarTitle(movie.getTitle());
        setTextViews();
        setPicture();
    }

    // Fill the textViews with the movie data
    public void setTextViews() {
        TextView title_tv = (TextView) view.findViewById(R.id.title_tv);
        TextView actors_tv = (TextView) view.findViewById(R.id.actor_tv);
        TextView description_tv = (TextView) view.findViewById(R.id.description_tv);

        title_tv.setText(movie.getTitle());
        actors_tv.setText(movie.getActors());
        description_tv.setText(movie.getPlot());
    }

    // Set the poster thumbnail
    public void setPicture() {
        final NetworkImageView iv = (NetworkImageView) view.findViewById(R.id.poster_iv);
        String url = movie.getPoster();
        ImageLoader il = RequestManager.getInstance(context).getImageLoader();
        iv.setImageUrl(url, il);
    }

    // This method gets called whenever the suggest button is pressed
    // It shows a popup of friends and adds the movie to the suggestions list of that friend
    public void suggestMovie(View v){
        // Create the options
        CharSequence friends[] = new CharSequence[friendArrayList.size()];
        for(int i = 0; i < friendArrayList.size(); i++){
            String friendName = friendArrayList.get(i).getName();
            friends[i] = friendName;
        }

        // Create the popup
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add to List");

        // Handle the selected option
        builder.setItems(friends, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                String friendUid = friendArrayList.get(index).getUid();
                firebaseDbManager.suggestMovie(friendUid, context.currentMovie);
            }
        });

        // Actually show the popup
        builder.show();
    }

    // This method is called whenever the add to watchlist button is pressed
    // It gives the movieLists without the suggestions and search-results lists as options.
    // It adds the current movie to the chosen movieList
    public void addMovieToList(View v){
        final ArrayList<MovieList> movieListArrayList = context.movieListArrayList;
        ArrayList<String> namesArrayList = new ArrayList<>();

        // Find the indices of the search and suggestions lists
        // And create the list of options
        int searchIndex = -1;
        int suggestionsIndex = -1;
        for(int i = 0; i < movieListArrayList.size(); i++){
            String listName = movieListArrayList.get(i).getName();
            if(listName.equals(SEARCH_RESULTS_LIST) ){
                searchIndex = i;
                continue;
            } else if (listName.equals(SUGGESTIONS_LIST)){
                suggestionsIndex = i;
                continue;
            }
            namesArrayList.add(listName);
        }
        CharSequence lists[] = namesArrayList.toArray(new CharSequence[movieListArrayList.size()-2]);
        final int searchIndex1 = searchIndex;
        final int suggestionsIndex1 = suggestionsIndex;

        // Build the popup and handle the selected option
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.heading_add_to_list);
        builder.setItems(lists, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                if(index>= Math.min(searchIndex1, suggestionsIndex1)){
                    index += 1;
                }
                if(index >= Math.max(searchIndex1, suggestionsIndex1)){
                    index += 1;
                }
                MovieList movieList = movieListArrayList.get(index);
                firebaseDbManager.saveMovie(movieList.getName(), movie);
            }
        });
        builder.show();
    }

    // This method gets called whenever the friends list gets updated on the database
    @Override
    public void receiveFriendListObject(ArrayList<Friend> friendArrayList) {
        this.friendArrayList.clear();
        this.friendArrayList.addAll(friendArrayList);
    }
}
