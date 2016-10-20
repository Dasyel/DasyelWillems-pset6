package com.dasyel.dasyelwillems_pset6.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dasyel.dasyelwillems_pset6.R;
import com.dasyel.dasyelwillems_pset6.models.MovieList;

import java.util.List;

import static com.dasyel.dasyelwillems_pset6.models.Contract.*;

/**
 * Adapter to manage the movieLists in the sideDrawer
 */
public class ListsAdapter extends ArrayAdapter<MovieList> {

    public ListsAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListsAdapter(Context context, int resource, List<MovieList> items) {
        super(context, resource, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.snippet_drawer_list_entry, null);
        }

        MovieList movieList = getItem(position);

        if (movieList != null) {
            String name = movieList.getName();
            TextView nameView = (TextView) v.findViewById(R.id.drawer_entry_title);
            nameView.setText(name);
            if(name.equals(SEARCH_RESULTS_LIST) || name.equals(SUGGESTIONS_LIST) ||
                    name.equals(DEFAULT_LIST)){
                nameView.setTextColor(Color.BLUE);
            }
        }

        return v;
    }
}
