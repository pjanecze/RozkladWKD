package pj.rozkladWKD;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pj.lib.errorhandler.Error;





import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;

import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RozkladWKD extends Activity {

	private static final String TAG = "MainActivity";

	public static final Boolean DEBUG_LOG = false;
	public static final String APP_VERSION = "10"; 
	
	
	

	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	static final int DIALOG_INFO = 2;
	static final int DIALOG_LOCAL_SCHEDULE = 3;
	static final int DIALOG_NEW_SCHEDULE = 4;
	
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	public static final String FROM_SPINNER_VALUE = "FROM_SPINNER_VALUE";
	public static final String TO_SPINNER_VALUE = "TO_SPINNER_VALUE";
	public static final String FROM_SPINNER_KEY = "FROM_SPINNER_KEY";
	public static final String TO_SPINNER_KEY = "TO_SPINNER_KEY";
	public static final String LOCAL_SCHEDULE = "local_schedules";
	public static final String CHECK_UPDATES = "check_updates";
	public static final String DB_VERSION = "db_version";
	public static final String NEW_VERSION_AVAILABLE = "new_version";
	public static final String LOCAL_SCHEDULE_CHANGED = "ch_upd_changed";
	
	
	public static final String YEAR = "YEAR";
	public static final String MONTH = "MONTH";
	public static final String DAY = "DAY";
	public static final String HOUR = "HOUR";
	public static final String MINUTE = "MINUTE";
	public static final String PREFS_NAME = "RozkladWKD";


	
	public static final String FROM_SPINNER_TEXT = "FROM_SPINNER_TEXT";
	public static final String TO_SPINNER_TEXT = "TO_SPINNER_TEXT";
	public static final String USERNAME = "username";
	
	private DownloadSchedule downloader = null;
	
	
	/**
	 * Spinnery: od której stacji, do której stacji
	 */
	private Spinner fromSpinner, toSpinner;
	private Button searchButton, dateButton, timeButton;
	private ImageButton changeStationsButton1;
	
	private ImageButton showMessagesButton;
	private CheckBox showNowCheckbox;
	private TextView chooseDateTimeTextView;
	
	
	
	private Calendar choosenDateTime;


	

	private int mYear;
    private int mMonth;
    private int mDay;
	private int mMinute;
	private int mHour;
	private ConnectivityManager connectivityManager;
	
	private Menu menu = null;
	
	/**
	 * Tworzenie activity
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rozklad_wkd_main);

		if(savedInstanceState!=null) {
            restoreProgress(savedInstanceState);

        }
		
		// przypisanie spinnerów
		fromSpinner = (Spinner) findViewById(R.id.from_spinner);
		toSpinner = (Spinner) findViewById(R.id.to_spinner);

		searchButton = (Button) findViewById(R.id.search_button);
		chooseDateTimeTextView = (TextView) findViewById(R.id.choose_date_and_time_text);
		dateButton = (Button) findViewById(R.id.set_date);
		timeButton = (Button) findViewById(R.id.set_time);
		changeStationsButton1 = (ImageButton) findViewById(R.id.change_station_view1);
		
		showMessagesButton = (ImageButton) findViewById(R.id.show_messages);
		showNowCheckbox= (CheckBox) findViewById(R.id.show_now);
		choosenDateTime = Calendar.getInstance();
		mYear = choosenDateTime.get(Calendar.YEAR);
        mMonth = choosenDateTime.get(Calendar.MONTH);
        mDay = choosenDateTime.get(Calendar.DAY_OF_MONTH);
        mHour = choosenDateTime.get(Calendar.HOUR_OF_DAY);
        mMinute = choosenDateTime.get(Calendar.MINUTE);
        
        
        
        
		connectivityManager =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		setButtons();
		setDateTimeForButtons();
		
		populateSpinners();
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		fromSpinner.setSelection(settings.getInt(FROM_SPINNER_KEY, 0));
		toSpinner.setSelection(settings.getInt(TO_SPINNER_KEY, 0));
		
		
		
		showNowCheckbox.setChecked(true);
		
		atFirstStart();
		
	}

	
	private void atFirstStart() {
		if(!settings.contains(LOCAL_SCHEDULE)) {
			showDialog(DIALOG_LOCAL_SCHEDULE);
		} else {
			if(settings.getBoolean(LOCAL_SCHEDULE, false)
					&& settings.getBoolean(CHECK_UPDATES, false)
					&& !settings.getBoolean(NEW_VERSION_AVAILABLE, false))
				if(checkConnection())
					checkScheduleUpdate(false);
				else
					Toast.makeText(this, R.string.no_connection_schedule_check, Toast.LENGTH_LONG).show();
			else if(settings.getBoolean(LOCAL_SCHEDULE, false)
					&& settings.getBoolean(CHECK_UPDATES, false)
					&& settings.getBoolean(NEW_VERSION_AVAILABLE, false))
				Toast.makeText(this, R.string.schedule_new_version, Toast.LENGTH_LONG).show();
			
		}
	}
	private void checkScheduleUpdate(boolean showDialog){
		
		if(settings.getInt(DB_VERSION, -1) == -1) {
			showDialog(DIALOG_NEW_SCHEDULE);
		} else {
			new ScheduleVersionCheck(showDialog).execute(new Integer[] {settings.getInt(DB_VERSION, -1)});
		}
	}

	private void setDateTimeForButtons() {
		
		
        
        dateButton.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                		.append(mDay).append("/")
                        .append(mMonth + 1).append("/") 
                        .append(mYear));
        timeButton.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                		.append(pad(mHour)).append(":")
                        .append(pad(mMinute)));
		
	}

	private void setButtons() {
		searchButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				Long fromSpinnerValue = fromSpinner.getSelectedItemId();
				Long toSpinnerValue = toSpinner.getSelectedItemId();
				
				/* Jeœli znajduje siê na ostatniej stacji nie mo¿e wybrac kierunku dla tej stacji */
				if(fromSpinnerValue == toSpinnerValue || (fromSpinnerValue ==27 && toSpinnerValue == 2) ||
						(fromSpinnerValue == 21 && toSpinnerValue ==1)){
					showAlertDialog(R.string.station_pick_error_title, R.string.station_pick_error_mess, R.string.positive_button);
				}  else {

					
					if(!checkConnection() && !settings.getBoolean(LOCAL_SCHEDULE, false)) {
						showDialog(getString(R.string.there_is_no_network_connection));
					} else {
						Bundle extras = new Bundle();
						extras.putLong(RozkladWKD.FROM_SPINNER_VALUE, fromSpinnerValue);
						extras.putLong(RozkladWKD.TO_SPINNER_VALUE, toSpinnerValue);
						extras.putString(RozkladWKD.FROM_SPINNER_TEXT, fromSpinner.getSelectedItem().toString());
						extras.putString(RozkladWKD.TO_SPINNER_TEXT, toSpinner.getSelectedItem().toString());
						if(!showNowCheckbox.isChecked()) {
							extras.putInt(RozkladWKD.YEAR, mYear);
							extras.putInt(RozkladWKD.MONTH, mMonth);
							extras.putInt(RozkladWKD.DAY, mDay);
							extras.putInt(RozkladWKD.HOUR, mHour);
							extras.putInt(RozkladWKD.MINUTE, mMinute);
						}
						
						Intent intent = new Intent(RozkladWKD.this, SearchResults.class);
						intent.putExtras(extras);
						startActivity(intent);
					}
				}
				
				
				
			}
			
		});
		
		changeStationsButton1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int temp = fromSpinner.getSelectedItemPosition();
				fromSpinner.setSelection(toSpinner.getSelectedItemPosition());
				toSpinner.setSelection(temp);
			}
		});
		showNowCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				showHideDateTimeButtons(isChecked);
				
				
			}

			
		});
		dateButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
				
			}
		});
		timeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
				
			}
		});
		
		
		showMessagesButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RozkladWKD.this, MessageActivity.class);
				startActivity(intent);
		
			}
		});
		
	}

	private void showHideDateTimeButtons(boolean isChecked) {
		if(isChecked) {
			chooseDateTimeTextView.setVisibility(View.GONE);
			dateButton.setVisibility(View.GONE);
			timeButton.setVisibility(View.GONE);
		} else {
			chooseDateTimeTextView.setVisibility(View.VISIBLE);
			dateButton.setVisibility(View.VISIBLE);
			timeButton.setVisibility(View.VISIBLE);
		}
		
	}
	
	/**
	 * Ustawianie elementów Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.rozkladwkd, menu);
	    this.menu = menu;
	    if(!settings.getBoolean(LOCAL_SCHEDULE, false)) {
	    	menu.findItem(R.id.menu_rozkladwkd_synch).setVisible(false);
	    } else {
	    	menu.findItem(R.id.menu_rozkladwkd_synch).setVisible(true);
	    }
		return true;
	}

	

	/**
	 * Gdy zostanie przyciœniety przycisk z menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_rozkladwkd_info:
			showDialog(DIALOG_INFO);
			
			return true;
		case R.id.menu_contribution:
			Intent i = new Intent(Intent.ACTION_VIEW, 
		    Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=BTBYU7GXRE75N&lc=PL&item_name=Support%20for%20application%20development%20%2d%20Pawe%c5%82%20Janeczek&currency_code=PLN&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));
			startActivity(i);
			return true;
		case R.id.menu_other_applications:
			final Intent i1 = new Intent(Intent.ACTION_VIEW, 
		    Uri.parse("market://search?q=pub:%22Pawe³%20Janeczek%22"));
			startActivity(i1);
			return true;
		case R.id.menu_rozkladwkd_settings:
			final Intent intent = new Intent(RozkladWKD.this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_rozkladwkd_synch:
			checkScheduleUpdate(true);
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}


	
	/*
	 * Metoda do tworzenia alertu: pobiera tytul, wiadomosc i tekst przycisku pozytywnego (jako liczby R.string.*)
	 * Jeœli nie chcesz tytulu b¹dz wiadomoœci to wpisujesz -1.
	 */
	void showAlertDialog(int title, int message, int positiveButtonText){
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		if(title != -1) alert.setTitle(title);
		if(message != -1) alert.setMessage(message);
		alert.setPositiveButton(positiveButtonText, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
			
		});
		alert.show();
		
	}
	
	

	
	
	/**
	 * Ustawianie adapterów dla spinnerów
	 */
	private void populateSpinners() {

		ArrayAdapter<CharSequence> from_aa, to_aa;
		// ustawianie spinnera from
		from_aa = ArrayAdapter.createFromResource(this, R.array.stations,
				android.R.layout.simple_spinner_item);

		from_aa
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		fromSpinner.setAdapter(from_aa);

		// ustawianie spinnera to
		to_aa = ArrayAdapter.createFromResource(this, R.array.stations,
				android.R.layout.simple_spinner_item);

		to_aa
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		toSpinner.setAdapter(to_aa);
	}

	
	
	@Override
	protected void onStop() {
		super.onStop();
		editor = settings.edit();
		editor.putInt(FROM_SPINNER_KEY, fromSpinner.getSelectedItemPosition());
		editor.putInt(TO_SPINNER_KEY, toSpinner.getSelectedItemPosition());
		editor.commit();
	}



	private Boolean checkConnection() {
		
		if(!connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
			return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
		} else {
			return true;
		}
		
	}
	
	private void showDialog(String text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(text)
		       .setCancelable(true)
		       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	
	
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG_ID:
	        return new DatePickerDialog(this,mDateSetListener,mYear, mMonth, mDay);
	    case TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	                mTimeSetListener, mHour, mMinute, true);
	    case DIALOG_INFO:

	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.info);
			builder.setMessage(R.string.main_info_text);
			builder.setIcon(R.drawable.ic_launcher_wkd);
			return builder.create();
	    case DIALOG_LOCAL_SCHEDULE:
	    	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	    	builder1.setMessage(R.string.do_you_want_local_schedule)
	    			.setCancelable(false)
	    			.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							editor = settings.edit();
							editor.putBoolean(LOCAL_SCHEDULE, true);
							editor.putBoolean(CHECK_UPDATES, true);
							editor.putBoolean(NEW_VERSION_AVAILABLE, true);
							editor.commit();
							
							downloader = new DownloadSchedule();
							downloader.execute(new Void[] {});
						}
					})
					.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							editor = settings.edit();
							editor.putBoolean(LOCAL_SCHEDULE, false);
							editor.putBoolean(CHECK_UPDATES, false);
							editor.commit();
							
						}
					});
	    	builder1.setIcon(R.drawable.ic_launcher_wkd);
	    	builder1.setTitle("Rozk¸ad WKD");
	    	return builder1.create();
	    case DIALOG_NEW_SCHEDULE:
	    	editor = settings.edit();
			editor.putBoolean(NEW_VERSION_AVAILABLE, true);
			editor.commit();
	    	AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
	    	builder2.setMessage(R.string.new_schedule)
	    			.setCancelable(false)
	    			.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							downloader = new DownloadSchedule();
							downloader.execute(new Void[] {});
						}
					})
					.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(RozkladWKD.this, "Je¿eli chcesz wy³¹czyæ automatyczne sprawdzanie aktualizacji rozk³adu przejdŸ do ustawieñ.", Toast.LENGTH_LONG).show();
							
						}
					});
	    	builder2.setIcon(R.drawable.ic_launcher_wkd);
	    	builder2.setTitle("Rozk³ad WKD");
	    	return builder2.create();
	    			
	    }
	    return null;
	}
	
	private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

            

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				mYear = year;
				mMonth = monthOfYear;
                mDay = dayOfMonth;
                setDateTimeForButtons();
				
			}
        };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
           

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mHour = hourOfDay;
                mMinute = minute;
                setDateTimeForButtons();
				
			}
        };
        
    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    
    
    
    private class ScheduleVersionCheck extends AsyncTask<Integer, Void, Boolean> {

    	ProgressDialog dialog;
    	private boolean showDialog = false;
    	
    	private Exception e;
    	
    	public ScheduleVersionCheck(boolean showDialog) {
    		super();
    		this.showDialog = showDialog;
    	}
    	
    	
		@Override
		protected void onPreExecute() {
			
			if(showDialog) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
				dialog = ProgressDialog.show(RozkladWKD.this, getString(R.string.app_name), getString(R.string.checking_schedule_version));
				dialog.setIcon(R.drawable.ic_launcher_wkd);
				dialog.setTitle(getString(R.string.app_name));
			}
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			final int localVersion = params[0];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
			
			
			
		    nameValuePairs.add(new BasicNameValuePair("requestType", "CHECK_VER"));  
		    
		    if(DEBUG_LOG) {
			    Log.i(TAG, "getting schedule version from server");

		    }

		    
		    int ver;
		    // Send the HttpPostRequest and receive a JSONObject in return
		    JSONObject jsonObjRecv;
			try {
				jsonObjRecv = pj.rozkladWKD.HttpClient.SendHttpPost(nameValuePairs);
				if(jsonObjRecv == null || jsonObjRecv.getString("result") == "ERROR") {
			    	return null;
			    }
			    
			    
			    ver = jsonObjRecv.getInt("db");
			} catch (ClientProtocolException e) {
				Log.e(TAG, e.getMessage(), e);
				this.e = e;
				return null;
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage(), e);
				this.e = e;
				return null;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return null;
			}
		
		    
			
		    return localVersion == ver;
			
		}

		@Override
		protected void onPostExecute(Boolean result) {
			
			if(e != null) {
				if(showDialog) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					dialog.dismiss();
				}
				Error.handle(RozkladWKD.this, "WKD", e);
			} else {
				if(result != null && result.booleanValue() == false) {
					//pobierz now¹ wersje
					if(showDialog) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
						dialog.dismiss();
					}
					editor = settings.edit();
					editor.putBoolean(NEW_VERSION_AVAILABLE, true);
					editor.commit();
					showDialog(DIALOG_NEW_SCHEDULE);
				} else if(result != null && result.booleanValue() == true) {
					if(showDialog) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
						dialog.dismiss();
						Toast.makeText(RozkladWKD.this, getString(R.string.have_newest_schedule), Toast.LENGTH_LONG).show();
					}
					
					editor = settings.edit();
					editor.putBoolean(NEW_VERSION_AVAILABLE, false);
					editor.commit();
				} else {
					if(showDialog) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
						dialog.dismiss();
					}
					Toast.makeText(RozkladWKD.this, getString(R.string.error_during_version_check), Toast.LENGTH_LONG).show();
				}
			}
		}
		
    }
    
    private class DownloadSchedule extends AsyncTask<Void, Integer, Boolean> {

    	public ProgressDialog progressDialog;
    	
    	LocalDB localDB;
    	
    	private int dbVersion;
    	
    	private Exception e;
    	
    	
		@Override
		protected void onPreExecute() {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			createProgressDialog();
			
			localDB = new LocalDB(RozkladWKD.this);
		}
		
		

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0]);
			if(values[0] == 10) {
				progressDialog.setMessage("Trwa wgrywanie danych do lokalnej bazy");
			}
		}



		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
			
			
			
		    nameValuePairs.add(new BasicNameValuePair("requestType", "GET_ALL_SCHEDULE"));  
		    
		    if(DEBUG_LOG) {
			    Log.i(TAG, "getting all schedule from server");

		    }


		    
		    




		    // Send the HttpPostRequest and receive a JSONObject in return
		    JSONObject jsonObjRecv;
			try {
				jsonObjRecv = pj.rozkladWKD.HttpClient.SendHttpPost(nameValuePairs);
				if(DEBUG_LOG)
			    	Log.i(TAG, "0");
				if(jsonObjRecv == null || jsonObjRecv.getString("result") == "ERROR") {
			    	return false;
			    }
			    publishProgress(10);
			    if(DEBUG_LOG)
			    	Log.i(TAG, "1");
			    JSONObject arr = jsonObjRecv.getJSONObject("db");
			    
			    if(DEBUG_LOG)
			    	Log.i(TAG, arr.toString());
			    JSONObject obj;
			    if(DEBUG_LOG)
			    	Log.i(TAG, "arr.length() = " + arr.length());
			    
				localDB.removeSchedule();
			    
			    for(int i = 0; i < arr.length(); i++) {
			    	if(arr.has(Integer.toString(i))) {
				    	obj = arr.getJSONObject(Integer.toString(i));
				    	
				    	localDB.addToSchedule(obj);
				    	publishProgress(10 + (int)(((float)i/(float)arr.length()) * 90));
			    	}
			    	
			    }
			    
			    dbVersion = arr.getInt("DB_VERSION");
			    
			    localDB.getWritableDatabase().close();
			    //ver = jsonObjRecv.getInt("db");
			} catch (ClientProtocolException e) {
				if(DEBUG_LOG)
					Log.e(TAG, e.getMessage(),e);
				
				this.e = e;
				return false;
			} catch (JSONException e) {
				if(DEBUG_LOG)
					Log.e(TAG, e.getMessage(),e);
				this.e = e;
				return false;
			} catch (IOException e) {
				if(DEBUG_LOG)
					Log.e(TAG, e.getMessage(),e);
				//this.e = e;
				return false;
			}
			
			publishProgress(100);
			
			return true;
		}



		@Override
		protected void onPostExecute(Boolean result) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			downloader = null;
			progressDialog.dismiss();
			if(e != null) {
				Error.handle(RozkladWKD.this, "WKD", e);
			} else {
				if(result.booleanValue()) {
					editor = settings.edit();
					editor.putBoolean(NEW_VERSION_AVAILABLE, false);
					editor.putInt(DB_VERSION, dbVersion);
					editor.commit();
					Toast.makeText(RozkladWKD.this, getString(R.string.success_schedule_download), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(RozkladWKD.this, getString(R.string.error_schedule_download), Toast.LENGTH_LONG).show();
				}
			}
		}



		public void createProgressDialog() {
			progressDialog = new ProgressDialog(RozkladWKD.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage(getString(R.string.downloading_schedule));
			progressDialog.setCancelable(false);
			progressDialog.setIcon(R.drawable.ic_launcher_wkd);
			progressDialog.setTitle(R.string.app_name);
			progressDialog.show();
		}
		
		
    	
    }
 

	@Override
	protected void onResume() {
		super.onResume();
		
		if(settings.getBoolean(LOCAL_SCHEDULE_CHANGED, false)) {
			editor = settings.edit();
			editor.putBoolean(LOCAL_SCHEDULE_CHANGED, false);
			editor.commit();
			
			if(settings.getBoolean(LOCAL_SCHEDULE, false)) {
				//Log.i(TAG, "menu: " + menu);
				checkScheduleUpdate(true);
				if(menu != null) {
					menu.findItem(R.id.menu_rozkladwkd_synch).setVisible(true);
				}
			} else {
				//Log.i(TAG, "menu: " + menu);
				if(menu != null) {
					menu.findItem(R.id.menu_rozkladwkd_synch).setVisible(false);
				}
			}
		}
		
	}
	
	private static WeakReference<ProgressDialog> weakDialog;
	
	@Override
    protected void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        if(downloader != null) {
        	weakDialog = new WeakReference<ProgressDialog>(downloader.progressDialog);
        	saveState.putBoolean("waiting",true);
        }
    }
	
	private void restoreProgress(Bundle savedInstanceState) {
		boolean waiting=savedInstanceState.getBoolean("waiting");
        if (waiting) {
            
            ProgressDialog refresher=(ProgressDialog) weakDialog.get();
            refresher.dismiss();
            downloader.createProgressDialog();
        }
	}


}

