package com.skyteam.easy.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ConversationActivity extends FragmentActivity {
    
    public static final String TAG = "ConversationActivity";
    public static final String USER = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);
        
        Intent intent = getIntent();
        String user = intent.getStringExtra(USER);
        
        ConversationFragment conversationFragment = ConversationFragment
                .newInstance(user);
        
        // Add ConversationFragment to View
        getSupportFragmentManager().beginTransaction()
                .add(R.id.conversation, conversationFragment, ConversationFragment.TAG)
                .commit();        
    }
    
    public void onSendMessageButtonClick(View button) {
        ConversationFragment f = (ConversationFragment) getSupportFragmentManager()
                .findFragmentByTag(ConversationFragment.TAG);
        f.sendMessage();
    } 
    
}
