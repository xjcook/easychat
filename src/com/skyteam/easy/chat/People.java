package com.skyteam.easy.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.jivesoftware.smack.SmackAndroid;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class People extends ListActivity {
	
	private static final String TAG = "People";	
    private static final String appID = "424998287563509";
    private SharedPreferences mPrefs;	
	private Facebook facebook = new Facebook(appID);
    private AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
	private FacebookChatManager fbChat = new FacebookChatManager();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
		SmackAndroid.init(this);
        
        // Get existing access_token if any
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if (access_token != null) 
        	facebook.setAccessToken(access_token);
        if (expires != 0) 
        	facebook.setAccessExpires(expires);
        
        // Only call authorize if the access_token has expired
        if (! facebook.isSessionValid()) {
	        facebook.authorize(this, new String[] {"xmpp_login"}, new DialogListener() {
	            @Override
	            public void onComplete(Bundle values) {
	            	SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
	            }
	
	            @Override
	            public void onFacebookError(FacebookError error) {}
	
	            @Override
	            public void onError(DialogError e) {}
	
	            @Override
	            public void onCancel() {}
	        });
        }
        
        // Connect to Facebook Chat
        Thread fbThread = new Thread(new FacebookConnect());
        fbThread.setName("FacebookConnect");
        fbThread.start();
        
        // TODO Get People
        /*String[] names = fbChat.getPeople();
		PeopleArrayAdapter adapter = new PeopleArrayAdapter(this, names);
		setListAdapter(adapter);*/
    }
    
    /* Connect to Facebook Chat by XMPP */
    private class FacebookConnect implements Runnable {
    	
		@Override
		public void run() {
			Log.v(TAG, "Session: " + facebook.isSessionValid());
			try {
				while (true) {
					if (! facebook.isSessionValid()) {
						Log.v(TAG, "Sleeping...");
						Thread.sleep(1000);
					} else {
						if (fbChat.login(facebook.getAppId(), facebook.getAccessToken())) {
							Log.v(TAG, "Connected !!!");
						} else {
							Log.v(TAG, "Not connected !!!");
						}
						break;
					}
				}
			} catch (InterruptedException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}			
		}
    	
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        facebook.extendAccessTokenIfNeeded(this, null);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	String item = (String) getListAdapter().getItem(position);
    	Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_people, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection
    	switch (item.getItemId()) {
    	case R.id.logout:
    		mAsyncRunner.logout(this, new RequestListener() {

				@Override
				public void onComplete(String response, Object state) {}

				@Override
				public void onIOException(IOException e, Object state) {}

				@Override
				public void onFileNotFoundException(FileNotFoundException e, Object state) {}

				@Override
				public void onMalformedURLException(MalformedURLException e, Object state) {}

				@Override
				public void onFacebookError(FacebookError e, Object state) {}

			});
    		Log.v(TAG, "Session: " + facebook.isSessionValid());
    		return true;
    	case R.id.menu_settings:
    		/* TODO What happens if user click on settings button */
    		return super.onOptionsItemSelected(item);
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
}
