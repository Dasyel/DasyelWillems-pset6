package com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces;

import com.dasyel.dasyelwillems_pset6.models.Friend;

import java.util.ArrayList;

/**
 * This interface is needed for getting callbacks from the firebaseDbManager on
 * requesting friend list data
 */
public interface FriendListRequester {

    // This method is called whenever the friendList changes in the database
    void receiveFriendListObject(ArrayList<Friend> friendArrayList);
}
