package com.skyteam.easy.chat;

import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.skyteam.easy.chat.ChatService.LocalBinder;

public class PeopleFragment extends ListFragment {
    
    public static final String TAG = "PeopleFragment";
    private final Facebook facebook = new Facebook(FacebookHelper.APPID);
    private boolean mDualPane;
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        bindToChatService();
        new ShowPeopleTask().execute();
        
        // Check dualView
        View messagesFrame = getActivity().findViewById(R.id.messages);
        mDualPane = messagesFrame != null && messagesFrame.getVisibility() == View.VISIBLE;
	}
    
    @Override
    public void onStop() {
        unbindFromChatService();
        super.onStop();
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		RosterEntry entry = (RosterEntry) getListAdapter().getItem(position);
    	Toast.makeText(getActivity(), entry.getName() + " selected", 
    			Toast.LENGTH_LONG).show();
    	showConversation(entry.getUser());
	}
	
	public void show(Collection<RosterEntry> entries) {
		setListAdapter(new PeopleAdapter(getActivity(), entries
				.toArray(new RosterEntry[entries.size()])));
	}

    public void clear() {
		setListAdapter(null);
	}
    
    private void showConversation(String user) {
        if (mDualPane) {
            // Replace MessagesFragment to ConversationFragment
            ConversationFragment conversationFragment = 
                    ConversationFragment.newInstance(user); 
            FragmentTransaction transaction = getActivity()
                    .getSupportFragmentManager().beginTransaction();
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
    
    /* Chat Service */
    private boolean mIsBound = false;
    private ChatService mChatService; 
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
    
    private void bindToChatService() {
        Intent intent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void unbindFromChatService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }
    
    private class ShowPeopleTask extends AsyncTask<Void, Void, Collection<RosterEntry>> {
        
        private static final String TAG = "ShowPeopleTask";
        private static final int SLEEP_TIME = 1000;
        
        @Override
        protected Collection<RosterEntry> doInBackground(Void... params) {            
            for (;;) {
                try {
                    if (isCancelled()) {
                        return null;
                    }
                                                 
                    if (mIsBound && mChatService.isAuthenticated()) {
                        Log.v(TAG, "Authenticated!");
                
                        Collection<RosterEntry> entries = mChatService
                                .getRoster().getEntries();
                        
                        if (! entries.isEmpty()) {
                            return entries;
                        }
                    }
            
                    Log.v(TAG, "Sleeping ShowPeopleTask...");
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    cancel(true);
                }
            }                                            
        }
        
        @Override
        protected void onPostExecute(Collection<RosterEntry> entries) {
            show(entries);
        }
        
        @Override
        protected void onCancelled(Collection<RosterEntry> entries) {
            clear();
        }
        
    }
	
}
