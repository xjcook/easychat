package com.skyteam.easy.chat;

import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class PeopleFragment extends ListFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		RosterEntry entry = (RosterEntry) getListAdapter().getItem(position);
    	Toast.makeText(getActivity(), entry.getName() + " selected", 
    			Toast.LENGTH_LONG).show();
	}
	
	public void show(Collection<RosterEntry> entries) {
		setListAdapter(new PeopleAdapter(getActivity(), entries
				.toArray(new RosterEntry[entries.size()])));
	}
	
	public void clear() {
		setListAdapter(null);
	}
	
}
