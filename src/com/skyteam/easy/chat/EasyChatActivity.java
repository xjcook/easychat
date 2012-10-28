package com.skyteam.easy.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.gson.Gson;

public class EasyChatActivity extends FragmentActivity {
	
	private static final String TAG = "EasyChatActivity";	
    private static final String appID = "424998287563509";
    private static final String[] PERMISSIONS = {"xmpp_login", "read_mailbox"};
    private Context context;
	private Facebook facebook;
    private AsyncFacebookRunner mAsyncRunner;
    private DialogListener mDialogListener;
    private SharedPreferences mPrefs;	
	private EasyChatManager mChat;
	private PeopleFragment peopleFragment;
	private MessagesFragment messagesFragment;
	private ConversationFragment conversationFragment;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.easychat);
    	
		context = this;	
		mChat = new EasyChatManager(context);
		facebook = new Facebook(appID);
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		mDialogListener = new DialogListener() {
			
			@Override
			public void onComplete(Bundle values) {
				SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("access_token", facebook.getAccessToken());
                editor.putLong("access_expires", facebook.getAccessExpires());
                editor.commit();				
			}
			
            @Override
            public void onCancel() {
            	// TODO What happen if user cancel authorize 
            }
			
            @Override
            public void onFacebookError(FacebookError e) {
            	Log.e(TAG, Log.getStackTraceString(e));
            }

            @Override
            public void onError(DialogError e) {
            	Log.e(TAG, Log.getStackTraceString(e));
            }
            
		};

    	/* Set Fragments */
		peopleFragment = new PeopleFragment();
    	getSupportFragmentManager().beginTransaction()
        		.add(R.id.first_pane, peopleFragment).commit();
        new FacebookConnectTask().execute();
        
    	// If dual view then set MessagesFragment
    	if (findViewById(R.id.second_pane) != null) {
    		messagesFragment = new MessagesFragment();
    		getSupportFragmentManager().beginTransaction()
    			.add(R.id.second_pane, messagesFragment).commit();
    		new FacebookMessagesTask().execute();
    	}
		
		/* Facebook authorize */
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
        if (! facebook.isSessionValid())
	        facebook.authorize(this, PERMISSIONS, mDialogListener);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_easychat, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// Get menu items
    	MenuItem loginItem = menu.findItem(R.id.menu_login);
    	MenuItem logoutItem = menu.findItem(R.id.menu_logout);
    	
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
    	case R.id.menu_people:
    		
    		return true;
    	case R.id.menu_messages:
//    		FragmentManager fragmentManager = getSupportFragmentManager();
//    		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//    		messagesFragment = new MessagesFragment();
//    		fragmentTransaction.add(R.id.fragment_single_pane, messagesFragment);
//    		fragmentTransaction.commit();
    		return true;
    	case R.id.menu_login:
    		// Facebook authorize
    		facebook.authorize(this, PERMISSIONS, mDialogListener);
    		new FacebookConnectTask().execute();
    		
    		return true;
    	case R.id.menu_logout:            
          	mChat.logout();

          	// Facebook logout
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
            
            // Clear fragments
            if (peopleFragment != null)
            	peopleFragment.clear();
            if (messagesFragment != null)
            	messagesFragment.clear();
            if (conversationFragment != null)
            	/* TODO Set ConversationFragment clear */;

			return true;
    	case R.id.menu_settings:
    		/* TODO What happens if user click on settings button */
    		return super.onOptionsItemSelected(item);
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    /* Connect to Facebook Chat by XMPP */
    private class FacebookConnectTask extends AsyncTask<Void, Void, PeopleAdapter> {

		@Override
		protected PeopleAdapter doInBackground(Void... params) {
			try {
				while (true) {
					if (! facebook.isSessionValid()) {
						Log.v(TAG, "Sleeping FacebookConnectTask...");
						Thread.sleep(1000);
					} else {
						// TODO fbChat does not login always 
						if (mChat.login(appID, facebook.getAccessToken())) {
							Log.v(TAG, "Connected !!!");
							Log.v(TAG, "Token: " + facebook.getAccessToken());
							Log.v(TAG, "Expires: " + new Date(facebook.getAccessExpires()*1000));
							return new PeopleAdapter(context, mChat.getPeople());
						} else {
							Log.v(TAG, "Not connected !!!");
							Log.v(TAG, "Token: " + facebook.getAccessToken());
							Log.v(TAG, "Expires: " + new Date(facebook.getAccessExpires()*1000));
							//return new PeopleAdapter(context, new String[]{});
						}
					}
				}								
			} catch (InterruptedException e) {
				Log.e(TAG, Log.getStackTraceString(e));
				mChat.logout();
				Thread.currentThread().interrupt();
			}			
			
			return null;
		}
		
		@Override
		protected void onPostExecute(PeopleAdapter adapter) {
				peopleFragment.show(adapter);
		}
    	
    }
    
    /* Get Facebook Messages */
    private class FacebookMessagesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				while (true) {
					if (! facebook.isSessionValid()) {
						Log.v(TAG, "Sleeping FacebookMessagesTask...");
						Thread.sleep(1000);
					} else {						
						String query = "SELECT thread_id, snippet_author, "
								+ "snippet FROM thread WHERE folder_id = 0";
						Bundle bundles = new Bundle();
						bundles.putString("q", query);
						
						mAsyncRunner.request("fql", bundles, new RequestListener() {
							
							@Override
							public void onComplete(String response, Object state) {
								Gson gson = new Gson();
								FacebookThread fbThread = gson.fromJson(response, 
										FacebookThread.class);
								messagesFragment.show(fbThread);								
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
						
						return null;
					}
				}
			} catch (InterruptedException e) {
				Log.e(TAG, Log.getStackTraceString(e));
				mChat.logout();
				Thread.currentThread().interrupt();
			}
			
			return null;
		}
    	
    }
    
}
