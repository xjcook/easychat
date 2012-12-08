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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);
        
        // Get Activity Intent
        Intent intent = getIntent();
        String user = intent.getStringExtra(USER);
        
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
    
    public void onSendMessageButtonClick(View button) {
        ConversationFragment f = (ConversationFragment) getSupportFragmentManager()
                .findFragmentByTag(ConversationFragment.TAG);
        f.sendMessage();
    } 
    
}
