package com.dasyel.dasyelwillems_pset6.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dasyel.dasyelwillems_pset6.R;
import com.dasyel.dasyelwillems_pset6.models.Friend;

import java.util.List;

/**
 * Adapter to manage a listView of Friend objects
 */
public class FriendListAdapter extends BaseAdapter {
    private List<Friend> friendList;
    private Activity activity;
    private LayoutInflater inflater;

    public FriendListAdapter(Activity activity, List<Friend> friendList){
        this.activity = activity;
        this.friendList = friendList;
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Friend getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.snippet_friend_list_entry, null);

        TextView name = (TextView) convertView.findViewById(R.id.friend_name);

        Friend friend = friendList.get(position);
        name.setText(friend.getName());

        return convertView;
    }
}
