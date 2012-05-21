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

public final class Prefs {
    public static final String REGISTRATION_ID = "deviceRegistrationID";
    public static final String TURN_ON_DIALOG_SHOWN = "turnOnDialogShown";
    public static final String PUSH_TURNED_ON = "pushturnedon";

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
