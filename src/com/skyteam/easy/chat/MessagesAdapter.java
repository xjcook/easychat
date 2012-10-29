package com.skyteam.easy.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessagesAdapter extends ArrayAdapter<FacebookData> {
	private final Context context;
	private final FacebookData[] data;
	
	static class ViewHolder {
		public TextView text;
	}
	
	public MessagesAdapter(Context context, FacebookData[] data) {
		super(context, R.layout.message_item, data);
		this.context = context;
		this.data = data;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
	
		if (rowView == null) {
			//LayoutInflater inflater = context.getLayoutInflater();
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.message_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.message_label);
			rowView.setTag(viewHolder);
		}
		
		ViewHolder holder = (ViewHolder) rowView.getTag();
		String s = data[position].snippet;
		holder.text.setText(s);
		
		return rowView;
	}
}
