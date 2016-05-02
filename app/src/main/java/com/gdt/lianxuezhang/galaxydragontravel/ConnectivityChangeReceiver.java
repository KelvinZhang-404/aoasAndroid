package com.gdt.lianxuezhang.galaxydragontravel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

/**
 * Created by LianxueZhang on 30/12/2015.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "aoas";
    private NotificationHandler handler;

    public ConnectivityChangeReceiver(NotificationHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        debugIntent(intent, TAG, context);
    }

    private void debugIntent(Intent intent, String tag, Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key: extras.keySet()) {
                if (key.equals("networkType")) {
                    switch ((int)extras.get(key)) {
                        case ConnectivityManager.TYPE_MOBILE:
                            validStatus(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState(), cm);
                            break;
                        case ConnectivityManager.TYPE_WIFI:
                            validStatus(cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState(), cm);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void validStatus(NetworkInfo.State state, ConnectivityManager connectivityManager) {
        if (state == NetworkInfo.State.CONNECTED) {
            this.handler.handle("Connected");
        } else if (state == NetworkInfo.State.DISCONNECTED) {
            this.handler.handle("Disconnected");
        }
    }
}

interface NotificationHandler {
    void handle(String status);
}