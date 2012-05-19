package pj.rozkladWKD;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.net.Uri;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pj.lib.errorhandler.Error;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MessageActivity extends SherlockListActivity{

	private static final String TAG = "MessageActivity";
	
	ProgressDialog uploadDialog;
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	private Dialog newMessageDialog;
	
	private boolean error = false;
	
	// variables for message dialog
	Button addButton;
	Button cancelButton;
	
	LinkedList<UserMessage> userMessagesList;
	MessageAdapter messageAdapter;
	
	ImageView messageLoader;
	AnimationDrawable messageLoaderAnimation;
	
	private static SimpleDateFormat formatterDate;

    private boolean refreshButtonClickable = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.rozklad_wkd_messages);
		
		setTitle(R.string.messages);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


		
		messageLoader = (ImageView) findViewById(R.id.user_messages_loader);
        messageLoader.setBackgroundResource(R.layout.loader);
        messageLoaderAnimation = (AnimationDrawable) messageLoader.getBackground();
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		startDownloadingAnimation();
		refreshButtonClickable = false;
        invalidateOptionsMenu();
		new DownloadMessages().execute(DownloadMessages.GET_MESSAGES);

	}
	
	private void setListAdapter() {
		if(RozkladWKD.DEBUG_LOG) {
		    Log.i(TAG, "set list adapter");
	    }
		if(error) {
			Toast.makeText(this, R.string.there_is_no_network_connection, 1000).show();
		}else if(userMessagesList == null) {
			
			Toast.makeText(this, R.string.have_not_found_messages, 1000).show();
		} else {

        	
			messageAdapter = new MessageAdapter(this, R.layout.rozklad_wkd_user_messages_row, userMessagesList, (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)); 
			
			setListAdapter(messageAdapter );
		}		
	}

	
	
	private void showAddMessageDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View contentView = LayoutInflater.from(this).inflate(R.layout.add_new_message_dialog, null);
		builder.setView(contentView);
		
		final EditText username = (EditText) contentView.findViewById(R.id.add_new_message_dialog_username);
		username.setText(settings.getString(RozkladWKD.USERNAME, getString(R.string.noone)));
		final EditText message = (EditText) contentView.findViewById(R.id.add_new_message_dialog_message);
		
		builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				String mess = message.getText().toString();
				String name = username.getText().toString();
				if(mess.equals("") || name.equals("")) {
					Toast.makeText(MessageActivity.this, R.string.fields_cannot_be_empty, Toast.LENGTH_SHORT).show();
				} else {
					showProgressDialog();
					
					new DownloadMessages().execute(DownloadMessages.SEND_MESSAGE, name, mess);
					
					editor = settings.edit();
					editor.putString(RozkladWKD.USERNAME, name);
					editor.commit();
				}
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				dialog.dismiss();
			}
		});
		builder.setTitle(R.string.creating_new_message);
		newMessageDialog = builder.create();

		
		
		
		

		newMessageDialog.show();
		
	}
	
	private void showProgressDialog() {
		uploadDialog = ProgressDialog.show( this, getString(R.string.app_name) , getString(R.string.uploading_message), true);

	}
	private void hideProgressDialog() {
		uploadDialog.dismiss();
	}
	
	private void startDownloadingAnimation() {
    	messageLoader.setVisibility(View.VISIBLE);
    	messageLoaderAnimation.start();
	}
	private void stopDownloadingAnimation() {
		messageLoader.setVisibility(View.INVISIBLE);
		messageLoaderAnimation.stop();
	}
	
	static LinkedList<UserMessage> getUserMessages() throws JSONException, ParseException, ClientProtocolException, NotFoundException, IOException {
		LinkedList<UserMessage> messagesList = new LinkedList<UserMessage>();
			formatterDate = new SimpleDateFormat("HH:mm dd-MM-yyyy");
		
			Calendar cal = Calendar.getInstance();
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
			
			
			
		    nameValuePairs.add(new BasicNameValuePair("requestType", "GET_MESSAGES"));  
		    
		    if(RozkladWKD.DEBUG_LOG) {
			    Log.i(TAG, "getting messages from server");

		    }

		    
		    
		    // Send the HttpPostRequest and receive a JSONObject in return
		    JSONObject jsonObjRecv = pj.rozkladWKD.HttpClient.SendHttpPost(nameValuePairs);
		
		    if(jsonObjRecv == null || jsonObjRecv.getString("result") == "ERROR") {
		    	return null;
		    }
		    
		    
			JSONArray arr = jsonObjRecv.getJSONArray("db");
			JSONObject obj;
			
			
			
			UserMessage message;


			for(int i = 0; i < arr.length(); i++) {
				obj = arr.getJSONObject(i);
				message= new UserMessage();

				
				
				
	            cal.setTime((Date)formatterDate.parse(obj.getString("time_char")));
	            message.time = (Calendar) cal.clone();
	            
	            message.message = obj.getString("message");
	            message.fromWho = obj.getString("USER");
                message.premium = (obj.getString("premium").equals("Y"));
	            
	            messagesList.add(message);
			}

			return messagesList;
		 

	

	}
	
	private Boolean sendNewUserMessage(String username, String mess) throws ClientProtocolException, JSONException, IOException {	
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
	
	    nameValuePairs.add(new BasicNameValuePair("requestType", "SEND_MESSAGE"));
	    nameValuePairs.add(new BasicNameValuePair("username", username));
	    nameValuePairs.add(new BasicNameValuePair("message", mess));
        nameValuePairs.add(new BasicNameValuePair("premium", getResources().getBoolean(R.bool.premium) ? "Y" : "N"));
	    
	    if(RozkladWKD.DEBUG_LOG) {
		    Log.i(TAG, "send message from server");
	    }


	    
	    // Send the HttpPostRequest and receive a JSONObject in return
	    JSONObject jsonObjRecv = pj.rozkladWKD.HttpClient.SendHttpPost(nameValuePairs);
	
	    if(jsonObjRecv == null || jsonObjRecv.getString("result") == "ERROR") {
	    	return false;
	    } else {
	    	return true;
	    }
	}
	
	private class DownloadMessages extends AsyncTask<Object, Object, Object>{
    	public static final int GET_MESSAGES = 1;
    	public static final int SEND_MESSAGE = 2;
    	
    	Exception e = null;
    	
    	int what;
		@Override
		protected Object doInBackground(Object... params) {
			
			what = ((Integer)params[0]).intValue();
			
			switch(what) {
			case GET_MESSAGES:
				try {
					userMessagesList = getUserMessages();
				} catch (JSONException e) {
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}	
					this.e = e;
				} catch (ParseException e) {
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}
					this.e = e;
				} catch (ClientProtocolException e) {
					error = true;
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}
					this.e = e;
				} catch (NotFoundException e) {
					error = true;
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}
					this.e = e;
				} catch (IOException e) {
					error = true;
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}
				}
				break;
			case SEND_MESSAGE:
				String username = (String) params[1];
				String mess = (String) params[2];
				
				try{
					sendNewUserMessage(username, mess);
				} catch (JSONException e) {
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}		
					this.e = e;
				} catch (ClientProtocolException e) {
					error = true;
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}
					this.e = e;
				} catch (NotFoundException e) {
					error = true;
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}
					this.e = e;
				} catch (IOException e) {
					error = true;
					if(RozkladWKD.DEBUG_LOG) {
						Log.w(TAG, e);
					}
				}
				
				break;
			}
			
			
			return null;
		}
		


		@Override
		protected void onPostExecute(Object result) {
			
				switch(what) {
				case GET_MESSAGES:
					setListAdapter();
					stopDownloadingAnimation();
					refreshButtonClickable = true;
                    invalidateOptionsMenu();
					break;
				case SEND_MESSAGE:
					hideProgressDialog();
					if(error) {
						Toast.makeText(MessageActivity.this, R.string.uploading_message_error, Toast.LENGTH_SHORT).show();
					} else {
						error = false;
						newMessageDialog.dismiss();
						Toast.makeText(MessageActivity.this, R.string.uploading_message_success, Toast.LENGTH_SHORT).show();
						
						startDownloadingAnimation();
						new DownloadMessages().execute(DownloadMessages.GET_MESSAGES);
					}
					break;
			
				}
				
				if(e != null) {
					Error.handle(MessageActivity.this, "WKD", e);
				}
		}

	 }

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        if(item.getItemId() == R.id.menu_add_message) {
            showAddMessageDialog();
            return true;
        } else if(item.getItemId() == R.id.menu_refresh_messages) {
            startDownloadingAnimation();
            refreshButtonClickable = false;
            item.setEnabled(false);
            new DownloadMessages().execute(DownloadMessages.GET_MESSAGES);
            return true;
        } else if(item.getItemId() == android.R.id.home) {
            //home buton
            Intent intent = new Intent(this, RozkladWKD.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }


		return super.onOptionsItemSelected(item);
	}



    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {


        com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.messages, menu);

        com.actionbarsherlock.view.MenuItem item = menu.findItem(R.id.menu_refresh_messages);
        if(refreshButtonClickable)
            item.setEnabled(true);
        else
            item.setEnabled(false);

        return true;



    }





	
}
