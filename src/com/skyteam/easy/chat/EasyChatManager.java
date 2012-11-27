package com.skyteam.easy.chat;

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

import android.util.Log;

public class EasyChatManager {
	
	private static final String TAG = "EasyChatManager";
	private static final String SERVER = "chat.facebook.com";
	private static final Integer PORT = 5222;
	private XMPPConnection xmpp;
	private EasyChatListener mListener;
	
	public EasyChatManager() {
		ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
		config.setDebuggerEnabled(false);
		config.setSASLAuthenticationEnabled(true);
		xmpp = new XMPPConnection(config);
	}
	
	public void login(String appID, String accessToken) throws XMPPException {		
		SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", 
				SASLXFacebookPlatformMechanism.class);
	    SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
	    
	    if (! xmpp.isConnected()) {
	    	xmpp.connect();
	    }
	    
	    xmpp.login(appID, accessToken, "Easy Chat");
	}
	
	public void logout() {
		xmpp.disconnect();
	}
	
	public void sendMessage(String message, String user) throws XMPPException {
	    ChatManager chatManager = xmpp.getChatManager();
	    Chat newChat = chatManager.createChat(user, new MessageListener() {
            
            @Override
            public void processMessage(Chat chat, Message msg) {
                // TODO Auto-generated method stub
                mListener.showMessage(msg.toString());
            }
            
        });
	    
        newChat.sendMessage(message);
	}
	
	public boolean isAuthenticated() {
		return xmpp.isAuthenticated();
	}
	
	public Roster getRoster() {
		return xmpp.getRoster();
	}
	
	public interface EasyChatListener {
	    public void showMessage(String msg);
	}
}
