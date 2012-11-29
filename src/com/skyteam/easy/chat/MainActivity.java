package com.skyteam.easy.chat;

import org.jivesoftware.smack.RosterEntry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.facebook.android.Facebook;
import com.skyteam.easy.chat.MessagesFragment.MessagesFragmentListener;
import com.skyteam.easy.chat.PeopleFragment.PeopleFragmentListener;

public class MainActivity extends FragmentActivity 
    implements PeopleFragmentListener, MessagesFragmentListener {

    private static final String TAG = "MainActivity";
    private final Facebook facebook = new Facebook(FacebookHelper.APPID);
    private final FacebookHelper mFacebookHelper = new FacebookHelper(this, facebook);
    
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.main_activity);
        
        // Log In to Facebook
        mFacebookHelper.login();
        
        // Start ChatService
        Intent intent = new Intent(this, ChatService.class);
        startService(intent);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (isFinishing()) {
            // Log Out from Facebook
            /*mFacebookHelper.logout();*/
            // Stop ChatService 
            /*Intent intent = new Intent(this, ChatService.class);
            stopService(intent);*/
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        facebook.extendAccessToken(this, null);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }
    
    @Override
    public void onPeopleSelected(RosterEntry entry) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMessageSelected(FacebookData data) {
        // TODO Auto-generated method stub
        
    }
    
}
