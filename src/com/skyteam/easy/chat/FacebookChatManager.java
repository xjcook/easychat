package com.skyteam.easy.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.util.Log;

public class FacebookChatManager {
	
	private static final String TAG = "FacebookChatManager";
	private static final String SERVER = "chat.facebook.com";
	private static final Integer PORT = 5222;
	private boolean isConnected = false;
	private XMPPConnection connection;
	
	public FacebookChatManager() {
		ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
		config.setDebuggerEnabled(true);
		config.setSASLAuthenticationEnabled(true);
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
		connection = new XMPPConnection(config);
	}
	
	public boolean login(String appID, String accessToken) {
		try {
			SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);
		    SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
		    connection.connect();
		    connection.login(appID, accessToken, "Easy Chat");
		    isConnected = true;
			return true;
		} catch (XMPPException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			connection.disconnect();
			isConnected = false;
		}	
		return false;
	}
	
	public void logout() {
		connection.disconnect();
	}
	
	public boolean isConnected() {
		return this.isConnected;
	}
	
	public String[] getPeople() {
		if (connection.isAuthenticated()) {
			List<String> people = new ArrayList<String>();
			
			Roster roster = connection.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			
			for (RosterEntry entry : entries)
			    people.add(entry.toString());
			
			return people.toArray(new String[people.size()]);
		} else {
			return new String[]{};
		}
	}
}
