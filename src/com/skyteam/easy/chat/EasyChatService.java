package com.skyteam.easy.chat;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class EasyChatService extends Service {
    
    private static final String TAG = "EasyChatService";
    private static final String SERVER = "chat.facebook.com";
    private static final Integer PORT = 5222;
    private XMPPConnection xmpp;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
    @Override
    public void onCreate() {    
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
        config.setDebuggerEnabled(false);
        config.setSASLAuthenticationEnabled(true);
        xmpp = new XMPPConnection(config);
        
        // Connect to XMPP server
        try {
            connect();
        } catch (XMPPException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            stopSelf();
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    @Override
    public void onDestroy() {
        disconnect();
    }
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        EasyChatService getService() {
            // Return this instance of LocalService so clients can call public methods
            return EasyChatService.this;
        }
    }
    
    /**
     * Methods for clients
     */
    public void connect() throws XMPPException {
        SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", 
                SASLXFacebookPlatformMechanism.class);
        SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
        
        xmpp.connect();
    }
    
    public void disconnect() {
        xmpp.disconnect();
    }
    
    public void login(String appId, String accessToken) throws XMPPException {              
        xmpp.login(appId, accessToken, "Easy Chat");
    }
    
    public void sendMessage(String user, String message) throws XMPPException {
        ChatManager chatManager = xmpp.getChatManager();
        Chat chat = chatManager.createChat(user, new MessageListener() {
            
            @Override
            public void processMessage(Chat chat, Message message) {
                Log.v(TAG, "Received message: " + message);
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
