package pj.rozkladWKD;

import android.app.Application;
import android.text.TextUtils;
import com.google.android.c2dm.C2DMessaging;

/**
 * Created with IntelliJ IDEA.
 * User: pawel
 * Date: 20.05.12
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */
public class RozkladWKDApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DeviceIdGenerator.init(this);
    }

    public void registerPushes() {
        if(Prefs.get(this).getBoolean(Prefs.PUSH_TURNED_ON, Prefs.DEFAULT_PUSH)) {
            String regId = C2DMessaging.getRegistrationId(this);
            if(TextUtils.isEmpty(Prefs.get(this).getString(Prefs.REGISTRATION_ID, ""))) {
                if (regId != null && !"".equals(regId)) {
                    DeviceRegistrar.registerWithServer(this, regId);
                } else {
                    C2DMessaging.register(this, DeviceRegistrar.SENDER_ID);
                }
            }
        } else {
            if(!TextUtils.isEmpty(Prefs.get(this).getString(Prefs.REGISTRATION_ID, null))) {
                C2DMessaging.unregister(this);
            }
        }
    }
}
