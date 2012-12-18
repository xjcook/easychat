package com.skyteam.easy.chat;

import com.skyteam.easy.chat.PeopleFragment.PeopleFragmentListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class ConversationActivity extends FragmentActivity 
    implements PeopleFragmentListener {
    
    public static final String TAG = "ConversationActivity";
    public static final String USER = ConversationFragment.USER;
    public static final String MESSAGE = ConversationFragment.MESSAGE;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);
        
        // Get user, message from Activity Intent to Bundle arguments
        Intent intent = getIntent();
        Bundle args = new Bundle();
        args.putString(USER, intent.getStringExtra(USER));
        args.putString(MESSAGE, intent.getStringExtra(MESSAGE));
        
        // Check if fragment exists, add or replace fragment to Activity View
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        ConversationFragment fragment = (ConversationFragment) manager
                .findFragmentByTag(ConversationFragment.TAG);
        
        if (fragment != null) {
            // Replace existing fragment
            fragment.setArguments(args);
            transaction.replace(R.id.conversation, fragment, ConversationFragment.TAG);
        } else {
            // Use Activity Intent to create new Fragment
            fragment = new ConversationFragment();
            fragment.setArguments(args);
            // Add new fragment
            transaction.add(R.id.conversation, fragment, ConversationFragment.TAG);
        }

        transaction.commit();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    @Override
    public void onPeopleSelected(String user) {
        
    }

    public void onSendMessageButtonClick(View button) {
        ConversationFragment f = (ConversationFragment) getSupportFragmentManager()
                .findFragmentByTag(ConversationFragment.TAG);
        f.sendMessage();
    } 
    
}
