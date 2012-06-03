package pj.rozkladWKD;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.viewpagerindicator.TitlePageIndicator;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pj.lib.errorhandler.Error;

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
import pj.rozkladWKD.pj.rozkladWKD.ui.MessagesFragment;
import pj.rozkladWKD.pj.rozkladWKD.ui.MessagesPagerAdapter;
import pj.rozkladWKD.pj.rozkladWKD.ui.SetUsernameFragment;

public class MessageActivity extends SherlockFragmentActivity implements View.OnClickListener, SetUsernameFragment.ISetUsernameListener,
        ViewPager.OnPageChangeListener, MessagesFragment.IMessagingListener{

	private static final String TAG = "MessageActivity";

	
	SharedPreferences settings;


    private ViewPager mViewPager;
	
	// variables for message dialog
	ImageButton sendButton;
	EditText message;
	

    MessagesFragment currentFragment;

    MessagesPagerAdapter mPagerAdapter;

    TitlePageIndicator mIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.rozklad_wkd_messages);
		
		setTitle(R.string.messages);

        setActionBar();



        message = (EditText) findViewById(R.id.message);
        sendButton = (ImageButton) findViewById(R.id.send);
        sendButton.setOnClickListener(this);


        removeNotifications();


		

		
		settings = PreferenceManager.getDefaultSharedPreferences(this);


        int page = 0;
        page = getIntent().getIntExtra("page", 0);


        mPagerAdapter = new MessagesPagerAdapter(this, getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mPagerAdapter);



        mIndicator = (TitlePageIndicator) findViewById(R.id.titles);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setOnPageChangeListener(this);
        mIndicator.setCurrentItem(page);

        currentFragment = (MessagesFragment) mPagerAdapter.getItem(page);
    }

    private void removeNotifications() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(C2DMReceiver.NOTIFICATION_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mUpdateUIReceiver, new IntentFilter("com.google.ctp.UPDATE_UI"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mUpdateUIReceiver);
    }



    private void setActionBar() {


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }



    @Override
    public void onClick(View view) {
        if(view == sendButton) {
            String messageText = message.getText().toString();
            if(TextUtils.isEmpty(messageText)) {
                return;
            }
            if(messageText.length() >512) {
                message.setError(getString(R.string.message_too_long));
                return;
            }

            String username = settings.getString(RozkladWKD.USERNAME, "");
            if(TextUtils.isEmpty(username)) {
                showSetUserNameDialog();
            } else {
                currentFragment.sendMessage(username, messageText);
            }
        }
    }

    private void showSetUserNameDialog() {
        SetUsernameFragment fragment = new SetUsernameFragment();
        fragment.show(getSupportFragmentManager(), "username_fragment");
    }



    @Override
    public void onSetUsernamePositive(String username) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(RozkladWKD.USERNAME, username);
        editor.commit();
        currentFragment.sendMessage(username, message.getText().toString());
    }

    @Override
    public void onSetUsernameNegative() {}








	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
//        if(item.getItemId() == R.id.menu_add_message) {
//            showAddMessageDialog();
//            return true;
//        } else
        if(item.getItemId() == R.id.menu_refresh_messages) {
            currentFragment.refreshMessages();

            return true;
        } else if(item.getItemId() == android.R.id.home) {
            //home buton
            Intent intent = new Intent(this, RozkladWKD.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        } else if(item.getItemId() == R.id.menu_settings) {
            final Intent intent = new Intent(MessageActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }


		return super.onOptionsItemSelected(item);
	}



    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {


        com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.messages, menu);

        return true;



    }


    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        currentFragment = (MessagesFragment) mPagerAdapter.getItem(i);
        currentFragment.resetMessages();
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void messageSent() {
        message.setText("");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("page", mViewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIndicator.invalidate();
            removeNotifications();
        }
    };
}
