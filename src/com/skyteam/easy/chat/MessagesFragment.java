package com.skyteam.easy.chat;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MessagesFragment extends ListFragment {
    
    MessagesFragmentListener mListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MessagesFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() 
                    + " must implement MessagesFragmentListener");
        }
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(null);
		mListener.onMessagesFragmentCreated();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FacebookData data = (FacebookData) getListAdapter().getItem(position);
    	Toast.makeText(getActivity(), data.getThreadId() + " selected", 
    			Toast.LENGTH_LONG).show();
    	mListener.onMessageSelected(data);
	}
	
	public void show(FacebookThread fbThread) {
		setListAdapter(new MessagesAdapter(getActivity(), fbThread.getData()));
	}
	
	public void clear() {
		setListAdapter(null);
	}
	
	public interface MessagesFragmentListener {
        public void onMessagesFragmentCreated();
        public void onMessageSelected(FacebookData data);
    }

}
