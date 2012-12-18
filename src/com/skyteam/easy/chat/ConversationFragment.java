package com.skyteam.easy.chat;

import java.util.ArrayList;

import org.jivesoftware.smack.XMPPException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class ConversationFragment extends Fragment {

    public static final String TAG = "ConversationFragment";
    public static final String ACTION = "chat.message";
    public static final String USER = "user";
    public static final String MESSAGE = "message";
    public static boolean isRunning = false;
    
    // Store chat messages
    private ArrayList<String> messages = new ArrayList<String>();
    private ConversationAdapter mAdapter;
    private ChatHistory mChatHistory;
    
    /* Broadcast receiver */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String user = intent.getStringExtra(USER);
            String message = intent.getStringExtra(MESSAGE);
            
            messages.add(message);
            mAdapter.notifyDataSetChanged();
            mChatHistory.insertMessage(user, message);
        }
        
    };
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
	    return inflater.inflate(R.layout.conversation_fragment, container, false);
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Get arguments
		Bundle args = getArguments();
		String user = args.getString(USER);
		
		// Initialize mChatHistory and get history of messages
		mChatHistory = new ChatHistory(getActivity());
		Cursor cursor = mChatHistory.getMessages(user);
		
		if (cursor.moveToFirst()) {
		    while (! cursor.isAfterLast()) {
		        String message = cursor.getString(cursor.getColumnIndex(
		                                          ChatHistory.COLUMN_MESSAGE));
		        messages.add(message);
		        cursor.moveToNext();
		    }
		}
		cursor.close();
		
        // Register to receive messages
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mMessageReceiver, new IntentFilter(ACTION));
        
		// Assign adapter to ListView
		mAdapter = new ConversationAdapter(getActivity(), messages);
		ListView listView = (ListView) getView().findViewById(R.id.messages_listview);
		listView.setAdapter(mAdapter);
		
		// Check if exists message
		String message = args.getString(MESSAGE);
		
		if (message != null) {
		    messages.add(message);
		    mAdapter.notifyDataSetChanged();
		    mChatHistory.insertMessage(user, message);
		}
	}
    
    @Override
    public void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    public void onDestroy() {        
        // Unregister receiver
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
    
    public void sendMessage(ChatService service) {
        EditText editText = (EditText) getView().findViewById(R.id.message_edittext);
        String user = getArguments().getString(USER);
        String message = editText.getText().toString();
        
        try {
            service.sendMessage(user, message);
        } catch (XMPPException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        
        messages.add(message);
        mAdapter.notifyDataSetChanged();
        mChatHistory.insertMessage(user, message);
    }
    
    public void show() {
        
    }
	
	public void clear() {
		
	}

}
