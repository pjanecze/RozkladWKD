package pj.rozkladWKD;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: pawel
 * Date: 20.05.12
 * Time: 16:04
 * To change this template use File | Settings | File Templates.
 */
public class DeviceIdGenerator {
    private static String deviceId;

    public static String getDeviceId() {
        return deviceId;
    }

    private static void generateDeviceId(Context context) {
        String generatedDeviceId = "";

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            generatedDeviceId += (String) get.invoke(c, "ro.serialno");


            generatedDeviceId += ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();

            deviceId = MD5(generatedDeviceId);

        } catch (Exception ignored) {
        }


    }

    private static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static void init(Context context) {
        generateDeviceId(context);
    }
}
