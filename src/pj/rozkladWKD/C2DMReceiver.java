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

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.util.Log;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.c2dm.C2DMBaseReceiver;
import pj.rozkladWKD.pj.rozkladWKD.ui.MessagesPagerAdapter;

public class C2DMReceiver extends C2DMBaseReceiver {
    public static int NOTIFICATION_ID = 100;

    public C2DMReceiver() {
        super(DeviceRegistrar.SENDER_ID);
    }

    @Override
    public void onRegistered(Context context, String registration) {

        DeviceRegistrar.registerWithServer(context, registration);
    }

    @Override
    public void onUnregistered(Context context) {
        SharedPreferences prefs = Prefs.get(context);
        String deviceRegistrationID = prefs.getString(Prefs.REGISTRATION_ID, null);
        DeviceRegistrar.unregisterWithServer(context, deviceRegistrationID);
    }

    @Override
    public void onError(Context context, String errorId) {

    }

    @Override
    public void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if(!Prefs.get(context).getBoolean(Prefs.PUSH_TURNED_ON, true)) {
            RozkladWKDApplication app = (RozkladWKDApplication) getApplication();
            SharedPreferences.Editor edit = Prefs.get(context).edit();
            edit.putBoolean(Prefs.PUSH_TURNED_ON, false);
            edit.commit();
            app.registerPushes();
        } else {
            if (extras != null) {
                String type = (String) extras.get("type");
                String msg = (String) extras.get("msg");
                String username = (String) extras.get("usr");


                if(RozkladWKD.DEBUG_LOG) {
                    Log.d("PUSH", type + ": " + msg);
                }


                String[] categories = Prefs.getStringArray(context, Prefs.PUSH_CATEGORIES, Prefs.DEFAULT_PUSH_CATEGORIES);

                Prefs.getNotificationMessageNextNumber(context, type);
                if(!username.equals(Prefs.get(context).getString(Prefs.USERNAME, "")) && categories != null
                        && type != null) {
                    for(String category : categories) {
                        if(type.equals(category)) {
                            Intent notificationIntent = new Intent(this, MessageActivity.class);
                            notificationIntent.putExtra("page", type.equals(MessagesPagerAdapter.MESSAGE_EVENTS) ? 0 :
                                                                                (type.equals(MessagesPagerAdapter.MESSAGE_WKD) ? 1:2));
                            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                            showNotification(msg, String.format(
                                    getString(R.string.new_message), getResources().getString(getResources().getIdentifier(type, "string", context.getPackageName()))),
                                    msg, contentIntent,
                                    Prefs.getNotificationMessageNextNumber(context));
                            break;
                        }
                    }
                }
                context.sendBroadcast(new Intent("com.google.ctp.UPDATE_UI"));

            }
        }
    }

    public void showNotification(String ticker, String contentTitle, String contentText, PendingIntent intent, int number) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        int icon = R.drawable.ic_launcher_wkd;
        CharSequence tickerText = ticker;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        Context context = getApplicationContext();
        notification.number = Prefs.getNotificationMessageNextNumber(context);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        SharedPreferences prefs = Prefs.get(context);
        if(prefs.getBoolean(Prefs.NOTIFICATION_SOUND, true))
            notification.defaults |= Notification.DEFAULT_SOUND;
        if(prefs.getBoolean(Prefs.NOTIFICATION_VIBRATION, true))
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        notification.setLatestEventInfo(context, contentTitle, contentText, intent);

        mNotificationManager.notify(NOTIFICATION_ID, notification);

    }

}