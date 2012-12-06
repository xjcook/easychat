package com.skyteam.easy.chat;

import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class PeopleFragment extends ListFragment {
    
    public static final String TAG = "PeopleFragment";
    private boolean mDualPane;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.people_fragment, container, false);
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        // Check dualView
        View messagesFrame = getActivity().findViewById(R.id.messages);
        mDualPane = messagesFrame != null && messagesFrame.getVisibility() == View.VISIBLE;
        
        // Add TextWatcher Listener
        EditText filterText = (EditText) getView().findViewById(R.id.people_filter_edittext);
        filterText.addTextChangedListener(mFilterTextWatcher);
	}
    
	@Override
    public void onDestroy() {
	    // Remove TextWatcher Listener
	    EditText filterText = (EditText) getView().findViewById(R.id.people_filter_edittext);
	    filterText.removeTextChangedListener(mFilterTextWatcher);
	    
	    super.onDestroy();
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		RosterEntry entry = (RosterEntry) getListAdapter().getItem(position);
    	showConversation(entry.getUser());
	}
	
	public void show(Collection<RosterEntry> entries) {
		setListAdapter(new PeopleAdapter(getActivity(), entries
				.toArray(new RosterEntry[entries.size()])));
	}

    public void clear() {
		setListAdapter(null);
	}
    
    private void showConversation(String user) {        
        if (mDualPane) {
            // Replace MessagesFragment to ConversationFragment
            ConversationFragment conversationFragment = 
                    ConversationFragment.newInstance(user); 
            FragmentTransaction transaction = getActivity()
                    .getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.messages, conversationFragment, 
                    ConversationFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            // Start new ConversationActivity
            Intent intent = new Intent(getActivity(), ConversationActivity.class);
            intent.putExtra(ConversationActivity.USER, user);
            startActivity(intent);
        }
    }
    
    private TextWatcher mFilterTextWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            PeopleAdapter adapter = (PeopleAdapter) getListAdapter();
            adapter.getFilter().filter(s);
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {}
        
    };
	
}
