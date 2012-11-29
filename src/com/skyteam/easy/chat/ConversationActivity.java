package com.skyteam.easy.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

public class ConversationActivity extends FragmentActivity {
    
    public static final String TAG = "ConversationActivity";
    public static final String USER = "user";
    private ConversationFragment conversationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);
        
        Intent intent = getIntent();
        String user = intent.getStringExtra(USER);
        
        conversationFragment = ConversationFragment.newInstance(user);
        
        // Add ConversationFragment to View
        getSupportFragmentManager().beginTransaction()
                .add(R.id.conversation, conversationFragment).commit();        
    }
    
    public void onSendMessageButtonClick(View button) {
        conversationFragment.sendMessage();
    } 
    
}
