package pj.rozkladWKD;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		
		Preference dbVersion = getPreferenceScreen().findPreference("db_version");
		dbVersion.setTitle(getString(R.string.version) + ": " + getPreferenceScreen().getSharedPreferences().getInt(RozkladWKD.DB_VERSION, -1));
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}



	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}



	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(RozkladWKD.LOCAL_SCHEDULE)) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(RozkladWKD.LOCAL_SCHEDULE_CHANGED, true);
			editor.commit();
		}
	}

	
}
