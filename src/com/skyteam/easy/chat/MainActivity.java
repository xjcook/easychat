package com.skyteam.easy.chat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.facebook.android.Facebook;

public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";
    private final Facebook facebook = new Facebook(FacebookHelper.APPID);
    private final FacebookHelper mFacebookHelper = new FacebookHelper(this, facebook);
    private boolean mDualPane;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        // Log In to Facebook
        mFacebookHelper.login();
        
        // Start ChatService when logged to Facebook
        new Thread(new Runnable() {
            
            private static final int SLEEP_TIME = 1000;

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
    public void onResume() {
        super.onResume();
        facebook.extendAccessToken(this, null);
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
            mFacebookHelper.login();
            
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
            mFacebookHelper.logout();
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
        ConversationFragment f = (ConversationFragment) getSupportFragmentManager()
                .findFragmentByTag(ConversationFragment.TAG);
        f.sendMessage();
    }  
    
}
