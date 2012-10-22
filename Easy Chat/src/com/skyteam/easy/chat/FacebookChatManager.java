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
import org.jivesoftware.smack.SmackAndroid;

import android.content.Context;

public class FacebookChatManager {
	private XMPPConnection connection;
	private final String SERVER = "chat.facebook.com";
	private final Integer PORT = 5222;
	private final String apiKey = "424998287563509";
	private final String sessionKey = "";
	private final String sessionSecret = "";
	
	public FacebookChatManager(Context context) {
		SmackAndroid.init(context);
		ConnectionConfiguration config = new ConnectionConfiguration(SERVER, PORT);
		config.setSASLAuthenticationEnabled(true);
		connection = new XMPPConnection(config);
	}
	
	public boolean connect() {
		try {
			SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);
		    SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
			connection.connect();
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			connection.disconnect();
		}
		return false;
	}
	
	public void disconnect() {
		connection.disconnect();
	}
	
	public boolean login() {
		try {
			connection.login(apiKey + "|" + sessionKey, sessionSecret, "Application");
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			connection.disconnect();
		}	
		return false;
	}
	
	public String[] getPeople() {
		if (connection.isAuthenticated()) {
			List<String> people = new ArrayList<String>();
			
			Roster roster = connection.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for (RosterEntry entry : entries) {
			    people.add(entry.toString());
			}
			
			return people.toArray(new String[people.size()]);
		} else {
			return new String[0];
		}
	}
}
