package pj.rozkladWKD;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		
		Preference dbVersion = getPreferenceScreen().findPreference("db_version");
		dbVersion.setTitle(getString(R.string.version) + ": " + getPreferenceScreen().getSharedPreferences().getInt(RozkladWKD.DB_VERSION, -1));



        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
		} else if(key.equals(Prefs.PUSH_TURNED_ON)) {
            RozkladWKDApplication app = (RozkladWKDApplication) getApplication();
            app.registerPushes();
        }
	}


    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        super.onOptionsItemSelected(item);


            if(item.getItemId() == android.R.id.home) {
                Intent intent = new Intent(this, RozkladWKD.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

        return false;
    }
	
}
