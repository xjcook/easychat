package com.skyteam.easy.chat;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PeopleAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final ArrayList<String> entries;
	
	static class ViewHolder {
		public TextView text;
	}
	
	public PeopleAdapter(Context context, ArrayList<String> entries) {
		super(context, R.layout.people_item, entries);
		this.context = context;
		this.entries = entries;
		Collections.sort(entries);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		
		if (rowView == null) {
			//LayoutInflater inflater = context.getLayoutInflater();
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.people_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.people_label);
			rowView.setTag(viewHolder);
		}
		
		ViewHolder holder = (ViewHolder) rowView.getTag();
		String name = entries.get(position);
		holder.text.setText(name);
		
		return rowView;
	}

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(entries);
        super.notifyDataSetChanged();
    }
	
}
