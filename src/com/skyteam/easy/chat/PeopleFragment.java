package com.skyteam.easy.chat;

import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class PeopleFragment extends ListFragment {

    public interface PeopleFragmentListener {
        public void onPeopleSelected(String user);
    }
    
    public static final String TAG = "PeopleFragment";
    private PeopleFragmentListener mListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        try {
            mListener = (PeopleFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + 
                    " must implement PeopleFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.people_fragment, container, false);
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        
        // Add TextWatcher Listener
        EditText filterText = (EditText) getView().findViewById(R.id.people_filter_edittext);
        filterText.addTextChangedListener(mFilterTextWatcher);
        
        // Start ShowPeopleTask
        new ShowPeopleTask().execute();
	}
    
	@Override
    public void onDestroy() {
	    // Remove TextWatcher Listener
	    EditText filterText = (EditText) getView().findViewById(R.id.people_filter_edittext);
	    filterText.removeTextChangedListener(mFilterTextWatcher);
	    
	    super.onDestroy();
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		RosterEntry entry = (RosterEntry) getListAdapter().getItem(position);
    	mListener.onPeopleSelected(entry.getUser());
	}
    
	public void show(Collection<RosterEntry> entries) {
		setListAdapter(new PeopleAdapter(getActivity(), entries
				.toArray(new RosterEntry[entries.size()])));
	}

    public void clear() {
		setListAdapter(null);
	}
    
    private class ShowPeopleTask extends AsyncTask<Void, Void, Collection<RosterEntry>> {

        @Override
        protected Collection<RosterEntry> doInBackground(Void... params) {            
            for (;;) {
                // Get ChatService from MainActivity
                MainActivity activity = (MainActivity) getActivity();
                ChatService chatService = activity.mChatService;
                boolean isBound = activity.mIsBound;
                
                if (isBound && chatService.isAuthenticated()) {
                    return chatService.getRoster().getEntries();
                } else {
                    try {
                        Thread.sleep(MainActivity.SLEEP_TIME);
                        Log.v(TAG, "Sleeping ShowPeopleTask...");
                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }                
            }
        }
        
        @Override
        protected void onPostExecute(Collection<RosterEntry> entries) {
            show(entries);
        }
        
    }
    
    private TextWatcher mFilterTextWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            PeopleAdapter adapter = (PeopleAdapter) getListAdapter();
            adapter.getFilter().filter(s);
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {}
        
    };
	
}
