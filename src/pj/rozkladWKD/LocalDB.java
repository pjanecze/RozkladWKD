package pj.rozkladWKD;

import java.util.Calendar;

import java.util.LinkedList;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDB extends SQLiteOpenHelper {
	private static final String TAG = "LocalDB"; 

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "rozkladWKD.db";
    
    private static final String DICTIONARY_TABLE_NAME = "schedules";
    private static final String DICTIONARY_TABLE_CREATE =
    	"CREATE TABLE " + DICTIONARY_TABLE_NAME +  
    	" ( id numeric(3,0) NOT NULL,"+
    	  "st_1 INTEGER(5),"+
    	  "st_2 INTEGER(5),"+
    	  "st_3 INTEGER(5),"+
    	  "st_4 INTEGER(5),"+
    	  "st_5 INTEGER(5),"+
    	  "st_6 INTEGER(5),"+
    	  "st_7 INTEGER(5),"+
    	  "st_8 INTEGER(5),"+
    	  "st_9 INTEGER(5),"+
    	  "st_10 INTEGER(5),"+
    	  "st_11 INTEGER(5),"+
    	  "st_12 INTEGER(5),"+
    	  "st_13 INTEGER(5),"+
    	  "st_14 INTEGER(5),"+
    	  "st_15 INTEGER(5),"+
    	  "st_16 INTEGER(5),"+
    	  "st_17 INTEGER(5),"+
    	  "st_18 INTEGER(5),"+
    	  "st_19 INTEGER(5),"+
    	  "st_20 INTEGER(5),"+
    	  "st_21 INTEGER(5),"+
    	  "st_22 INTEGER(5),"+
    	  "st_23 INTEGER(5),"+
    	  "st_24 INTEGER(5),"+
    	  "st_25 INTEGER(5),"+
    	  "st_26 INTEGER(5),"+
    	  "st_27 INTEGER(5),"+
    	  "st_28 INTEGER(5),"+
    	  "st_29 INTEGER(5),"+
    	  "direction character varying(20),"+
    	  "type character(2));";

    LocalDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		db.execSQL("DROP TABLE " + DICTIONARY_TABLE_NAME);
		
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	public void addToSchedule(JSONObject obj) throws JSONException {
		ContentValues cv = new ContentValues();
		cv.put("st_1", getTimeIntegerValue(obj.getString("st_1")));
		cv.put("st_2", getTimeIntegerValue(obj.getString("st_2")));
		cv.put("st_3", getTimeIntegerValue(obj.getString("st_3")));
		cv.put("st_4", getTimeIntegerValue(obj.getString("st_4")));
		cv.put("st_5", getTimeIntegerValue(obj.getString("st_5")));
		cv.put("st_6", getTimeIntegerValue(obj.getString("st_6")));
		cv.put("st_7", getTimeIntegerValue(obj.getString("st_7")));
		cv.put("st_8", getTimeIntegerValue(obj.getString("st_8")));
		cv.put("st_9", getTimeIntegerValue(obj.getString("st_9")));
		cv.put("st_10", getTimeIntegerValue(obj.getString("st_10")));
		cv.put("st_11", getTimeIntegerValue(obj.getString("st_11")));
		cv.put("st_12", getTimeIntegerValue(obj.getString("st_12")));
		cv.put("st_13", getTimeIntegerValue(obj.getString("st_13")));
		cv.put("st_14", getTimeIntegerValue(obj.getString("st_14")));
		cv.put("st_15", getTimeIntegerValue(obj.getString("st_15")));
		cv.put("st_16", getTimeIntegerValue(obj.getString("st_16")));
		cv.put("st_17", getTimeIntegerValue(obj.getString("st_17")));
		cv.put("st_18", getTimeIntegerValue(obj.getString("st_18")));
		cv.put("st_19", getTimeIntegerValue(obj.getString("st_19")));
		cv.put("st_20", getTimeIntegerValue(obj.getString("st_2")));
		cv.put("st_21", getTimeIntegerValue(obj.getString("st_21")));
		cv.put("st_22", getTimeIntegerValue(obj.getString("st_22")));
		cv.put("st_23", getTimeIntegerValue(obj.getString("st_23")));
		cv.put("st_24", getTimeIntegerValue(obj.getString("st_24")));
		cv.put("st_25", getTimeIntegerValue(obj.getString("st_25")));
		cv.put("st_26", getTimeIntegerValue(obj.getString("st_26")));
		cv.put("st_27", getTimeIntegerValue(obj.getString("st_27")));
		cv.put("st_28", getTimeIntegerValue(obj.getString("st_28")));
		cv.put("st_29", getTimeIntegerValue(obj.getString("st_29")));
		cv.put("st_29", getTimeIntegerValue(obj.getString("st_29")));
		cv.put("direction", obj.getString("direction"));
		cv.put("type", obj.getString("type"));
		cv.put("id", obj.getString("id"));
		
		getWritableDatabase().insert(DICTIONARY_TABLE_NAME, null, cv);
		
			
	}

	private Integer getTimeIntegerValue(String string) {
		if(string == null || string.trim().length() == 0)
			return null;
		//Log.i(TAG, "string: " + string);
		final String[] values = string.split(":");
		if(values.length != 3)
			return null;
		//Log.i(TAG, "values: " + values);
		final int hours = Integer.valueOf(values[0]);
		final int minutes = Integer.valueOf(values[1]);
		
		return hours*60 + minutes;
		
	}

	public void removeSchedule() {
		getWritableDatabase().delete(DICTIONARY_TABLE_NAME, null, null);
		
	}

	public LinkedList<Schedule> getRozklad(boolean afterMidnight, Long fromStation,
			Long toStation, int fromMin, int toMin, String direction) {
		LinkedList<Schedule> schedulesList = new LinkedList<Schedule>();
		
		if(!afterMidnight) {
			
			Calendar cal = Calendar.getInstance();
			
			Cursor c = getReadableDatabase().query(DICTIONARY_TABLE_NAME, 
					new String[] {"ST_" + fromStation ,
									"ST_" + toStation ,
									"type"
							},
					"ST_" + fromStation + " is not null and ST_" + toStation + " is not null and " +
					"ST_" + fromStation + " > " + fromMin + " and ST_" + fromStation + " < " + toMin+ " and direction = ?", 
					new String[] { direction}, 
					null, 
					null, 
					"ST_" + fromStation);
			
			
			cursorToList(schedulesList, c, cal);
			
		} else {
			Calendar cal = Calendar.getInstance();
			
			Cursor c = getReadableDatabase().query(DICTIONARY_TABLE_NAME, 
					new String[] {"ST_" + fromStation,
									"ST_" + toStation,
									"type"
							},
					"ST_" + fromStation + " is not null and ST_" + toStation + " is not null and " +
					"ST_" + fromStation + " > " + fromMin + " and ST_" + fromStation + " <= " + (23 * 60 + 59)+ " and direction = ?", 
					new String[] { direction}, 
					null, 
					null, 
					"ST_" + fromStation);
			
			
			cursorToList(schedulesList, c, cal);
			
			cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis() + 1000*60*60*24);
			
			Cursor c1 = getReadableDatabase().query(DICTIONARY_TABLE_NAME, 
					new String[] {"ST_" + fromStation,
									"ST_" + toStation,
									"type"
							},
					"ST_" + fromStation + " is not null and ST_" + toStation + " is not null and " +
					"ST_" + fromStation + " >= 0 and ST_" + fromStation + " <= " + toMin+ " and direction = ?", 
					new String[] { direction}, 
					null, 
					null, 
					"ST_" + fromStation);
			
			
			
			
			
			cursorToList(schedulesList, c1, cal);
		}
		getReadableDatabase().close();
		return schedulesList;
	}

	private void cursorToList(LinkedList<Schedule> schedulesList, Cursor c, Calendar cal) {
		
		if(c != null ) {
			if(c.moveToFirst()) {

				Schedule schedule;
				do {
					schedule = new Schedule();

					final int fromTime = c.getInt(0);
					cal.set(Calendar.HOUR_OF_DAY, ((Double)Math.floor(fromTime / 60)).intValue());
					cal.set(Calendar.MINUTE, fromTime % 60);
					cal.set(Calendar.SECOND, 0);		
					schedule.fromTime = (Calendar) cal.clone();
					
					final int toTime = c.getInt(1);
					cal.set(Calendar.HOUR_OF_DAY, ((Double)Math.floor(toTime / 60)).intValue());
					cal.set(Calendar.MINUTE, toTime % 60);
					cal.set(Calendar.SECOND, 0);	
					schedule.toTime = (Calendar) cal.clone();
					
					schedule.type = c.getString(2).trim();
					
					schedulesList.add(schedule);
					
				} while(c.moveToNext());
			}
			c.close();
			c = null;
		}
		
		
	}
	
	//public void add
	
}
