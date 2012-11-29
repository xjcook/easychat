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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MessagesFragment extends ListFragment {
    
    public static final String TAG = "MessagesFragment";
    private Facebook facebook = new Facebook(FacebookHelper.APPID);
    private AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	    new ShowMessagesTask().execute();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FacebookData data = (FacebookData) getListAdapter().getItem(position);
    	Toast.makeText(getActivity(), data.getThreadId() + " selected", 
    			Toast.LENGTH_LONG).show();
    	showConversation();
	}
	
	public void show(FacebookThread fbThread) {
		setListAdapter(new MessagesAdapter(getActivity(), fbThread.getData()));
	}
	
	public void clear() {
		setListAdapter(null);
	}
	
	private void showConversation() {
	    
	}
	
	private class ShowMessagesTask extends AsyncTask<Void, Void, FacebookThread> {
        
        private static final String TAG = "ShowMessagesTask";
        private static final int SLEEP_TIME = 500;
        private FacebookThread fbThread = null;

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
            show(fbThread);
        }
        
        @Override
        protected void onCancelled(FacebookThread fbThread) {
            clear();
        }
        
    }

}
