package com.skyteam.simple.facebook.chat;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class People extends ListActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_people);
        
        String[] people_names = getResources().getStringArray(R.array.people_names);
        
        PeopleArrayAdapter adapter = new PeopleArrayAdapter(this, people_names);          
        setListAdapter(adapter);
        
        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_list_item_1, android.R.id.text1, people_names);  */
        
        /* ListView listView = (ListView) findViewById(R.id.list_people);  
        listView.setAdapter(adapter);        
        listView.setOnItemClickListener(new OnItemClickListener() {
        	
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getApplicationContext(), 
						"Click ListItem Number " + position, Toast.LENGTH_LONG).show();				
			}
        	
		});*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_people, menu);
        return true;
    }
}
