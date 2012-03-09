package pj.rozkladWKD;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResults extends ListActivity implements OnItemClickListener {

	private static final String TAG = "SearchResults";

	private static final int DIALOG_DETAILS = 101;

	private Boolean error = false;

	private Boolean showAllSchedules = false;

	Long fromSpinnerValue;
	Long toSpinnerValue;
	String fromSpinnerText;
	String toSpinnerText;

	LinkedList<Schedule> schedulesList;

	Calendar cal;
	Calendar tempCal;

	Calendar searchTime;

	ScheduleAdapter scheduleAdapter;
	ProgressDialog downloadDialog;

	SharedPreferences settings;
	SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rozklad_wkd_search_results);

		if(android.os.Build.VERSION.SDK_INT >=11) {
			android.app.ActionBar actionBar = getActionBar();
		    actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);

		getExtras();

		setTitle(fromSpinnerText + " > " + toSpinnerText);
		if (RozkladWKD.DEBUG_LOG) {
			Log.i(TAG, "creating search results");
		}

		showProgressDialog();
		new DownloadSchedules().execute();

		getListView().setOnItemClickListener(this);
	}

	private void showProgressDialog() {
		downloadDialog = ProgressDialog.show(this,
				getString(R.string.app_name),
				getString(R.string.downloading_schedules), true);

	}

	private void hideProgressDialog() {
		downloadDialog.dismiss();
	}

	private void getDataFromServer() throws JSONException, ParseException,
			ClientProtocolException, NotFoundException, IOException {

		Calendar cal = Calendar.getInstance();
		if (searchTime != null) {
			cal = searchTime;
		}

		Date a = cal.getTime();
		if (RozkladWKD.DEBUG_LOG) {
			Log.i("TAG", "android time:" + a.getHours() + ":" + a.getMinutes());
		}

		schedulesList = getRozklad(fromSpinnerValue, toSpinnerValue,
				a.getHours() + ":" + a.getMinutes(), getResources());
		if (RozkladWKD.DEBUG_LOG) {
			Log.i("TAG", "from spinner:" + fromSpinnerValue + "; to spinner: "
					+ toSpinnerValue);
		}

	}

	private void setListAdapter() {
		if (RozkladWKD.DEBUG_LOG) {
			Log.i(TAG, "set list adapter");
		}
		if (error) {
			Toast.makeText(this, R.string.there_is_no_network_connection, 1000)
					.show();
		} else if (schedulesList == null) {

			Toast.makeText(this, R.string.have_not_found_connections, 1000)
					.show();
		} else {
			cal = Calendar.getInstance();

			scheduleAdapter = new ScheduleAdapter(this,
					R.layout.rozklad_wkd_search_results_row, schedulesList);

			setListAdapter(scheduleAdapter);
		}
	}

	/*
	 * Pobieranie danych z RozkladWKD activity o stacji poczatkowej i kierunku
	 * jazdy
	 */
	private void getExtras() {

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			fromSpinnerValue = extras.getLong(RozkladWKD.FROM_SPINNER_VALUE);
			toSpinnerValue = extras.getLong(RozkladWKD.TO_SPINNER_VALUE);
			fromSpinnerText = extras.getString(RozkladWKD.FROM_SPINNER_TEXT);
			toSpinnerText = extras.getString(RozkladWKD.TO_SPINNER_TEXT);
			if (extras.containsKey(RozkladWKD.YEAR)) {
				searchTime = Calendar.getInstance();
				searchTime.set(Calendar.YEAR, extras.getInt(RozkladWKD.YEAR));
				searchTime.set(Calendar.MONTH, extras.getInt(RozkladWKD.MONTH));
				searchTime.set(Calendar.DAY_OF_MONTH,
						extras.getInt(RozkladWKD.DAY));
				searchTime.set(Calendar.HOUR_OF_DAY,
						extras.getInt(RozkladWKD.HOUR));
				searchTime.set(Calendar.MINUTE,
						extras.getInt(RozkladWKD.MINUTE));
			}
		} else {
			this.finish();
		}

	}

	String minute;
	String hour;

	private class ScheduleAdapter extends ArrayAdapter<Schedule> {
		private LinkedList<Schedule> items;
		private int changedPosition = 1;

		public ScheduleAdapter(Context context, int textViewResourceId,
				LinkedList<Schedule> items) {
			super(context, textViewResourceId, items);

			if (!showAllSchedules) {
				int size = items.size();
				for (int i = 0; i < size; i++) {
					if (!checkIfScheduleExist(searchTime, items.get(i))) {
						items.remove(i);
						size--;
					}
				}
			}
			this.items = items;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				v = vi.inflate(R.layout.rozklad_wkd_search_results_row, null);

			}
			Schedule o = items.get(position);
			if (RozkladWKD.DEBUG_LOG) {
				Log.i(TAG, "item type=" + o.type + " fromtime=" + o.fromTime);
			}
			if (o != null) {

				// if(!showAllSchedules) {
				//
				// if(!checkIfScheduleExist(searchTime, o)) {
				// LayoutInflater vi =
				// (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				// return
				// vi.inflate(R.layout.rozklad_wkd_search_results_null_row,
				// null);
				//
				// }
				// }

				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);

				TextView et = (TextView) v.findViewById(R.id.elapsedtime);
				TextView st = (TextView) v.findViewById(R.id.schedule_type);

				if (tt != null) {
					tt.setTextColor(R.color.black);
					if (o.fromTime.get(Calendar.HOUR_OF_DAY) >= 0
							&& o.fromTime.get(Calendar.HOUR_OF_DAY) <= 9) {
						hour = "0" + o.fromTime.get(Calendar.HOUR_OF_DAY);
					} else {
						hour = String.valueOf(o.fromTime
								.get(Calendar.HOUR_OF_DAY));
					}
					if (o.fromTime.get(Calendar.MINUTE) >= 0
							&& o.fromTime.get(Calendar.MINUTE) <= 9) {
						minute = "0" + o.fromTime.get(Calendar.MINUTE);
					} else {
						minute = String
								.valueOf(o.fromTime.get(Calendar.MINUTE));
					}

					tt.setText("Odjazd: " + hour + ":" + minute);
				}
				if (bt != null) {
					bt.setTextColor(R.color.very_dark_grey);
					if (o.toTime.get(Calendar.HOUR_OF_DAY) >= 0
							&& o.toTime.get(Calendar.HOUR_OF_DAY) <= 9) {
						hour = "0" + o.toTime.get(Calendar.HOUR_OF_DAY);
					} else {
						hour = String.valueOf(o.toTime
								.get(Calendar.HOUR_OF_DAY));
					}
					if (o.toTime.get(Calendar.MINUTE) >= 0
							&& o.toTime.get(Calendar.MINUTE) <= 9) {
						minute = "0" + o.toTime.get(Calendar.MINUTE);
					} else {
						minute = String.valueOf(o.toTime.get(Calendar.MINUTE));
					}
					bt.setText("Przyjazd: " + hour + ":" + minute);
				}

				if (st != null) {
					st.setTextColor(R.color.very_dark_grey);
					st.setText(o.type);
				}

				if (et != null) {

					if (searchTime != null) {
						et.setTextColor(getResources().getColor(
								R.color.search_list_neutral));
						et.setText((position + 1) + ".");
						// changedPosition ++;
						// if(position == items.size() -1) {
						// changedPosition = 1;
						// }
					} else {

						Long diff = o.fromTime.getTimeInMillis()
								- cal.getTimeInMillis();

						tempCal = Calendar.getInstance();
						tempCal.setTimeInMillis(Math.abs(diff));
						tempCal.set(Calendar.MINUTE,
								tempCal.get(Calendar.MINUTE) + 1);

						if (diff < 0) {
							et.setTextColor(getResources().getColor(
									R.color.search_list_red));
							et.setText("- " + tempCal.get(Calendar.MINUTE)
									+ "m");

						} else if (tempCal.get(Calendar.HOUR_OF_DAY) == 1
								&& tempCal.get(Calendar.MINUTE) >= 0
								&& tempCal.get(Calendar.MINUTE) <= 1) {
							et.setTextColor(getResources().getColor(
									R.color.search_list_red));
							et.setText(tempCal.get(Calendar.MINUTE) + "m");
						} else if (tempCal.get(Calendar.HOUR_OF_DAY) == 1
								&& tempCal.get(Calendar.MINUTE) > 1
								&& tempCal.get(Calendar.MINUTE) <= 10) {
							et.setTextColor(getResources().getColor(
									R.color.search_list_yellow));
							et.setText(tempCal.get(Calendar.MINUTE) + "m");
						} else {
							et.setTextColor(getResources().getColor(
									R.color.search_list_neutral));
							if (tempCal.get(Calendar.HOUR_OF_DAY) == 1) {
								et.setText(tempCal.get(Calendar.MINUTE) + "m");
							} else {
								et.setText((tempCal.get(Calendar.HOUR_OF_DAY) - 1)
										+ "h"
										+ tempCal.get(Calendar.MINUTE)
										+ "m");
							}

						}
					}
				}
			}
			return v;
		}

		private Boolean checkIfScheduleExist(Calendar calendar, Schedule o) {

			if (calendar == null) {
				calendar = Calendar.getInstance();
			}
			if (o.type == null || o.type.equals("")) {
				return true;
			} else {
				if (o.type.equals("D")) {
					return checkIfItIsD(calendar);
				} else if (o.type.equals("D1")) {
					if (!checkIfItIsD(calendar)) {

						return false;
					}
					if (checkIfItIs1(calendar)) {
						return false;
					}
					return true;
				} else if (o.type.equals("D2")) {
					if (!checkIfItIsD(calendar))
						return false;
					if (!checkIfItIs1(calendar))
						return false;
					return true;
				} else if (o.type.equals("A")) {
					if (checkIfItIsD(calendar)) {
						return true;
					} else {
						if (fromSpinnerValue >= 18 && fromSpinnerValue <= 20) {
							return true;
						} else {
							return false;
						}
					}
				} else if (o.type.equals("C3")) {
					if (checkIfItIs1(calendar)) {
						return true;
					} else {

						if (calendar.get(Calendar.DAY_OF_WEEK) == 1
								|| calendar.get(Calendar.DAY_OF_WEEK) == 7
								|| isHoliday(calendar)) {
							return true;
						} else {
							return false;
						}
					}
				}
			}
			return false;
		}

		private boolean checkIfItIs1(Calendar calendar) {
			if (calendar.get(Calendar.MONTH) == 6
					|| calendar.get(Calendar.MONTH) == 7)
				return true;
			else
				return false;
		}

		private Boolean checkIfItIsD(Calendar calendar) {

			if (calendar.get(Calendar.DAY_OF_WEEK) >= 2
					&& calendar.get(Calendar.DAY_OF_WEEK) <= 6
					&& !isHoliday(calendar)) {
				return true;
			} else {
				return false;
			}

		}

		private boolean isHoliday(Calendar calendar) {
			if (calendar.get(Calendar.MONTH) == 0
					&& calendar.get(Calendar.DAY_OF_MONTH) == 1) {
				return true;
			} else if (calendar.get(Calendar.MONTH) == 4
					&& (calendar.get(Calendar.DAY_OF_MONTH) == 1 || calendar
							.get(Calendar.DAY_OF_MONTH) == 3)) {
				return true;
			} else if (calendar.get(Calendar.MONTH) == 7
					&& calendar.get(Calendar.DAY_OF_MONTH) == 15) {
				return true;
			} else if (calendar.get(Calendar.MONTH) == 10
					&& (calendar.get(Calendar.DAY_OF_MONTH) == 1 || calendar
							.get(Calendar.DAY_OF_MONTH) == 11)) {
				return true;
			} else if (calendar.get(Calendar.MONTH) == 11
					&& (calendar.get(Calendar.DAY_OF_MONTH) == 25 || calendar
							.get(Calendar.DAY_OF_MONTH) == 26)) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Ustawianie elementów Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.searchresults, menu);
		return true;
	}

	/**
	 * Gdy zostanie przyciœniety przycisk z menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.menu_searchresults_show_all:
			if (showAllSchedules) {
				showAllSchedules = false;
				item.setTitle(R.string.show_all);
			} else {
				showAllSchedules = true;
				item.setTitle(R.string.dont_show_all);
			}
			showProgressDialog();
			new DownloadSchedules().execute();

			return true;
		case R.id.menu_searchresults_refresh:
			showProgressDialog();
			new DownloadSchedules().execute();

			return true;
		case R.id.menu_searchresults_info:
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setMessage(getString(R.string.info_text));

			builder1.setIcon(R.drawable.ic_launcher_wkd);
			builder1.setTitle(getString(R.string.info));
			builder1.show();
			return true;
		}
		
		if(android.os.Build.VERSION.SDK_INT >=11) {
			if(item.getItemId() == android.R.id.home) {
				Intent intent = new Intent(this, RozkladWKD.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		}
		return false;
	}

	private class DownloadSchedules extends AsyncTask<Object, Object, Object> {
		private Exception e = null;

		@Override
		protected Object doInBackground(Object... params) {

			try {
				getDataFromServer();
			} catch (JSONException e) {
				if (RozkladWKD.DEBUG_LOG) {
					Log.w(TAG, e);
				}
				this.e = e;
			} catch (ParseException e) {
				if (RozkladWKD.DEBUG_LOG) {
					Log.w(TAG, e);
				}
				this.e = e;
			} catch (ClientProtocolException e) {
				error = true;
				if (RozkladWKD.DEBUG_LOG) {
					Log.w(TAG, e);
				}
				this.e = e;
			} catch (NotFoundException e) {
				error = true;
				if (RozkladWKD.DEBUG_LOG) {
					Log.w(TAG, e);
				}
				this.e = e;
			} catch (IOException e) {
				error = true;
				if (RozkladWKD.DEBUG_LOG) {
					Log.w(TAG, e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			setListAdapter();
			hideProgressDialog();
			if (e != null) {
				Error.handle(SearchResults.this, "WKD", e);
			}
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	private LinkedList<Schedule> getRozklad(Long fromStation, Long toStation,
			String time, Resources resources) throws JSONException,
			ParseException, ClientProtocolException, NotFoundException,
			IOException {

		final boolean isLocalSchedule = settings.getBoolean(
				RozkladWKD.LOCAL_SCHEDULE, false);
		final boolean isNewVersionAvailable = settings.getBoolean(
				RozkladWKD.NEW_VERSION_AVAILABLE, false);

		if (isLocalSchedule && !isNewVersionAvailable) {
			return getLocalRozklad(fromStation, toStation, time, resources);
		} else {

			return getOnlineRozklad(fromStation, toStation, time, resources);
		}

	}

	private LinkedList<Schedule> getOnlineRozklad(Long fromStation,
			Long toStation, String time, Resources resources)
			throws ClientProtocolException, JSONException, IOException,
			ParseException {
		LinkedList<Schedule> schedulesList = new LinkedList<Schedule>();
		SimpleDateFormat formatterDate = new SimpleDateFormat(
				"HH:mm:ss yyyy-MM-dd");

		Calendar cal = Calendar.getInstance();

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

		nameValuePairs.add(new BasicNameValuePair("requestType",
				"QUERY_SCHEDULE"));
		nameValuePairs.add(new BasicNameValuePair("fromStation", fromStation
				.toString()));
		nameValuePairs.add(new BasicNameValuePair("toStation", toStation
				.toString()));
		nameValuePairs.add(new BasicNameValuePair("time", time));
		if (RozkladWKD.DEBUG_LOG) {
			Log.i(TAG, "fromStation: " + fromStation.toString());
			Log.i(TAG, "toStation: " + toStation.toString());
			Log.i(TAG, "time: " + time);
		}

		// waw - grodzisk

		fromStation = fromStation + 1;
		toStation = toStation + 1;

		// Send the HttpPostRequest and receive a JSONObject in return
		JSONObject jsonObjRecv = pj.rozkladWKD.HttpClient
				.SendHttpPost(nameValuePairs);

		if (jsonObjRecv == null || jsonObjRecv.getString("result") == "ERROR") {
			return null;
		}

		JSONArray arr = jsonObjRecv.getJSONArray("db");
		JSONObject obj;

		Schedule schedule;

		for (int i = 0; i < arr.length(); i++) {
			obj = arr.getJSONObject(i);
			schedule = new Schedule();

			cal.setTime((Date) formatterDate.parse(obj.getString("from")));
			schedule.fromTime = (Calendar) cal.clone();

			cal.setTime((Date) formatterDate.parse(obj.getString("to")));
			schedule.toTime = (Calendar) cal.clone();

			schedule.type = obj.getString("type").trim();

			schedulesList.add(schedule);
		}

		return schedulesList;
	}

	private LinkedList<Schedule> getLocalRozklad(Long fromStation,
			Long toStation, String time, Resources resources) {
		LocalDB localDB = new LocalDB(this);

		fromStation += 1;
		toStation += 1;

		String direction;
		if (toStation > fromStation)
			direction = "GR_MIL";
		else
			direction = "WAW";

		String[] timeVal = time.split(":");

		final int timeMin = Integer.valueOf(timeVal[0]) * 60
				+ Integer.valueOf(timeVal[1]);

		final int fromMin = timeMin - 5;
		int toMin = timeMin + 60 * 5;

		boolean afterMidnight = false;

		if (toMin > 24 * 60) {
			afterMidnight = true;
			toMin = toMin - 24 * 60;
		}

		return localDB.getRozklad(afterMidnight, fromStation, toStation,
				fromMin, toMin, direction);

	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Schedule selectedSchedule = schedulesList.get(position);
		final boolean isLocalSchedule = settings.getBoolean(
				RozkladWKD.LOCAL_SCHEDULE, false);
		final boolean isNewVersionAvailable = settings.getBoolean(
				RozkladWKD.NEW_VERSION_AVAILABLE, false);

		if (isLocalSchedule && !isNewVersionAvailable) {
			ListView lv = new ListView(this);
			lv.setAdapter(new DetailsAdapter(this,
					selectedSchedule.stationsTimes));
			
			lv.setCacheColorHint(this.getResources().getColor(android.R.color.transparent));

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.connection_details);
			builder.setView(lv);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		} else {
			Toast.makeText(this, R.string.only_local_details, Toast.LENGTH_LONG)
					.show();
		}
	}



	private class DetailsAdapter extends BaseAdapter {
		ArrayList<Object[]> values;
		Context context;

		public DetailsAdapter(Context context, ArrayList<Object[]> stationsTimes) {
			this.values = stationsTimes;
			this.context = context;
		}

		@Override
		public int getCount() {
			return values.size();
		}

		@Override
		public Object getItem(int position) {
			return values.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						android.R.layout.simple_list_item_2, null);
			}
			Object[] obj = (Object[]) getItem(position);
			Calendar time = (Calendar) obj[1];
			String station = (String) obj[0];

			String h, m;

			if (time.get(Calendar.HOUR_OF_DAY) >= 0
					&& time.get(Calendar.HOUR_OF_DAY) <= 9) {
				h = "0" + time.get(Calendar.HOUR_OF_DAY);
			} else {
				h = String.valueOf(time.get(Calendar.HOUR_OF_DAY));
			}
			if (time.get(Calendar.MINUTE) >= 0
					&& time.get(Calendar.MINUTE) <= 9) {
				m = "0" + time.get(Calendar.MINUTE);
			} else {
				m = String.valueOf(time.get(Calendar.MINUTE));
			}

			TextView tv1 = (TextView) convertView.findViewById(android.R.id.text1);
			TextView tv2 = (TextView) convertView.findViewById(android.R.id.text2);
			tv1.setText(h + ":" + m);
			tv2.setText(station);
			
			return convertView;
		}
	}
}