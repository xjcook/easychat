package com.skyteam.easy.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;

public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";
    public static final String ACTION = "show.people";
    public static final int SLEEP_TIME = 1000;
    private final Facebook facebook = new Facebook(FacebookHelper.APPID);
    private final AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
    private boolean mDualPane;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        // Check dualView
        View messagesFrame = findViewById(R.id.messages);
        mDualPane = messagesFrame != null && messagesFrame.getVisibility() == View.VISIBLE;

        // If dual view then show MessagesFragment
        if (mDualPane) {
            // Replace Messages Frame by fragment
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            MessagesFragment messagesFragment = new MessagesFragment();
            transaction.replace(R.id.messages, messagesFragment, MessagesFragment.TAG);
            transaction.commit();
        }
        
        // If first time loaded
        if (savedInstanceState == null) {
            // Log In to Facebook and start ChatService
            loginToFacebook();
            new StartChatServiceTask().execute();
        } else {
            // Restore facebook session
            FacebookHelper.sessionRestore(facebook, this);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        facebook.extendAccessToken(this, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (isFinishing()) {
            // if app is finishing (not restarting) what to do
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
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
            
        // Log In
        case R.id.menu_login:
            // Log In to Facebook and start ChatService
            loginToFacebook();
            new StartChatServiceTask().execute();

            // Replace fragments
            if (mDualPane) {
                // Replace Messages Frame by fragment
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                MessagesFragment messagesFragment = new MessagesFragment();
                transaction.replace(R.id.messages, messagesFragment, MessagesFragment.TAG);
                transaction.commit();
                
                // TODO replace people #1
            } else {
                // TODO replace people #2
            }
            
            return true;
            
        // Log Out
        case R.id.menu_logout:
            // Chat & Facebook logout 
            logoutFromFacebook();
            stopService(new Intent(this, ChatService.class));
            
            PeopleFragment peopleFragment = (PeopleFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.people);
            MessagesFragment messagesFragment = (MessagesFragment) getSupportFragmentManager()
                    .findFragmentByTag(MessagesFragment.TAG);
            
            if (peopleFragment != null) {
                peopleFragment.clear();
            }
            
            if (messagesFragment != null) {
                messagesFragment.clear();
            }
            
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
    
    public void onSendMessageButtonClick(View button) {
        ConversationFragment f = (ConversationFragment) getSupportFragmentManager()
                .findFragmentByTag(ConversationFragment.TAG);
        f.sendMessage();
    } 

    public void loginToFacebook() {        
        if (! FacebookHelper.sessionRestore(facebook, this)) {
            facebook.authorize(this, FacebookHelper.PERMISSIONS, new DialogListener() {
            
                @Override
                public void onComplete(Bundle values) {
                    Log.v(TAG, "Facebook logged in");
    
                    // Save token
                    FacebookHelper.sessionSave(facebook, MainActivity.this);
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
                    
            });
        }   
    }
    
    public void logoutFromFacebook() {
        // Invalidate token in shared preferences
        FacebookHelper.sessionClear(this);
        
        // Facebook logout
        mAsyncRunner.logout(this, new RequestListener() {
            
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
            
        });
    }
    
    private class StartChatServiceTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (;;) {
                try {
                    if (facebook.isSessionValid()) {
                        Intent intent = new Intent(MainActivity.this, 
                                ChatService.class);
                        intent.putExtra(FacebookHelper.TOKEN, 
                                facebook.getAccessToken());
                        startService(intent);
                        return null;
                    } else {
                        Thread.sleep(SLEEP_TIME);
                    }                        
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }                
        }
        
    }
    
}
