package com.skyteam.easy.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class ConversationActivity extends FragmentActivity {
    
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
        
        /* Check if fragment exists and replace fragment */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();        
        
        ConversationFragment fragment = new ConversationFragment();
        fragment.setArguments(args);
        
        transaction.replace(R.id.conversation, fragment, ConversationFragment.TAG);
        transaction.commit();
    }
    
    public void onSendMessageButtonClick(View button) {
        ConversationFragment f = (ConversationFragment) getSupportFragmentManager()
                .findFragmentByTag(ConversationFragment.TAG);
        f.sendMessage();
    } 
    
}
