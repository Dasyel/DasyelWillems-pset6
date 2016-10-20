package com.dasyel.dasyelwillems_pset6.persistence.external_db;

import android.content.Context;
import android.widget.Toast;

import com.dasyel.dasyelwillems_pset6.R;
import com.dasyel.dasyelwillems_pset6.models.Friend;
import com.dasyel.dasyelwillems_pset6.models.Movie;
import com.dasyel.dasyelwillems_pset6.models.MovieList;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces.FriendListRequester;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces.MovieListRequester;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces.NewUserChecker;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.dasyel.dasyelwillems_pset6.models.Contract.*;

/**
 * All firebase database operations in one file
 */
public class FirebaseDbManager {
    private DatabaseReference dbRef;
    private FirebaseUser user;
    private static FirebaseDbManager instance;

    // Private constructor
    private FirebaseDbManager(FirebaseUser user){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        this.user = user;
    }

    // Get singleton instance
    public static FirebaseDbManager getInstance(FirebaseUser user){
        if(instance == null){
            instance = new FirebaseDbManager(user);
        }
        return instance;
    }

    // Add a user to the database
    public void addUser(){
        String email = user.getEmail();
        if(email == null) return;
        email = email.replace('.', ',');
        dbRef.child(USERS).child(email).setValue(user.getUid());
    }

    // Check whether the current user exists in the database. Gives a callback on result.
    public void checkForUser(final NewUserChecker newUserChecker){
        String email = user.getEmail();
        if(email == null) return;
        email = email.replace('.', ',');
        dbRef.child(USERS).child(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null){
                            newUserChecker.handleNewUser(false);
                        } else {
                            newUserChecker.handleNewUser(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
    }

    // Add a movieList to the database
    public void addMovieList(final String name){
        final DatabaseReference ref = dbRef.child(MOVIE_LISTS).child(user.getUid()).child(name);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    ref.setValue("1");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Update a movieList
    public void saveMovieList(MovieList movieList){
        dbRef.child(MOVIE_LISTS).child(user.getUid()).child(movieList.getName()).removeValue();
        for(Movie movie : movieList.getMovieArrayList()){
            saveMovie(movieList.getName(), movie);
        }
    }

    // Add a movie to a movieList
    public void saveMovie(String list, final Movie movie){
        final DatabaseReference ref = dbRef.child(MOVIE_LISTS).child(user.getUid()).child(list);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String val;
                try {
                    val = dataSnapshot.getValue(String.class);
                } catch (DatabaseException e){
                    val = "0"; //just some value to skip the following if
                }
                if("1".equals(val)){
                    ref.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            ref.child(movie.getId()).setValue(movie);
                        }
                    });
                } else {
                    ref.child(movie.getId()).setValue(movie);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Suggest a movie to a friend
    // This effectively does the same as "saveMovie" but then at another user
    public void suggestMovie(String friendUid, final Movie movie){
        final DatabaseReference ref = dbRef.child(MOVIE_LISTS).child(friendUid)
                .child(SUGGESTIONS_LIST);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String val;
                try {
                    val = dataSnapshot.getValue(String.class);
                } catch (DatabaseException e){
                    val = "0"; //just some value to skip the following if
                }
                if("1".equals(val)){
                    ref.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            ref.child(movie.getId()).setValue(movie);
                        }
                    });
                } else {
                    ref.child(movie.getId()).setValue(movie);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Remove a movieList from the database
    public void removeMovieList(String name){
        dbRef.child(MOVIE_LISTS).child(user.getUid()).child(name).removeValue();
    }

    // Remove a movie from a list in the database
    public void removeMovie(String list, final String movieId){
        final DatabaseReference ref = dbRef.child(MOVIE_LISTS).child(user.getUid()).child(list);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 1 && dataSnapshot.hasChild(movieId)){
                    ref.setValue("1");
                } else {
                    ref.child(movieId).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Gets all movieLists from the database. Callback on results
    public void getMovieLists(final MovieListRequester movieListRequester){
        final ArrayList<MovieList> movieListArrayList = new ArrayList<>();
        final DatabaseReference ref = dbRef.child(MOVIE_LISTS).child(user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // First create all empty MovieLists and send them to the requester
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    MovieList movieList = new MovieList(new ArrayList<Movie>(), child.getKey());
                    movieListArrayList.add(movieList);
                }
                movieListRequester.receiveMovieLists(movieListArrayList);

                // Then set listeners to fill the movieLists and keep them up to date
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    ref.child(child.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            MovieList movieList = new MovieList(
                                    new ArrayList<Movie>(), dataSnapshot.getKey());
                            String val;
                            try {
                                val = dataSnapshot.getValue(String.class);
                            } catch (DatabaseException ignored) {
                                val = "0"; // just some value to skip the following if
                            }
                            if(val != null && !val.equals("1")){
                                for(DataSnapshot movieSnap : dataSnapshot.getChildren()){
                                    movieList.addMovie(movieSnap.getValue(Movie.class));
                                }
                            }
                            movieListRequester.receiveMovieList(movieList);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Get friendList from database. Callbacks on changes
    public void getFriends(final FriendListRequester friendListRequester){
        DatabaseReference ref = dbRef.child(FRIENDS).child(user.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Friend> friendArrayList = new ArrayList<>();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    friendArrayList.add(child.getValue(Friend.class));
                }
                friendListRequester.receiveFriendListObject(friendArrayList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Add a friend to the database
    public void addFriend(final Context context, final String email, final String name){
        DatabaseReference ref = dbRef.child(USERS).child(email.replace('.', ','));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.getValue(String.class);
                if(uid != null){
                    dbRef.child(FRIENDS).child(user.getUid()).child(uid)
                            .setValue(new Friend(name, uid));
                } else {
                    Toast.makeText(context, R.string.error_no_such_friend, Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Remove a friend from the database
    public void removeFriend(Friend friend){
        dbRef.child(FRIENDS).child(user.getUid()).child(friend.getUid()).removeValue();
    }
}
