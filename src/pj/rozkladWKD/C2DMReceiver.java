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


            if (type.equals("MESSAGE")) {

                //TODO dokonczyc

            }
        }
    }
}