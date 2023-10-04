package com.melissa.mikochat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ForegroundFCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification() != null){
            Intent intent = new Intent();
            intent.setAction("com.package.notification");
            sendBroadcast(intent);
        }
        else{
            System.out.println( "getNotification null");
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}