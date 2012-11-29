package com.skyteam.easy.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.google.gson.Gson;
import com.skyteam.easy.chat.ChatService.LocalBinder;
import com.skyteam.easy.chat.PeopleFragment.PeopleFragmentListener;

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

public class MessagesFragment extends ListFragment {
    
    private static final String TAG = "MessagesFragment";
    private static final int SLEEP_TIME = 500;
    private Facebook facebook = new Facebook(FacebookHelper.APPID);
    private AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
    private MessagesFragmentListener mListener;  
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MessagesFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() 
                    + " must implement MessagesFragmentListener");
        }
    }
	
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(null);
	    new ShowMessagesTask().execute();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FacebookData data = (FacebookData) getListAdapter().getItem(position);
    	Toast.makeText(getActivity(), data.getThreadId() + " selected", 
    			Toast.LENGTH_LONG).show();
    	mListener.onMessageSelected(data);
	}
	
	public void show(FacebookThread fbThread) {
		setListAdapter(new MessagesAdapter(getActivity(), fbThread.getData()));
	}
	
	public void clear() {
		setListAdapter(null);
	}
	
	public interface MessagesFragmentListener {
        public void onMessageSelected(FacebookData data);
    }
	
	private class ShowMessagesTask extends AsyncTask<Void, Void, FacebookThread> {
        
        private static final String TAG = "ShowMessagesTask";
        private FacebookThread fbThread = null;
        private ProgressBar progressBar;
        
        @Override
        protected void onPreExecute() {
            // if dual view
            /*if (getActivity().findViewById(R.id.second_pane) != null) {
                progressBar = (ProgressBar) getActivity().findViewById(R.id.second_progressbar);
            } else {
                progressBar = (ProgressBar) getActivity().findViewById(R.id.first_progressbar);
            }
            
            progressBar.setVisibility(View.VISIBLE);*/
            
        }

        @Override
        protected FacebookThread doInBackground(Void... params) {
            for (;;) {
                try {
                    if (isCancelled()) {
                        return null;
                    }
                    
                    if (FacebookHelper.sessionRestore(facebook, getActivity())) {
                        Log.v(TAG, "Authorized!");
                        
                        // Get time in Unix seconds
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, -7);
                        String time = String.valueOf(cal.getTimeInMillis() / 1000);                
                        
                        // FQL query
                        String query = "SELECT thread_id, snippet_author, "
                                + "snippet FROM thread WHERE folder_id = 0 AND "
                                + "updated_time > " + time;
                        Bundle bundles = new Bundle();
                        bundles.putString("q", query);
                        
                        mAsyncRunner.request("fql", bundles, new RequestListener() {
                            
                            @Override
                            public void onComplete(String response, Object state) {
                                Gson gson = new Gson();
                                fbThread = gson.fromJson(response, 
                                        FacebookThread.class);                            
                            }
                            
                            @Override
                            public void onFacebookError(
                                    FacebookError e, Object state) {
                                Log.e(TAG, Log.getStackTraceString(e));                                
                            }
                            
                            @Override
                            public void onMalformedURLException(
                                    MalformedURLException e, Object state) {
                                Log.e(TAG, Log.getStackTraceString(e));                                
                            }
                            
                            @Override
                            public void onIOException(
                                    IOException e, Object state) {
                                Log.e(TAG, Log.getStackTraceString(e));                                
                            }
                            
                            @Override
                            public void onFileNotFoundException(
                                    FileNotFoundException e, Object state) {
                                Log.e(TAG, Log.getStackTraceString(e));                                
                            }
                            
                        });
                        
                        if (fbThread != null) {
                            if (! fbThread.isEmpty()) {
                                return fbThread;
                            } else {
                                Log.v(TAG, "FacebookThread is empty");
                                cancel(true);
                            }
                        }
                    } else {
                        Log.v(TAG, "Not authorized!");
                        Log.v(TAG, "Token: " + facebook.getAccessToken());
                    }
        
                    Log.v(TAG, "Sleeping ShowMessagesTask...");
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    cancel(true);
                }
            }  
        }
        
        @Override
        protected void onPostExecute(FacebookThread fbThread) {      
            //progressBar.setVisibility(View.GONE);
            show(fbThread);
        }
        
        @Override
        protected void onCancelled(FacebookThread fbThread) {
            //progressBar.setVisibility(View.GONE);
            clear();
        }
        
    }

}
