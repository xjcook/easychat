package com.skyteam.easy.chat;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ChatService extends Service {
    
    public static final String TAG = "EasyChatService";
    public static final String SERVER = "chat.facebook.com";
    public static final int PORT = 5222;
    public static final int ATTEMPT_COUNT = 10;
    private final IBinder mBinder = new LocalBinder();
    private XMPPConnection xmpp;
    private String mAccessToken;
    private MessageListener mMessageListener = new MessageListener() {
        
        @Override
        public void processMessage(Chat chat, Message message) {
            String body = message.getBody();
            
            if (body != null) {
                Log.v(TAG, "Received message: " + body);
                Intent intent = new Intent(ConversationFragment.ACTION);
                intent.putExtra(ConversationFragment.USER, chat.getParticipant());
                intent.putExtra(ConversationFragment.MESSAGE, body);
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(intent);
                createNotification(chat.getParticipant(), body);
            }
        }
        
    };
    
    @Override
    public void onCreate() {  
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
        config.setDebuggerEnabled(true);
        config.setSASLAuthenticationEnabled(true);
        xmpp = new XMPPConnection(config);
        SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", 
                SASLXFacebookPlatformMechanism.class);
        SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service");
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        mAccessToken = intent.getStringExtra(FacebookHelper.TOKEN);
        connect();
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Bound to " + intent);
        return mBinder;
    }
    
    @Override
    public void onDestroy() {
        Log.i(TAG, "Service is killed");
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
                        if (xmpp.isConnected()) {
                            login(FacebookHelper.APPID, mAccessToken);
                            return;
                        } else {
                            xmpp.connect();
                            Thread.sleep(MainActivity.SLEEP_TIME);
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
                            listenChat();
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
    
    public void listenChat() {
        if (xmpp.isConnected()) {
            xmpp.getChatManager().addChatListener(new ChatManagerListener() {
                
                @Override
                public void chatCreated(Chat chat, boolean createdLocally) {
                    if (! createdLocally) {
                        chat.addMessageListener(mMessageListener);
                    }
                }
                
            });
        } else {
            // TODO connect and listen
            Log.e(TAG, "listenChat is not connected!");
        }
    }
    
    public void sendMessage(String user, String message) throws XMPPException {
        if (xmpp.isConnected() && xmpp.isAuthenticated()) {        
            ChatManager chatManager = xmpp.getChatManager();
            Chat chat = chatManager.createChat(user, mMessageListener);
            chat.sendMessage(message);
        } else {
            // TODO connect and send message
            Log.e(TAG, "sendMessage() is not connected!");
        }
    }
    
    public void createNotification(String user, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(user)
                .setContentText(message);
        
        Intent resultIntent = new Intent(this, ConversationActivity.class);
        resultIntent.putExtra(ConversationActivity.USER, user);
        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ConversationActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, 
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        
        NotificationManager mNotificationManager = (NotificationManager) 
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(12345, mBuilder.build());
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
