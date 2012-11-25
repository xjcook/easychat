package com.skyteam.easy.chat;

import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class PeopleFragment extends ListFragment {
    
    PeopleFragmentListener mListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PeopleFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() 
                    + " must implement PeopleFragmentListener");
        }
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(null);
		mListener.onPeopleFragmentCreated();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		RosterEntry entry = (RosterEntry) getListAdapter().getItem(position);
    	Toast.makeText(getActivity(), entry.getName() + " selected", 
    			Toast.LENGTH_LONG).show();
    	mListener.onPeopleSelected(entry);
	}
	
    public interface PeopleFragmentListener {
        public void onPeopleFragmentCreated();
        public void onPeopleSelected(RosterEntry entry);
    }
	
	public void show(Collection<RosterEntry> entries) {
		setListAdapter(new PeopleAdapter(getActivity(), entries
				.toArray(new RosterEntry[entries.size()])));
	}

    public void clear() {
		setListAdapter(null);
	}
	
}
