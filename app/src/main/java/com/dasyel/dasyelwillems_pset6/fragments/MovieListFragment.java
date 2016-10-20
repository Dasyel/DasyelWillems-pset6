package com.dasyel.dasyelwillems_pset6.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.dasyel.dasyelwillems_pset6.MainActivity;
import com.dasyel.dasyelwillems_pset6.R;
import com.dasyel.dasyelwillems_pset6.adapters.MovieListAdapter;
import com.dasyel.dasyelwillems_pset6.models.Movie;
import com.dasyel.dasyelwillems_pset6.models.MovieList;
import com.dasyel.dasyelwillems_pset6.movie_api.OmdbApi;
import com.dasyel.dasyelwillems_pset6.movie_api.interfaces.NameSearch;
import com.dasyel.dasyelwillems_pset6.persistence.SpManager;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.FirebaseDbManager;

import static com.dasyel.dasyelwillems_pset6.models.Contract.SEARCH_RESULTS_LIST;

public class MovieListFragment extends Fragment implements NameSearch{
    private MainActivity context;
    private MovieListAdapter movieListAdapter;
    private FirebaseDbManager firebaseDbManager;
    private MovieList movieList;
    private SpManager spManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (MainActivity) getActivity();
        spManager = SpManager.getInstance(context);
        firebaseDbManager = FirebaseDbManager.getInstance(context.user);

        // Remove the current movie so the app starts here instead of movieFragment
        spManager.setCurrentMovie("");

        // Find the current MovieList in the array of all MovieLists
        for(MovieList movieList: context.movieListArrayList){
            if(movieList.getName().equals(spManager.getCurrentList())){
                this.movieList = movieList;
            }
        }

        setHasOptionsMenu(true);

        // Set the title of the screen to the name of the list
        context.setActionBarTitle(movieList.getName());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_list, container, false);

        ListView listView = (ListView) v.findViewById(R.id.movie_list_view);
        movieListAdapter = new MovieListAdapter(context, context, movieList.getMovieArrayList());
        listView.setAdapter(movieListAdapter);

        // Set onClickListener for transition to Movie fragment
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Movie movie = (Movie) parent.getItemAtPosition(position);
                spManager.setCurrentMovie(movie.getId());
                MainActivity.showMovie();
            }
        });

        // The search result list doesn't need a delete possibility
        if(!movieList.getName().equals(SEARCH_RESULTS_LIST)) {
            // Set longClickListener for deletion of items from MovieList
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Movie movie = (Movie) parent.getItemAtPosition(position);
                    firebaseDbManager.removeMovie(spManager.getCurrentList(), movie.getId());
                    movieList.removeMovie(movie);
                    movieListAdapter.notifyDataSetChanged();
                    return true;
                }
            });
        }
        return v;
    }

    // Needed for sequential requests to OMDB
    @Override
    public int getBaseRequestId() {
        return 2000;
    }

    // This method gets called every time the OMDB request comes back with movies from the search
    @Override
    public void receiveMovieListObject(MovieList movieList, int requestId) {
        // We're done waiting so hide the progressDialog
        context.progressDialog.hide();

        // Safeguard in case the search is called on a non-search list
        if(!this.movieList.getName().equals(SEARCH_RESULTS_LIST)){
            return;
        }

        // If this is the first callback and no movies are returned: this is a failed search
        if(movieList.getMovieArrayList().size() == 0 && requestId == getBaseRequestId()){
            Toast t = Toast.makeText(context, R.string.error_no_results, Toast.LENGTH_SHORT);
            t.show();
            return;
        }

        // Clear the old search results if this is a new search
        if(requestId == getBaseRequestId()) {
            this.movieList.getMovieArrayList().clear();
        }

        // As long as we keep getting additional search results, keep adding and requesting more
        if(movieList.getMovieArrayList().size() != 0) {
            this.movieList.getMovieArrayList().addAll(movieList.getMovieArrayList());
            new OmdbApi().getMoviesByName(context.searchQuery, this, context, requestId + 1);
            this.movieListAdapter.notifyDataSetChanged();
        }
    }
}
