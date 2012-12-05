package com.skyteam.easy.chat;

import java.util.ArrayList;

import org.jivesoftware.smack.XMPPException;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.skyteam.easy.chat.ChatService.LocalBinder;

public class ConversationFragment extends Fragment {

    public static final String TAG = "ConversationFragment";
    public static final String USER = "user";
    public static final String ACTION = "chat.message";
    public static final String MESSAGE = "message";
    private ArrayList<String> messages = new ArrayList<String>();
    private ConversationAdapter adapter;
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
	    return inflater.inflate(R.layout.conversation_fragment, container, false);
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		bindToChatService();
		
		// Assign adapter to ListView
		adapter = new ConversationAdapter(getActivity(), messages);
		ListView listView = (ListView) getView().findViewById(R.id.messages_listview);
		listView.setAdapter(adapter);
		
		// Register to receive messages
		LocalBroadcastManager.getInstance(getActivity())
		        .registerReceiver(mMessageReceiver, new IntentFilter(ACTION));
	}
    
    @Override
    public void onStop() {
        unbindFromChatService();
        
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mMessageReceiver);
        
        super.onStop();
    }
    
    public void sendMessage() {
        if (mIsBound) {
            EditText editText = (EditText) getView().findViewById(R.id.message_edittext);
            String user = getArguments().getString(USER);
            String message = editText.getText().toString();
            
            try {
                mChatService.sendMessage(user, message);
            } catch (XMPPException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else {
            Log.e(TAG, "Service is not bound");
            // TODO Wait for service
        }
    }
    
    public void show() {
        
    }
	
	public void clear() {
		
	}
	
    public static ConversationFragment newInstance(String user) {
        ConversationFragment conversationFragment = new ConversationFragment();
        
        Bundle args = new Bundle();
        args.putString(USER, user);
        conversationFragment.setArguments(args);
        
        return conversationFragment;
    }
	
    /* Chat Service */
    private boolean mIsBound = false;
    private ChatService mChatService; 
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
            mChatService = null;
            mIsBound = false;
        }
     
    };
    
    private void bindToChatService() {
        // Bind to ChatService
        Intent intent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void unbindFromChatService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }
    
    /* Broadcast receiver */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            messages.add(intent.getStringExtra(MESSAGE));
            adapter.notifyDataSetChanged();
        }
        
    };
	
}
