package com.skyteam.simple.facebook.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PeopleArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
	
	public PeopleArrayAdapter(Context context, String[] values) {
		super(context, R.layout.list_item, values);
		this.context = context;
		this.values = values;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.list_item, parent, false);
		TextView textview = (TextView) rowView.findViewById(R.id.label);
		textview.setText(values[position]);
		
		return rowView;
	}
}
