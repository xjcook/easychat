package com.skyteam.easy.chat;

import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;

import com.skyteam.easy.chat.ChatService.LocalBinder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
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

    public static final String TAG = "PeopleFragment";
    private EditText mFilterText;
    
    /* Chat Service */
    public boolean mIsBound = false;
    public ChatService mChatService; 
    private ServiceConnection mConnection = new ServiceConnection() {
     
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mChatService = binder.getService();  
            mIsBound = true;
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected");
            mIsBound = false;
        }
     
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.people_fragment, container, false);
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        
        // Add TextWatcher Listener
        mFilterText = (EditText) getView().findViewById(R.id.people_filter_edittext);
        mFilterText.addTextChangedListener(mFilterTextWatcher);
        
        // Start ShowPeopleTask
        new ShowPeopleTask().execute();
	}
    
	@Override
    public void onStart() {
        super.onStart();
        
        // Bind to ChatService
        Intent intent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(intent, mConnection, 0);
    }

    @Override
    public void onStop() {
        super.onStop();
        
        // Unbind from ChatService
        if (mIsBound) {
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onDestroy() {
	    super.onDestroy();
   
	    // Remove TextWatcher Listener
	    mFilterText.removeTextChangedListener(mFilterTextWatcher);
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		RosterEntry entry = (RosterEntry) getListAdapter().getItem(position);
    	showConversation(entry.getUser());
	}
    
	public void show(Collection<RosterEntry> entries) {
		setListAdapter(new PeopleAdapter(getActivity(), entries
				.toArray(new RosterEntry[entries.size()])));
	}

    public void clear() {
		setListAdapter(null);
	}
    
    public void showConversation(String user) {       
        // Check dualView
        View messagesFrame = getActivity().findViewById(R.id.messages);
        boolean dualPane = messagesFrame != null && messagesFrame.getVisibility() == View.VISIBLE;
        
        if (dualPane) {
            // Replace MessagesFragment to ConversationFragment
            Bundle args = new Bundle();
            args.putString(ConversationFragment.USER, user);
            ConversationFragment conversationFragment = new ConversationFragment(); 
            conversationFragment.setArguments(args);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.messages, conversationFragment, 
                    ConversationFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            // Start new ConversationActivity
            Intent intent = new Intent(getActivity(), ConversationActivity.class);
            intent.putExtra(ConversationActivity.USER, user);
            startActivity(intent);
        }
    }
    
    private class ShowPeopleTask extends AsyncTask<Void, Void, Collection<RosterEntry>> {
        // TODO show progress circle
        
        @Override
        protected Collection<RosterEntry> doInBackground(Void... params) { 
            for (;;) {
                if (mIsBound && mChatService.isAuthenticated()) {
                    return mChatService.getRoster().getEntries();
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
            // TODO make proper filter without orientation crash
            /*PeopleAdapter adapter = (PeopleAdapter) getListAdapter();
            adapter.getFilter().filter(s);*/
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {}
        
    };
	
}
