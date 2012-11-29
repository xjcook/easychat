package com.skyteam.easy.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;

public class FacebookHelper {
    
    private static final String TAG = "FacebookHelper";
    
    public static final String APPID = "424998287563509";
    public static final String[] PERMISSIONS = {"xmpp_login", "read_mailbox"};
    private static final String TOKEN = "access_token";
    private static final String EXPIRES = "access_expires";
    private static final String KEY = "facebook-session";
    private Context context;
    private Facebook facebook;
    private AsyncFacebookRunner mAsyncRunner;
    
    public FacebookHelper(Context context, Facebook facebook) {
        this.context = context;
        this.facebook = facebook;
        this.mAsyncRunner = new AsyncFacebookRunner(this.facebook);
    }
    
    public void login() {        
        if (! sessionRestore(facebook, context)) {
            facebook.authorize((Activity) context, PERMISSIONS, new DialogListener() {
            
                @Override
                public void onComplete(Bundle values) {
                    Log.v(TAG, "Facebook logged in");
    
                    // Save token
                    sessionSave(facebook, context);
                }
                
                @Override
                public void onCancel() {
                    Log.v(TAG, "Facebook login cancelled");
                    /* TODO what happens if user cancel authorize */
                }
                
                @Override
                public void onFacebookError(FacebookError e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                
                @Override
                public void onError(DialogError e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                    
            });
        }   
    }
    
    public void logout() {
        // Invalidate token in shared preferences
        sessionClear(context);
        
        // Facebook logout
        mAsyncRunner.logout((Activity) context, new RequestListener() {
            
            @Override
            public void onComplete(String response, Object state) {
                Log.v(TAG, "Facebook logged out");
            }
            
            @Override
            public void onFacebookError(FacebookError e, Object state) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
            @Override
            public void onIOException(IOException e, Object state) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
            @Override
            public void onFileNotFoundException(FileNotFoundException e, 
                    Object state) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
            @Override
            public void onMalformedURLException(MalformedURLException e, 
                    Object state) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            
        });
    }
    
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
