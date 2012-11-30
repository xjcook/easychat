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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ChatService extends Service {
    
    public static final String TAG = "EasyChatService";
    public static final String SERVER = "chat.facebook.com";
    public static final int PORT = 5222;
    public static final int SLEEP_TIME = 500;
    public static final int ATTEMPT_COUNT = 5;
    private final IBinder mBinder = new LocalBinder();
    private XMPPConnection xmpp;
    
    @Override
    public void onCreate() {    
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
        config.setDebuggerEnabled(false);
        config.setSASLAuthenticationEnabled(true);
        xmpp = new XMPPConnection(config);
        
        SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", 
                SASLXFacebookPlatformMechanism.class);
        SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
        
        connect();
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
                for (int i = 0; i < ATTEMPT_COUNT; i++) {
                    try {        
                        if (! xmpp.isConnected()) {
                            xmpp.connect();
                        } else {
                            return;
                        }    
                        Thread.sleep(SLEEP_TIME);
                    } catch (XMPPException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
                
                // if we can't connect stop service
                stopSelf();
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
                if (! xmpp.isConnected()) {
                    return;
                }
                
                if (! xmpp.isAuthenticated()) { 
                    try {
                        xmpp.login(appId, accessToken, "Easy Chat");
                    } catch (XMPPException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    } catch (IllegalStateException e) {
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
