package com.skyteam.easy.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.gson.Gson;

public class EasyChatActivity extends FragmentActivity {
    
    private static final String TAG = "EasyChatActivity";    
    private static final String APPID = "424998287563509";
    private static final String[] PERMISSIONS = {"xmpp_login", "read_mailbox"};
    private static final int SLEEPTIME = 500;
    private EasyChatManager mChat = new EasyChatManager();
    private Facebook facebook = new Facebook(APPID);
    private AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
    private SharedPreferences mPrefs;    
    private PeopleFragment peopleFragment;
    private MessagesFragment messagesFragment;
    private ConversationFragment conversationFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.easychat);
            
        facebookLogin();
        
        /* Load Fragments */
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        
        if (findViewById(R.id.first_pane) != null) {
            peopleFragment = new PeopleFragment();
            transaction.add(R.id.first_pane, peopleFragment);
            new ShowPeopleTask().execute();
        }
        
        if (findViewById(R.id.second_pane) != null) {
            messagesFragment = new MessagesFragment();
            transaction.add(R.id.second_pane, messagesFragment);
            new ShowMessagesTask().execute();
        }
        
        transaction.commit();
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
    public void onDestroy() {
        super.onDestroy();
        //mChat.logout();
        
        if (isFinishing()) {
            mChat.logout();
            facebookLogout();
        }
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
        switch (item.getItemId()) {
        
        // Show people
        case R.id.menu_people:
            if (findViewById(R.id.first_pane) != null) {
                new ShowPeopleTask().execute();
            }
            
            return true;
            
        // Show messages
        case R.id.menu_messages:
            if (findViewById(R.id.second_pane) != null) {
                new ShowMessagesTask().execute();
            }
            
            return true;
            
        // Log In
        case R.id.menu_login:
            facebookLogin();
            
            if (findViewById(R.id.first_pane) != null) {
                new ShowPeopleTask().execute();
            }
            
            if (findViewById(R.id.second_pane) != null) {
                new ShowMessagesTask().execute();
            }
            
            return true;
            
        // Log Out
        case R.id.menu_logout:
            // Chat & Facebook logout 
            facebookLogout();
            mChat.logout();  
            
            // Clear fragments
            if (peopleFragment != null)
                peopleFragment.clear();
            if (messagesFragment != null)
                messagesFragment.clear();
            if (conversationFragment != null)
                conversationFragment.clear();
            
            return true;
        
        // Exit option only for testing purposes
        case R.id.menu_exit:
            //android.os.Process.killProcess(android.os.Process.myPid());
            finish();
            
            return true;
            
        // Settings
        case R.id.menu_settings:
            /* TODO what happens if user click on settings button */            
            return true;
        
        default:
            return super.onOptionsItemSelected(item);
            
        }
    }
    
    private void facebookLogin() {
        DialogListener dialogListener = new DialogListener() {
        
            @Override
            public void onComplete(Bundle values) {
                Log.v(TAG, "Facebook logged in");
                
                // Save token
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("access_token", facebook.getAccessToken());
                editor.putLong("access_expires", facebook.getAccessExpires());
                editor.commit();
            }
            
            @Override
            public void onCancel() {
                Log.v(TAG, "Facebook login cancelled");
                /* TODO what happens if user cancel authorize */
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
        
        /* Facebook authorize */
        // Get existing access_token if any
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if (access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if (expires != 0) { 
            facebook.setAccessExpires(expires);
        }
        
        // Only call authorize if the access_token has expired
        Log.v(TAG, "FacebookSession: " + facebook.isSessionValid());
        if (! facebook.isSessionValid()) {
            facebook.authorize(this, PERMISSIONS, dialogListener);
        }
    }
    
    private void facebookLogout() {
        RequestListener requestListener = new RequestListener() {
            
            @Override
            public void onComplete(String response, Object state) {
                Log.v(TAG, "Facebook logged out");
            }
            
            @Override
            public void onFacebookError(FacebookError e, Object state) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
            @Override
            public void onIOException(IOException e, Object state) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
            @Override
            public void onFileNotFoundException(FileNotFoundException e, 
                    Object state) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
            @Override
            public void onMalformedURLException(MalformedURLException e, 
                    Object state) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
        };
        
        // Invalidate token in shared preferences
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("access_token", null);
        editor.putLong("access_expires", 0);
        editor.commit();
        
        // Facebook logout
        mAsyncRunner.logout(this, requestListener);        
    }
    
    /* Connect to XMPP and get people */
    private class ShowPeopleTask extends AsyncTask<Void, Void, Collection<RosterEntry>> {
        
        private static final String TAG = "ShowPeopleTask";
        private ProgressBar progressBar;
        
        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.first_progressbar);
            progressBar.setVisibility(View.VISIBLE);            
        }

        @Override
        protected Collection<RosterEntry> doInBackground(Void... params) {
            try {
                for (;;) {
                    if (isCancelled()) {
                        return null;
                    }
                    
                    if (facebook.isSessionValid() && mChat.isAuthenticated()) {
                        Log.v(TAG, "Authorized & Connected!");
                
                        Collection<RosterEntry> entries = mChat.getRoster()
                                .getEntries();
                        
                        if (! entries.isEmpty()) {
                            return entries;
                        } else {
                            Log.v(TAG, "Roster Entries are empty");
                        }
                    } else {
                        if (facebook.isSessionValid()) {
                            Log.v(TAG, "Not connected!");
                            mChat.login(APPID, facebook.getAccessToken());
                        } else {
                            Log.v(TAG, "Not authorized!");
                            Log.v(TAG, "Token: " + facebook.getAccessToken());
                        }
                    }
            
                    Log.v(TAG, "Sleeping ShowPeopleTask...");
                    Thread.sleep(SLEEPTIME);
                }                                
            } catch (XMPPException e) {
                /* TODO show retry button */
                Log.e(TAG, Log.getStackTraceString(e));
                cancel(true);
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                cancel(true);
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Collection<RosterEntry> entries) {    
            progressBar.setVisibility(View.GONE);
            
            if (entries != null) {
                peopleFragment.show(entries);
            } else {
                peopleFragment.clear();
            }
        }
        
        @Override
        protected void onCancelled(Collection<RosterEntry> entries) {
            progressBar.setVisibility(View.GONE);
            mChat.logout();
        }
        
    }
    
    /* Get Facebook Messages */
    private class ShowMessagesTask extends AsyncTask<Void, Void, FacebookThread> {
        
        private static final String TAG = "ShowMessagesTask";
        private FacebookThread fbThread = null;
        private ProgressBar progressBar;
        
        @Override
        protected void onPreExecute() {
            // if dual view
            if (findViewById(R.id.second_pane) != null) {
                progressBar = (ProgressBar) findViewById(R.id.second_progressbar);
            } else {
                progressBar = (ProgressBar) findViewById(R.id.first_progressbar);
            }
            
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected FacebookThread doInBackground(Void... params) {
            try {
                for (;;) {
                    if (isCancelled()) {
                        return null;
                    }
                    
                    if (facebook.isSessionValid()) {
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
                                return null;
                            }
                        }
                    } else {
                        Log.v(TAG, "Not authorized!");
                        Log.v(TAG, "Token: " + facebook.getAccessToken());
                    }
        
                    Log.v(TAG, "Sleeping ShowMessagesTask...");
                    Thread.sleep(SLEEPTIME);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                cancel(true);
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(FacebookThread fbThread) {      
            progressBar.setVisibility(View.GONE);
            
            if (fbThread != null) {
                messagesFragment.show(fbThread);
            } else {
                messagesFragment.clear();
            }
        }
        
        @Override
        protected void onCancelled(FacebookThread fbThread) {
            progressBar.setVisibility(View.GONE);
        }
        
    }
    
}
