package com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces;

/**
 * this interface is needed to get a callback on whether the user already exists or not
 */
public interface NewUserChecker {

    // This method gets called when the result comes back from firebase
    void handleNewUser(boolean userExists);
}
