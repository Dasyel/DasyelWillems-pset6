package com.dasyel.dasyelwillems_pset6;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.dasyel.dasyelwillems_pset6.adapters.ListsAdapter;
import com.dasyel.dasyelwillems_pset6.fragments.FriendListFragment;
import com.dasyel.dasyelwillems_pset6.fragments.MovieFragment;
import com.dasyel.dasyelwillems_pset6.fragments.MovieListFragment;
import com.dasyel.dasyelwillems_pset6.models.Movie;
import com.dasyel.dasyelwillems_pset6.models.MovieList;
import com.dasyel.dasyelwillems_pset6.movie_api.OmdbApi;
import com.dasyel.dasyelwillems_pset6.persistence.SpManager;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.FirebaseDbManager;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces.MovieListRequester;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import static com.dasyel.dasyelwillems_pset6.models.Contract.*;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MovieListRequester {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDbManager firebaseDbManager;
    public FirebaseUser user;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    public ProgressDialog progressDialog;

    static FragmentManager fragmentManager;

    private ListsAdapter listsAdapter;
    public ArrayList<MovieList> movieListArrayList;

    private SpManager spManager;

    private static boolean viewingMovie;
    public String searchQuery;
    public Movie currentMovie;

    // ------------------------START ACTIVITY METHODS-------------------------- \\

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        spManager = SpManager.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Only initialize data if the user is authenticated
                    initializeData();
                }
            }
        };

        movieListArrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);

        // Set the burger button for the side drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setupDrawer();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    // Handles the backButton to either go back to list view from item view or to leave the app
    @Override
    public void onBackPressed() {
        if(spManager.getFriendListOpen()){
            spManager.setFriendListOpen(false);
            fragmentManager.popBackStack();
            if(viewingMovie){
                showMovie();
            } else {
                showMovieList();
            }
        } else if(viewingMovie){
            showMovieList();
        } else {
            super.onBackPressed();
        }
    }

    // ------------------------END ACTIVITY METHODS-------------------------- \\

    // ------------------------START SEARCH INTERFACE METHODS-------------------------- \\

    @Override
    public boolean onQueryTextSubmit(String query) {
        goToSearch(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    // ------------------------END SEARCH INTERFACE METHODS-------------------------- \\

    // ------------------------START OMDB INTERFACE METHODS-------------------------- \\

    @Override
    public void receiveMovieLists(ArrayList<MovieList> movieListArrayList) {
        this.movieListArrayList = movieListArrayList;
        addDrawerItems();

        if(spManager.getCurrentMovie().isEmpty()){
            showMovieList();
        } else {
            showMovie();
        }
    }

    @Override
    public void receiveMovieList(MovieList movieList) {
        for(MovieList ml : movieListArrayList){
            if(ml.getName().equals(movieList.getName())){
                ml.refresh(movieList);
            }
        }
    }

    // ------------------------END OMDB INTERFACE METHODS-------------------------- \\

    // ------------------------START BUTTON HANDLER METHODS-------------------------- \\

    // Logs out the user when the button is pressed in the side drawer
    public void logout(View v){
        mAuth.signOut();
    }

    // Transition method to open friendListFragment
    public void goToFriends(View v){
        FriendListFragment friendListFragment = new FriendListFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentHolder, friendListFragment)
                .commit();
        mDrawerLayout.closeDrawers();
    }

    // Handles the add Friend button in the friendListFragment
    public void addFriend(View v){
        EditText nameET = (EditText) findViewById(R.id.friend_name_input);
        EditText emailET = (EditText) findViewById(R.id.friend_email_input);
        String name = nameET.getText().toString();
        String email = emailET.getText().toString();

        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            emailET.setError("Required.");
            valid = false;
        } else {
            emailET.setError(null);
        }
        if (TextUtils.isEmpty(name)) {
            nameET.setError("Required.");
            valid = false;
        } else {
            nameET.setError(null);
        }
        if(valid) {
            emailET.setText("");
            nameET.setText("");
            firebaseDbManager.addFriend(this, email, name);
        }
    }

    // Adds a list with the specified name when the add button is pressed in side drawer
    public void addList(View view){
        EditText editText = (EditText) findViewById(R.id.list_name_et);
        final String name = editText.getText().toString();

        if(name.isEmpty()){
            editText.setError("Required.");
        } else {
            editText.setError(null);
        }

        editText.setText(""); // Clear the contents of the editText
        MovieList movieList = new MovieList(new ArrayList<Movie>(), name);
        firebaseDbManager.addMovieList(name);
        movieListArrayList.add(movieList);
        listsAdapter.notifyDataSetChanged();
    }

    // ------------------------END BUTTON HANDLER METHODS-------------------------- \\

    // ------------------------START INITIALIZER METHODS-------------------------- \\

    // Gets called after the user is authenticated
    private void initializeData(){
        firebaseDbManager = FirebaseDbManager.getInstance(user);
        firebaseDbManager.getMovieLists(this);
    }

    // Sets the side drawer contents and onClickListeners
    private void addDrawerItems(){
        ListView listView = (ListView) findViewById(R.id.navList);
        listsAdapter = new ListsAdapter(this, R.layout.snippet_drawer_list_entry, movieListArrayList);
        listView.setAdapter(listsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieList movieList = (MovieList) parent.getItemAtPosition(position);

                spManager.setCurrentList(movieList.getName());
                mDrawerLayout.closeDrawers(); // Close the side drawer
                MainActivity.showMovieList();
            }
        });

        // Set onClickListener for deletion of lists
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MovieList movieList = (MovieList) parent.getItemAtPosition(position);

                // Don't allow the defaultList and suggestionsList to be deleted
                if(movieList.getName().equals(DEFAULT_LIST) ||
                        movieList.getName().equals(SUGGESTIONS_LIST) ||
                        movieList.getName().equals(SEARCH_RESULTS_LIST)){
                    return true;
                }

                firebaseDbManager.removeMovieList(movieList.getName());
                movieListArrayList.remove(movieList);
                listsAdapter.notifyDataSetChanged();

                // If the deleted list was open, jump to default list
                String openListName = spManager.getCurrentList();
                if(movieList.getName().equals(openListName)) {
                    spManager.setCurrentList(DEFAULT_LIST);
                    mDrawerLayout.closeDrawers(); // Close the side drawer
                    MainActivity.showMovieList();
                }
                return true;
            }
        });
    }

    // Sets some values for the burger menu button
    private void setupDrawer(){
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    // Lets the fragments change the title of the actionBar when needed
    public void setActionBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    // ------------------------END INITIALIZER METHODS-------------------------- \\

    // ------------------------START TRANSITION METHODS-------------------------- \\

    // Loads the todoList fragment
    public static void showMovieList(){
        viewingMovie = false;
        MovieListFragment movieListFragment = new MovieListFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentHolder, movieListFragment)
                .commit();
    }

    // Loads the todoItem fragment
    public static void showMovie(){
        viewingMovie = true;
        MovieFragment movieFragment = new MovieFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentHolder, movieFragment)
                .commit();
    }

    // Calls for results on a search query from OMDB and opens the searchResultsList
    public void goToSearch(String query) {
        // Show progressDialog because searching can take a while
        this.progressDialog.show();
        this.searchQuery = query;
        spManager.setCurrentList(SEARCH_RESULTS_LIST);
        viewingMovie = false;
        MovieListFragment movieListFragment = new MovieListFragment();
        new OmdbApi().getMoviesByName(query, movieListFragment,
                this, movieListFragment.getBaseRequestId());
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentHolder, movieListFragment)
                .commit();
        mDrawerLayout.closeDrawers();
    }
}
