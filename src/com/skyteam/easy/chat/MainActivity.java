package com.skyteam.easy.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
import com.skyteam.easy.chat.ChatService.LocalBinder;

public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";
    public static final String ACTION = "show.people";
    public static final int SLEEP_TIME = 1000;
    private final Facebook facebook = new Facebook(FacebookHelper.APPID);
    private final AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
    private boolean mDualPane;
    
    /* Chat Service */
    public boolean mIsBound = false;
    public ChatService mChatService; 
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        // Log In to Facebook
        loginToFacebook();
        
        // Start ChatService when logged to Facebook
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                for (;;) {
                    try {
                        if (facebook.isSessionValid()) {
                            Intent intent = new Intent(MainActivity.this, 
                                    ChatService.class);
                            intent.putExtra(FacebookHelper.TOKEN, 
                                    facebook.getAccessToken());
                            startService(intent);
                            return;
                        } else {
                            Thread.sleep(SLEEP_TIME);
                        }                        
                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }                
            }
            
        }).start();
        
        // Check dualView
        View messagesFrame = findViewById(R.id.messages);
        mDualPane = messagesFrame != null && messagesFrame.getVisibility() == View.VISIBLE;
        
        // If dual view then show MessagesFragment
        if (mDualPane) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            MessagesFragment messagesFragment = (MessagesFragment) manager
                    .findFragmentByTag(MessagesFragment.TAG);
            
            if (messagesFragment != null) {
                // Replace existing fragment
                transaction.replace(R.id.messages, messagesFragment, MessagesFragment.TAG);
            } else {
                // Add new fragment
                messagesFragment = new MessagesFragment();
                transaction.add(R.id.messages, messagesFragment, MessagesFragment.TAG);
            }

            transaction.commit();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // Bind to ChatService
        Intent intent = new Intent(MainActivity.this, 
                ChatService.class);
        bindService(intent, mConnection, 0);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        facebook.extendAccessToken(this, null);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        // Unbind from ChatService
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (isFinishing()) {
            // Log Out from Facebook
            /*mFacebookHelper.logout();*/
            // Stop ChatService 
            /*Intent intent = new Intent(this, ChatService.class);
            stopService(intent);*/
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
        //FragmentTransaction transaction;

        switch (item.getItemId()) {
            
        // Log In
        case R.id.menu_login:
            loginToFacebook();
            
            /*if (findViewById(R.id.first_pane) != null) {
                new ShowPeopleTask().execute();
            }
            
            if (findViewById(R.id.second_pane) != null) {
                new ShowMessagesTask().execute();
            }*/
            
            return true;
            
        // Log Out
        case R.id.menu_logout:
            // Chat & Facebook logout 
            logoutFromFacebook();
            //mChat.logout();  
            
            // Clear fragments
            /*if (peopleFragment != null)
                peopleFragment.clear();
            if (messagesFragment != null)
                messagesFragment.clear();
            if (conversationFragment != null)
                conversationFragment.clear();*/
            
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
        if (mIsBound) {
            ConversationFragment f = (ConversationFragment) getSupportFragmentManager()
                    .findFragmentByTag(ConversationFragment.TAG);
            f.sendMessage(mChatService);
        } else {
            Log.e(TAG, "Service is not bound!");
        }
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
    
}
