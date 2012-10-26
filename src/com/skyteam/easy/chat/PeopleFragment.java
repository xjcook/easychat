package com.skyteam.easy.chat;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class PeopleFragment extends ListFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String item = (String) getListAdapter().getItem(position);
    	Toast.makeText(getActivity(), item + " selected", Toast.LENGTH_LONG).show();
	}
	
	public void show(PeopleAdapter adapter) {
		setListAdapter(adapter);
	}
	
	public void clear() {
		setListAdapter(null);
	}
	
}
