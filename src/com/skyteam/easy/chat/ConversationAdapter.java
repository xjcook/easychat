package com.skyteam.easy.chat;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ConversationAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> messages;
    
    static class ViewHolder {
        public TextView text;
    }
    
    public ConversationAdapter(Context context, ArrayList<String> messages) {
        super(context, R.layout.message_item, messages);
        this.context = context;
        this.messages = messages;
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
        String name = messages.get(position);
        holder.text.setText(name);
        
        return rowView;
    }

    @Override
    public void notifyDataSetChanged() {
        // Sort by newest messages
        // TODO remove reversed messages + add scrollbar to newest messages
        Collections.reverse(messages);
        super.notifyDataSetChanged();
    }
    
}
