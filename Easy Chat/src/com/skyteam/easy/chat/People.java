package com.skyteam.easy.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
	
    private final String appID = "424998287563509";
    private SharedPreferences mPrefs;
	
	Facebook facebook = new Facebook(appID);
    AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        
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
        
        /* TODO Adjust FacebookChatManager to properly support SASL */
        //new FacebookConnectTask().execute(this);
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
    		return true;
    	case R.id.menu_settings:
    		/* TODO What happens if user click on settings button */
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        facebook.extendAccessTokenIfNeeded(this, null);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	String item = (String) getListAdapter().getItem(position);
    	Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    /* AsyncTask to manage facebook xmpp connection in own thread */
    private class FacebookConnectTask extends AsyncTask<Context, Void, PeopleArrayAdapter> {

		@Override
		protected PeopleArrayAdapter doInBackground(Context... contexts) {
			for (Context context : contexts) {
				FacebookChatManager fbChat = new FacebookChatManager(context);
		        if (fbChat.connect()) {
		        	if (fbChat.login()) {
		        		String[] names = fbChat.getPeople();
		        		PeopleArrayAdapter adapter = new PeopleArrayAdapter(context, names);
		        		return adapter;
		        	}
		        }
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(PeopleArrayAdapter adapter) {
			if (adapter != null)
				setListAdapter(adapter);		
		}
    	
    }
    
}
