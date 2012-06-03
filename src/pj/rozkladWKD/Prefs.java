package pj.rozkladWKD;

/**
 * Created with IntelliJ IDEA.
 * User: pawel
 * Date: 20.05.12
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.threefiftynice.android.preference.ListPreferenceMultiSelect;
import pj.rozkladWKD.pj.rozkladWKD.ui.MessagesPagerAdapter;

public final class Prefs {
    public static final String REGISTRATION_ID = "deviceRegistrationID";
    public static final String TURN_ON_DIALOG_SHOWN = "turnOnDialogShown";
    public static final String PUSH_TURNED_ON = "pushturnedon";
    public static final String USERNAME = "username";
    public static final String NOTIFICATION_SOUND = "notification_sound";
    public static final String NOTIFICATION_VIBRATION = "notification_vibration";
    public static final String PUSH_CATEGORIES = "push_categories";

    public static final String NOTIFICATION_MESSAGE_NUMBER = "nmnumber";

    public static final boolean DEFAULT_PUSH = true;

    public static final String[] DEFAULT_PUSH_CATEGORIES;
    static {
        DEFAULT_PUSH_CATEGORIES = new String[] {MessagesPagerAdapter.MESSAGE_APP, MessagesPagerAdapter.MESSAGE_EVENTS};
    }

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }



    public static int getNotificationMessageNextNumber(Context context) {
        SharedPreferences prefs = Prefs.get(context);
        int msgNumber = prefs.getInt(NOTIFICATION_MESSAGE_NUMBER + MessagesPagerAdapter.MESSAGE_EVENTS, 0);
        msgNumber += prefs.getInt(NOTIFICATION_MESSAGE_NUMBER + MessagesPagerAdapter.MESSAGE_WKD, 0);
        msgNumber += prefs.getInt(NOTIFICATION_MESSAGE_NUMBER + MessagesPagerAdapter.MESSAGE_APP, 0);

        return msgNumber;

    }

    public static int getNotificationMessageNextNumber(Context context, String type) {
        SharedPreferences prefs = Prefs.get(context);
        int msgNumber = prefs.getInt(NOTIFICATION_MESSAGE_NUMBER + type, 0);
        msgNumber++;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(NOTIFICATION_MESSAGE_NUMBER + type, msgNumber);
        editor.commit();

        return msgNumber;

    }

    public static void resetNotificationMessageNumber(Context context, String type) {
        SharedPreferences.Editor editor = get(context).edit();
        editor.putInt(NOTIFICATION_MESSAGE_NUMBER + type,0);
        editor.commit();
    }


    public static String[] getStringArray(Context context, String pushCategories, String[] defaultPushCategories) {
        String result = get(context).getString(PUSH_CATEGORIES, null);
        if(TextUtils.isEmpty(result)) {
            return defaultPushCategories;
        }
        return ListPreferenceMultiSelect.parseStoredValue(result, ";");

    }

    public static int getNotificationMessageNumber(Context context, String type) {
        return get(context).getInt(NOTIFICATION_MESSAGE_NUMBER + type, 0);
    }
}
