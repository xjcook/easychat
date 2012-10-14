package com.skyteam.simple.facebook.chat;

import android.os.Bundle;
import android.app.ListActivity;
import android.view.Menu;

public class People extends ListActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String[] people_names = getResources().getStringArray(R.array.people_names);        
        PeopleArrayAdapter adapter = new PeopleArrayAdapter(this, people_names);    
        
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_people, menu);
        return true;
    }
}
