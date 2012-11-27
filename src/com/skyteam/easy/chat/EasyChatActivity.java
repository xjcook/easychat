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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.gson.Gson;
import com.skyteam.easy.chat.MessagesFragment.MessagesFragmentListener;
import com.skyteam.easy.chat.PeopleFragment.PeopleFragmentListener;

public class EasyChatActivity extends FragmentActivity 
    implements PeopleFragmentListener, MessagesFragmentListener {
    
    private static final String TAG = "EasyChatActivity";    
    private static final String APPID = "424998287563509";
    private static final String[] PERMISSIONS = {"xmpp_login", "read_mailbox"};
    private static final int SLEEPTIME = 500;
    private EasyChatManager mChatManager = new EasyChatManager();
    private Facebook facebook = new Facebook(APPID);
    private AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
    private SharedPreferences mPrefs;    
    private PeopleFragment peopleFragment;
    private MessagesFragment messagesFragment;
    private ConversationFragment conversationFragment;
    private boolean mDualPane;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.easychat_activity);
            
        facebookLogin();
        
        // Connect to XMPP server
        try {
            mChatManager.connect();
        } catch (XMPPException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        
        /* Load Fragments */
        View secondPane = findViewById(R.id.second_pane);
        mDualPane = secondPane != null && secondPane.getVisibility() == View.VISIBLE;
        
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        
        if (mDualPane) {
            peopleFragment = new PeopleFragment();
            messagesFragment = new MessagesFragment();
            transaction.add(R.id.first_pane, peopleFragment, "people");
            transaction.add(R.id.second_pane, messagesFragment, "messages");
        } else {
            peopleFragment = new PeopleFragment();
            transaction.add(R.id.first_pane, peopleFragment, "people");
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
        
        if (isFinishing()) {
            mChatManager.disconnect();
            //facebookLogout();
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
        FragmentTransaction transaction;

        switch (item.getItemId()) {
            
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
            mChatManager.disconnect();  
            
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
    
    @Override
    public void onPeopleFragmentCreated() {
        new ShowPeopleTask().execute();
    }
    
    @Override
    public void onPeopleSelected(RosterEntry entry) {
        conversationFragment = ConversationFragment.newInstance(entry.getUser());
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        
        if (mDualPane) {
            transaction.replace(R.id.second_pane, conversationFragment);
        } else {
            transaction.replace(R.id.first_pane, conversationFragment);
        }
        
        transaction.addToBackStack(null);
        transaction.commit();
    }
    
    @Override
    public void onMessagesFragmentCreated() {
        new ShowMessagesTask().execute();
    }
    
    @Override
    public void onMessageSelected(FacebookData data) {
        conversationFragment = new ConversationFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        
        if (mDualPane) {
            transaction.replace(R.id.second_pane, conversationFragment);
        } else {
            transaction.replace(R.id.first_pane, conversationFragment);
        }
        
        transaction.addToBackStack(null);
        transaction.commit();
    }
    
    // Called by MessagesFragment
    public void sendMessage(View button) {
        EditText editText = (EditText) findViewById(R.id.message_edittext);
        String message = editText.getText().toString();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        try {
            mChatManager.sendMessage(conversationFragment
                    .getArguments().getString("user"), message);
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, Log.getStackTraceString(e));
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
            if (peopleFragment != null) {
                progressBar = (ProgressBar) findViewById(R.id.first_progressbar);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                cancel(true);
            }
        }

        @Override
        protected Collection<RosterEntry> doInBackground(Void... params) {            
            for (;;) {
                try {
                    if (isCancelled()) {
                        return null;
                    }
                    
                    if (facebook.isSessionValid() && mChatManager.isAuthenticated()) {
                        Log.v(TAG, "Authorized & Connected!");
                
                        Collection<RosterEntry> entries = mChatManager.getRoster()
                                .getEntries();
                        
                        if (! entries.isEmpty()) {
                            return entries;
                        } else {
                            Log.v(TAG, "Roster Entries are empty");
                        }
                    } else {
                        if (facebook.isSessionValid()) {
                            Log.v(TAG, "Not connected!");
                            mChatManager.login(APPID, facebook.getAccessToken());
                        } else {
                            Log.v(TAG, "Not authorized!");
                            Log.v(TAG, "Token: " + facebook.getAccessToken());
                        }
                    }
            
                    Log.v(TAG, "Sleeping ShowPeopleTask...");
                    Thread.sleep(SLEEPTIME);
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
            progressBar.setVisibility(View.GONE);
            peopleFragment.show(entries);
        }
        
        @Override
        protected void onCancelled(Collection<RosterEntry> entries) {
            if (peopleFragment != null) {
                progressBar.setVisibility(View.GONE);
                peopleFragment.clear();
                mChatManager.disconnect();
            } else {
                Log.v(TAG, "peopleFragment == null");
            }
        }
        
    }
    
    /* Get Facebook Messages */
    private class ShowMessagesTask extends AsyncTask<Void, Void, FacebookThread> {
        
        private static final String TAG = "ShowMessagesTask";
        private FacebookThread fbThread = null;
        private ProgressBar progressBar;
        
        @Override
        protected void onPreExecute() {
            if (messagesFragment != null) {
                // if dual view
                if (findViewById(R.id.second_pane) != null) {
                    progressBar = (ProgressBar) findViewById(R.id.second_progressbar);
                } else {
                    progressBar = (ProgressBar) findViewById(R.id.first_progressbar);
                }
                
                progressBar.setVisibility(View.VISIBLE);
            } else {
                cancel(true);
            }
        }

        @Override
        protected FacebookThread doInBackground(Void... params) {
            for (;;) {
                try {
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
                                cancel(true);
                            }
                        }
                    } else {
                        Log.v(TAG, "Not authorized!");
                        Log.v(TAG, "Token: " + facebook.getAccessToken());
                    }
        
                    Log.v(TAG, "Sleeping ShowMessagesTask...");
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    cancel(true);
                }
            }  
        }
        
        @Override
        protected void onPostExecute(FacebookThread fbThread) {      
            progressBar.setVisibility(View.GONE);
            messagesFragment.show(fbThread);
        }
        
        @Override
        protected void onCancelled(FacebookThread fbThread) {
            if (messagesFragment != null) {
                progressBar.setVisibility(View.GONE);
                messagesFragment.clear();
            } else {
                Log.v(TAG, "messagesFragment == null");
            }
        }
        
    }
    
}
