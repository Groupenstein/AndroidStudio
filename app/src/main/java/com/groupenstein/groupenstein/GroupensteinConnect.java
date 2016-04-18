package com.groupenstein.groupenstein;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class GroupensteinConnect {
    // variable to hold context
    private Context context;
    public static String LoggedInUserTokenKey = "com.groupenstein.groupenstein.LoggedInUserToken";
    public static final String PROPERTY_REG_ID = "com.groupenstein.groupenstein.registration_id";
    public static final String PROPERTY_APP_VERSION = "com.groupenstein.groupenstein.appVersion";

//save the context recievied via constructor in a local variable

    public GroupensteinConnect(Context context){
        this.context=context;
    }

    public String MakeRequest(String uri, String json) {
        String responseString = "";
        try {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = new DefaultHttpClient().execute(httpPost);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            responseString = out.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseString;
    }

    public void SavePreference(String keyName, String keyValue)
    {
        SharedPreferences prefs = this.context.getSharedPreferences(
                "com.groupenstein.groupenstein", Context.MODE_PRIVATE);

        prefs.edit().putString(keyName, keyValue).commit();
    }

    public String GetPreference(String keyName)
    {
        SharedPreferences prefs = this.context.getSharedPreferences(
                "com.groupenstein.groupenstein", Context.MODE_PRIVATE);

        return prefs.getString(keyName,"");
    }

    public boolean IsLoggedInUser()
    {
        String userToken = GetPreference(GroupensteinConnect.LoggedInUserTokenKey);
        if (userToken.startsWith("0000") || userToken.equals(""))
        {
            return  false;
        }
        else
        {
            return true;
        }
    }
}


