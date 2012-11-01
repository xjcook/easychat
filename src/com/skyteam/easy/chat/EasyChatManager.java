package com.skyteam.easy.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.content.Context;
import android.util.Log;

public class EasyChatManager {
	
	private static final boolean DEBUG = false;
	private static final String TAG = "EasyChatManager";
	private static final String SERVER = "chat.facebook.com";
	private static final Integer PORT = 5222;
	private XMPPConnection xmpp;
	
	public EasyChatManager(Context context) {
		SmackAndroid.init(context);
		ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
		config.setDebuggerEnabled(DEBUG);
		config.setSASLAuthenticationEnabled(true);
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
		xmpp = new XMPPConnection(config);
	}
	
	public boolean login(String appID, String accessToken) {
		try {		
			SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", 
					SASLXFacebookPlatformMechanism.class);
		    SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
		    
		    if (! xmpp.isConnected())
		    	xmpp.connect();
		    
		    xmpp.login(appID, accessToken, "Easy Chat");
			
		    return true;
		    
		} catch (XMPPException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			xmpp.disconnect();
		}	
		
		return false;
	}
	
	public void logout() {
		xmpp.disconnect();
	}
	
	public boolean isAuthenticated() {
		return xmpp.isAuthenticated();
	}
	
	public String[] getPeople() {
		// TODO Need to be faster
		if (xmpp.isAuthenticated()) {
			List<String> people = new ArrayList<String>();
			
			Roster roster = xmpp.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			
			for (RosterEntry entry : entries)
			    people.add(entry.getName());
			
			return people.toArray(new String[people.size()]);
		} else {
			return null;
		}
	}
	
	public Roster getRoster() {
		if (xmpp.isAuthenticated())
			return xmpp.getRoster();
		else
			return null;
	}
}
