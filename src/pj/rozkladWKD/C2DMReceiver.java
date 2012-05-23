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

public class C2DMReceiver extends C2DMBaseReceiver {
    private static int NOTIFICATION_ID = 1;

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
        context.sendBroadcast(new Intent("com.google.ctp.UPDATE_UI"));
    }

    @Override
    public void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String type = (String) extras.get("type");
            String msg = (String) extras.get("msg");

            if(RozkladWKD.DEBUG_LOG) {
                Log.d("PUSH", type + ": " + msg);
            }

            if (type.equals("MESSAGE")) {

                Intent notificationIntent = new Intent(this, MessageActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                showNotification(msg, getString(R.string.new_message),
                        msg, contentIntent);
            }
        }
    }

    public void showNotification(String ticker, String contentTitle, String contentText, PendingIntent intent) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        int icon = R.drawable.ic_launcher_wkd;
        CharSequence tickerText = ticker;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        Context context = getApplicationContext();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.setLatestEventInfo(context, contentTitle, contentText, intent);

        mNotificationManager.notify(NOTIFICATION_ID++, notification);
    }
}