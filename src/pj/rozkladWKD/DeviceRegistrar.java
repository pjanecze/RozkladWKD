

/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pj.rozkladWKD;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.provider.Settings.Secure;
import org.json.JSONObject;

/**
 * Register/unregister with the Chrome to Phone App Engine server.
 */
public class DeviceRegistrar {
    public static final String STATUS_EXTRA = "Status";
    public static final int REGISTERED_STATUS = 1;
    public static final int AUTH_ERROR_STATUS = 2;
    public static final int UNREGISTERED_STATUS = 3;
    public static final int ERROR_STATUS = 4;

    private static final String TAG = "DeviceRegistrar";
    static final String SENDER_ID = "pjanecze@gmail.com";


    public static void registerWithServer(final Context context,
                                          final String deviceRegistrationID) {
        new Thread(new Runnable() {
            public void run() {
                Intent updateUIIntent = new Intent("com.google.ctp.UPDATE_UI");
                try {
                    List<NameValuePair> post = new ArrayList<NameValuePair>();
                    post.add(new BasicNameValuePair("requestType", "REGISTER_PUSH"));
                    post.add(new BasicNameValuePair("deviceId", DeviceIdGenerator.getDeviceId()));
                    post.add(new BasicNameValuePair("clientRegistrationId", deviceRegistrationID));
                    post.add(new BasicNameValuePair("productId", context.getString(R.string.productId)));

                    JSONObject result = HttpClient.SendHttpPost(post);

                    if(result== null || result.getString("result") == "ERROR") {
                        updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);

                    } else {
                        updateUIIntent.putExtra(STATUS_EXTRA, REGISTERED_STATUS);
                        SharedPreferences settings = Prefs.get(context);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Prefs.REGISTRATION_ID, deviceRegistrationID);
                        editor.commit();
                    }

                    context.sendBroadcast(updateUIIntent);

                } catch (Exception e) {
                    updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
                    context.sendBroadcast(updateUIIntent);
                }
            }
        }).start();
    }

    public static void unregisterWithServer(final Context context,
                                            final String deviceRegistrationID) {
        new Thread(new Runnable() {
            public void run() {
                Intent updateUIIntent = new Intent("com.google.ctp.UPDATE_UI");
                try {

                    List<NameValuePair> post = new ArrayList<NameValuePair>();
                    post.add(new BasicNameValuePair("requestType", "UNREGISTER_PUSH"));
                    post.add(new BasicNameValuePair("deviceId", DeviceIdGenerator.getDeviceId()));
                    post.add(new BasicNameValuePair("clientRegistrationId", deviceRegistrationID));
                    post.add(new BasicNameValuePair("productId", context.getString(R.string.productId)));

                    JSONObject result = HttpClient.SendHttpPost(post);
                } catch (Exception e) {
                    if(RozkladWKD.DEBUG_LOG) {
                        Log.e("PUSH", e.getMessage(), e);
                    }
                } finally {
                    SharedPreferences settings = Prefs.get(context);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove(Prefs.REGISTRATION_ID);
                    editor.commit();
                    updateUIIntent.putExtra(STATUS_EXTRA, UNREGISTERED_STATUS);
                }

                // Update dialog activity
                context.sendBroadcast(updateUIIntent);
            }
        }).start();
    }


}
