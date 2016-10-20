package com.dasyel.dasyelwillems_pset6.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dasyel.dasyelwillems_pset6.MainActivity;
import com.dasyel.dasyelwillems_pset6.R;
import com.dasyel.dasyelwillems_pset6.adapters.FriendListAdapter;
import com.dasyel.dasyelwillems_pset6.models.Friend;
import com.dasyel.dasyelwillems_pset6.persistence.SpManager;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.FirebaseDbManager;
import com.dasyel.dasyelwillems_pset6.persistence.external_db.interfaces.FriendListRequester;

import java.util.ArrayList;

import static com.dasyel.dasyelwillems_pset6.models.Contract.FRIENDS;

/**
 * Fragment which shows the list of friends and handles the management of
 * adding and deleting friends
 */
public class FriendListFragment extends Fragment implements FriendListRequester{
    private MainActivity context;
    private FirebaseDbManager firebaseDbManager;
    private FriendListAdapter friendListAdapter;
    private ArrayList<Friend> friendArrayList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (MainActivity) getActivity();
        firebaseDbManager = FirebaseDbManager.getInstance(context.user);
        SpManager.getInstance(context).setFriendListOpen(true);

        setHasOptionsMenu(true);
        context.setActionBarTitle(FRIENDS);
        friendArrayList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend_list, container, false);

        ListView listView = (ListView) v.findViewById(R.id.friend_list_view);
        friendListAdapter = new FriendListAdapter(context, friendArrayList);
        listView.setAdapter(friendListAdapter);

        // Request friends from the firebase database to start listening to updates in friends list
        firebaseDbManager.getFriends(this);

        // Set longClickListener for deletion of friends
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend = (Friend) parent.getItemAtPosition(position);
                firebaseDbManager.removeFriend(friend);
                return true;
            }
        });

        return v;
    }

    // This method gets called whenever the friends list is updated
    @Override
    public void receiveFriendListObject(ArrayList<Friend> friendArrayList) {
        this.friendArrayList.clear();
        this.friendArrayList.addAll(friendArrayList);
        this.friendListAdapter.notifyDataSetChanged();
    }
}
