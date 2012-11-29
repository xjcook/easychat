package com.skyteam.easy.chat;

import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.skyteam.easy.chat.ChatService.LocalBinder;

public class PeopleFragment extends ListFragment {
    
    private static final String TAG = "PeopleFragment";
    private static final int SLEEP_TIME = 500;
    private Facebook facebook = new Facebook(FacebookHelper.APPID);
    private PeopleFragmentListener mListener;  
    
    /* Chat Service */
    private boolean mIsBound;
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

//	@Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(null);
        bindToChatService();
        new ShowPeopleTask().execute();
	}
    
    @Override
    public void onDestroy() {
        unbindFromChatService();
        super.onDestroy();
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		RosterEntry entry = (RosterEntry) getListAdapter().getItem(position);
    	Toast.makeText(getActivity(), entry.getName() + " selected", 
    			Toast.LENGTH_LONG).show();
    	mListener.onPeopleSelected(entry);
	}
	
	public void show(Collection<RosterEntry> entries) {
		setListAdapter(new PeopleAdapter(getActivity(), entries
				.toArray(new RosterEntry[entries.size()])));
	}

    public void clear() {
		setListAdapter(null);
	}
    
    public void bindToChatService() {
        // Bind to ChatService
        Intent intent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    public void unbindFromChatService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }
    
    public interface PeopleFragmentListener {
        public void onPeopleSelected(RosterEntry entry);
    }
    
    private class ShowPeopleTask extends AsyncTask<Void, Void, Collection<RosterEntry>> {
        
        private static final String TAG = "ShowPeopleTask";
        private ProgressBar progressBar;
        
        @Override
        protected void onPreExecute() {            
            /*progressBar = (ProgressBar) getActivity().findViewById(
                    R.id.first_progressbar);
            progressBar.setVisibility(View.VISIBLE);*/
        }

        @Override
        protected Collection<RosterEntry> doInBackground(Void... params) {            
            for (;;) {
                try {
                    if (isCancelled()) {
                        return null;
                    }
                    
                    if (FacebookHelper.sessionRestore(facebook, getActivity())) {
                        Log.v(TAG, "Authorized!");
                        
                        if (mIsBound && mChatService.isAuthenticated()) {
                            Log.v(TAG, "Connected!");
                    
                            Collection<RosterEntry> entries = mChatService.getRoster()
                                    .getEntries();
                            
                            if (! entries.isEmpty()) {
                                return entries;
                            } else {
                                Log.v(TAG, "Roster Entries are empty");
                            }
                        } else {
                            Log.v(TAG, "Not connected!");
                            if (mIsBound) {
                                mChatService.login(facebook.getAppId(), 
                                                   facebook.getAccessToken());
                            }
                        }
                    } else {
                        Log.v(TAG, "Not authorized!");
                        Log.v(TAG, "Token: " + facebook.getAccessToken());
                    }
            
                    Log.v(TAG, "Sleeping ShowPeopleTask...");
                    Thread.sleep(SLEEP_TIME);
                } catch (XMPPException e) {
                    /* TODO show retry button */
                    Log.e(TAG, Log.getStackTraceString(e));
                    cancel(true);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    cancel(true);
                }
            }                                            
        }
        
        @Override
        protected void onPostExecute(Collection<RosterEntry> entries) {
            //progressBar.setVisibility(View.GONE);
            show(entries);
        }
        
        @Override
        protected void onCancelled(Collection<RosterEntry> entries) {
            //progressBar.setVisibility(View.GONE);
            clear();
        }
        
    }
	
}
