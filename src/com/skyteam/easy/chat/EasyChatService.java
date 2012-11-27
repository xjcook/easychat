package com.skyteam.easy.chat;

import org.jivesoftware.smack.XMPPException;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class EasyChatService extends IntentService {
    
    private static final String TAG = "EasyChatService";
    private EasyChatManager mChatManager = new EasyChatManager();

    public EasyChatService() {
        super("EasyChatService");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();        
        
        // Connect to XMPP server
        try {
            mChatManager.connect();
        } catch (XMPPException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            stopSelf();
        }
        
        // Register receiver to send messages
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("SEND_MESSAGE"));
    }
    
    @Override
    public void onDestroy() {
        mChatManager.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {        
        try {
            mChatManager.login(intent.getStringExtra("appId"), 
                               intent.getStringExtra("accessToken"));
        } catch (XMPPException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            stopSelf();
        }        
    }
    
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                mChatManager.sendMessage(intent.getStringExtra("user"), 
                                         intent.getStringExtra("message"));
            } catch (XMPPException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                // TODO What to do: unsuccessful send of message
            }            
        }
        
    };

}
