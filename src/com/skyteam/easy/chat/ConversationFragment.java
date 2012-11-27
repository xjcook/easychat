package com.skyteam.easy.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ConversationFragment extends Fragment {

    private static final String TAG = "ConversationFragment";
    private ListView listView;
    private ArrayAdapter<String> adapter;
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
	    return inflater.inflate(R.layout.conversation_fragment, container, false);
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView = (ListView) getActivity().findViewById(R.id.messages_listview);
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1);
	}
	
	public void clear() {
		
	}

	public static ConversationFragment newInstance(String user) {
	    ConversationFragment conversationFragment = new ConversationFragment();
	    
	    Bundle args = new Bundle();
	    args.putString("user", user);
	    conversationFragment.setArguments(args);
	    
	    return conversationFragment;
	}
	
}
