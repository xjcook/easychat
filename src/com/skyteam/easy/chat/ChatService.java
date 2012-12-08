package com.skyteam.easy.chat;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ChatService extends Service {
    
    public static final String TAG = "EasyChatService";
    public static final String SERVER = "chat.facebook.com";
    public static final String ACTION = "chat.receive";
    public static final int PORT = 5222;
    public static final int ATTEMPT_COUNT = 10;
    private final IBinder mBinder = new LocalBinder();
    private XMPPConnection xmpp;
    private String mAccessToken;
    
    private BroadcastReceiver mSendMessageReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String user = intent.getStringExtra(ConversationFragment.USER);
            String message = intent.getStringExtra(ConversationFragment.MESSAGE);
            
            try {
                sendMessage(user, message);
            } catch (XMPPException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    };
    
    @Override
    public void onCreate() {  
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
        config.setDebuggerEnabled(false);
        config.setSASLAuthenticationEnabled(true);
        xmpp = new XMPPConnection(config);
        SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", 
                SASLXFacebookPlatformMechanism.class);
        SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mSendMessageReceiver, 
                                  new IntentFilter(ConversationFragment.ACTION));
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAccessToken = intent.getStringExtra(FacebookHelper.TOKEN);
        connect();
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Bound to " + intent);
        return mBinder;
    }
    
    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mSendMessageReceiver);
        disconnect();                
    }
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ChatService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ChatService.this;
        }
    }
    
    /**
     * Methods for clients
     */
    public void connect() {   
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (;;) {
                    try {        
                        if (! xmpp.isConnected()) {
                            xmpp.connect();
                            Thread.sleep(MainActivity.SLEEP_TIME);
                        } else {
                            login(FacebookHelper.APPID, mAccessToken);
                            return;
                        }    
                    } catch (XMPPException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
            
        }).start();
    }
    
    public void disconnect() {
        xmpp.disconnect();
    }
    
    public void login(final String appId, final String accessToken) { 
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (;;) {
                    try {                        
                        if (xmpp.isAuthenticated()) { 
                            // Get roster and convert to ArrayList
                            ArrayList<String> list = new ArrayList<String>();
                            for (RosterEntry entry : xmpp.getRoster().getEntries()) {
                                list.add(entry.getName());
                            }
                            
                            // Inform that user is logged by broadcast
                            Intent intent = new Intent(MainActivity.ACTION);
                            intent.putExtra(MainActivity.ENTRIES, list);
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(intent);
                            
                            return;
                        } else {
                            xmpp.login(appId, accessToken, "Easy Chat");
                            Thread.sleep(MainActivity.SLEEP_TIME);
                        }
                    } catch (XMPPException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    } catch (IllegalStateException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
            
        }).start();
    }
    
    public void sendMessage(String user, String message) throws XMPPException {
        ChatManager chatManager = xmpp.getChatManager();
        Chat chat = chatManager.createChat(user, new MessageListener() {
            
            @Override
            public void processMessage(Chat chat, Message message) {
                String body = message.getBody();
                
                if (body != null) {
                    Log.v(TAG, "Received message: " + body);
                    Intent intent = new Intent(ConversationFragment.ACTION);
                    intent.putExtra(ConversationFragment.MESSAGE, body);
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(intent);
                }
            }
            
        });
        
        chat.sendMessage(message);
    }
    
    public boolean isConnected() {
        return xmpp.isConnected();
    }
    
    public boolean isAuthenticated() {
        return xmpp.isAuthenticated();
    }
    
    public Roster getRoster() {
        return xmpp.getRoster();
    }

}
