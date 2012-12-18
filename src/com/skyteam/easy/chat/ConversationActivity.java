package com.skyteam.easy.chat;

import com.skyteam.easy.chat.ChatService.LocalBinder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

public class ConversationActivity extends FragmentActivity {
    
    public static final String TAG = "ConversationActivity";
    public static final String USER = "user";
    
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);
        
        // Get user from Activity Intent
        String user = getIntent().getStringExtra(USER);
        
        // Check if fragment exists, add or replace fragment to Activity View
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        ConversationFragment fragment = (ConversationFragment) manager
                .findFragmentByTag(ConversationFragment.TAG);
        
        if (fragment != null) {
            // Replace existing fragment
            transaction.replace(R.id.conversation, fragment, ConversationFragment.TAG);
        } else {
            // Use Activity Intent to create new Fragment
            fragment = ConversationFragment.newInstance(user);
            // Add new fragment
            transaction.add(R.id.conversation, fragment, ConversationFragment.TAG);
        }

        transaction.commit();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // Bind to ChatService
        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, mConnection, 0);
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

    public void onSendMessageButtonClick(View button) {
        if (mIsBound) {
            ConversationFragment f = (ConversationFragment) getSupportFragmentManager()
                    .findFragmentByTag(ConversationFragment.TAG);
            f.sendMessage(mChatService);
        } else {
            Log.e(TAG, "Service is not bound!");
        }
    } 
    
}
