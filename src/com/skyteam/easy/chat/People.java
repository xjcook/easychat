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
    private static final String[] PERMISSIONS = {"xmpp_login"};
    private Context context;
	private Facebook facebook;
    private AsyncFacebookRunner mAsyncRunner;
    private DialogListener mDialogListener;
    private SharedPreferences mPrefs;	
	private FacebookChatManager fbChat;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
		SmackAndroid.init(this);
		context = this;		
		facebook = new Facebook(appID);
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		fbChat = new FacebookChatManager();
        
		/* Facebook authorize */
		mDialogListener = new DialogListener() {
			
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
		};
		
		// Get existing access_token if any
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if (access_token != null) 
        	facebook.setAccessToken(access_token);
        if (expires != 0) 
        	facebook.setAccessExpires(expires);
        
        // Only call authorize if the access_token has expired
        Log.v(TAG, "Session: " + facebook.isSessionValid());
        if (! facebook.isSessionValid()) {
	        facebook.authorize(this, PERMISSIONS, mDialogListener);
        }
        
        // Connect to Facebook Chat
        new FacebookConnectTask().execute();
    }

    /* Connect to Facebook Chat by XMPP */
    private class FacebookConnectTask extends AsyncTask<Void, Void, PeopleArrayAdapter> {

		@Override
		protected PeopleArrayAdapter doInBackground(Void... params) {
			try {
				while (true) {
					if (! facebook.isSessionValid()) {
						Log.v(TAG, "Sleeping...");
						Thread.sleep(1000);
					} else {
						// TODO fbChat does not login always 
						if (fbChat.login(appID, facebook.getAccessToken())) {
						//if (fbChat.login(appID, "AAAGCiJZC12vUBAGK03mxEImBDQVkrVwgPcUFqrklaNVoHBSYCKVF797ARwUy9LVZBPVD2d6lirPs1fuAm9BrDnk83PxmSXlOPHjhDsWq6Ybp2KZCUxa")) {
							Log.v(TAG, "Connected !!!");
							return new PeopleArrayAdapter(context, fbChat.getPeople());
						} else {
							Log.v(TAG, "Not connected !!!");
							Log.v(TAG, "Token: " + facebook.getAccessToken());
							Log.v(TAG, "Expires: " + facebook.getAccessExpires());
						}
						break;
					}
				}								
			} catch (InterruptedException e) {
				Log.e(TAG, Log.getStackTraceString(e));
				fbChat.logout();
				Thread.currentThread().interrupt();
			}			
			return null;
		}
		
		@Override
		protected void onPostExecute(PeopleArrayAdapter peopleAdapter) {
			setListAdapter(peopleAdapter);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// Get menu items
    	MenuItem loginItem = menu.findItem(R.id.login);
    	MenuItem logoutItem = menu.findItem(R.id.logout);
    	
    	// Set visibility
    	if (facebook.isSessionValid()) {
    		loginItem.setVisible(false);
    		logoutItem.setVisible(true);
    	} else {
    		loginItem.setVisible(true);
    		logoutItem.setVisible(false);
    	}
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection
    	switch (item.getItemId()) {
    	case R.id.login:
    		// Facebook authorize
    		facebook.authorize(this, PERMISSIONS, mDialogListener);
    		new FacebookConnectTask().execute();
    		
    		return true;
    	case R.id.logout:
    		// Facebook logout            
          	fbChat.logout();

			mAsyncRunner.logout(this, new RequestListener() {
				  @Override
				  public void onComplete(String response, Object state) {}
				  
				  @Override
				  public void onIOException(IOException e, Object state) {}
				  
				  @Override
				  public void onFileNotFoundException(FileNotFoundException e,
				        Object state) {}
				  
				  @Override
				  public void onMalformedURLException(MalformedURLException e,
				        Object state) {}
				  
				  @Override
				  public void onFacebookError(FacebookError e, Object state) {}
			});
		
			// Invalidate token in shared preferences
			SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString("access_token", null);
            editor.putLong("access_expires", 0);
            editor.commit();
            
            // Clear list 
            setListAdapter(null);

			return true;
    	case R.id.menu_settings:
    		/* TODO What happens if user click on settings button */
    		return super.onOptionsItemSelected(item);
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
}
