package com.skyteam.easy.chat;

public class FacebookThread {
	
	private FacebookData[] data;

	public FacebookData[] getData() {
		return data;
	}
	
	public boolean isEmpty() {
		return data.length == 0;
	}
}
