package pj.rozkladWKD;

import java.text.SimpleDateFormat;
import java.util.LinkedList;

import android.content.Context;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<UserMessage>{
	
		private LinkedList<UserMessage> items;
		private LayoutInflater vi;
		private SimpleDateFormat sdf;
	    public MessageAdapter(Context context, int textViewResourceId, LinkedList<UserMessage> items, LayoutInflater vi) {
	            super(context, textViewResourceId, items);
	            this.items = items;
	            this.vi = vi;
	            sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	            View v = convertView;
	            if (v == null) {

	                v = vi.inflate(R.layout.rozklad_wkd_user_messages_row, null);
	
	            }
	            UserMessage o = items.get(position);

	            if (o != null) {

	                	
	                    TextView user = (TextView) v.findViewById(R.id.message_user);
	                    TextView text = (TextView) v.findViewById(R.id.message_text);
	                    
	                    TextView date = (TextView) v.findViewById(R.id.message_date);              
	                   
	                    if (user != null) {
	                    	user.setText(o.fromWho);                          
	                    }
	                    if(text != null) {
	                    	text.setText(o.message);
	                    	Linkify.addLinks(text, Linkify.ALL);
	                    }
	                    if(date != null) {
	                    	date.setText(sdf.format(o.time.getTime()));
	                    }
	                    
	                    
	            }
	            return v;
	    }
}
