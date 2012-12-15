package com.skyteam.easy.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.facebook.android.Facebook;

public class FacebookHelper {
    
    public static final String TAG = "FacebookHelper";
    public static final String APPID = "424998287563509";
    public static final String[] PERMISSIONS = {"xmpp_login", "read_mailbox"};
    public static final String TOKEN = "access_token";
    public static final String EXPIRES = "access_expires";
    public static final String KEY = "facebook-session";
    
    public static boolean sessionSave(Facebook session, Context context) {
        Editor editor =
            context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
        editor.putString(TOKEN, session.getAccessToken());
        editor.putLong(EXPIRES, session.getAccessExpires());
        return editor.commit();
    }

    public static boolean sessionRestore(Facebook session, Context context) {
        SharedPreferences savedSession =
            context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        session.setAccessToken(savedSession.getString(TOKEN, null));
        session.setAccessExpires(savedSession.getLong(EXPIRES, 0));
        return session.isSessionValid();
    }

    public static void sessionClear(Context context) {
        Editor editor = 
            context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }
    
}
