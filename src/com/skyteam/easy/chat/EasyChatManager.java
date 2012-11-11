package com.skyteam.easy.chat;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.util.Log;

public class EasyChatManager {
	
	private static final boolean ENABLE_LOG = false;
	private static final String TAG = "EasyChatManager";
	private static final String SERVER = "chat.facebook.com";
	private static final Integer PORT = 5222;
	private XMPPConnection xmpp;
	
	public EasyChatManager() {
		ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
		config.setDebuggerEnabled(ENABLE_LOG);
		config.setSASLAuthenticationEnabled(true);
		xmpp = new XMPPConnection(config);
	}
	
	public void login(String appID, String accessToken) {
		try {		
			SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", 
					SASLXFacebookPlatformMechanism.class);
		    SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
		    
		    if (! xmpp.isConnected()) {
		    	xmpp.connect();
		    }
		    
		    xmpp.login(appID, accessToken, "Easy Chat");
		} catch (XMPPException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			xmpp.disconnect();
		}	
	}
	
	public void logout() {
		xmpp.disconnect();
	}
	
	public boolean isAuthenticated() {
		return xmpp.isAuthenticated();
	}
	
	public Roster getRoster() {
		return xmpp.getRoster();
	}
}
