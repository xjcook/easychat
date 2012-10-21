package com.skyteam.easy.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.skyteam.easy.chat.R;

public class People extends ListActivity {
	public String[] peopleNames;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);        
        new FacebookConnectTask().execute(this);
    }
    
    private class FacebookConnectTask extends AsyncTask<Context, Void, PeopleArrayAdapter> {

		@Override
		protected PeopleArrayAdapter doInBackground(Context... contexts) {
			for (Context context : contexts) {
				FacebookChatManager fbChat = new FacebookChatManager(context);
		        if (fbChat.connect()) {
		        	if (fbChat.login("xjcook@gmail.com", "nqgGBp69OK")) {
		        		String[] names = fbChat.getPeople();
		        		PeopleArrayAdapter adapter = new PeopleArrayAdapter(context, names);
		        		return adapter;
		        	}
		        }
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(PeopleArrayAdapter adapter) {
			if (adapter != null)
				setListAdapter(adapter);		
		}
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_people, menu);
        return true;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	String item = (String) getListAdapter().getItem(position);
    	Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
    }
    
}
