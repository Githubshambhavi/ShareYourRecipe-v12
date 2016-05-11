package com.example.allen.shareyourrecipe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class IncomingReceiver extends BroadcastReceiver {

    NotificationManager manager;
    Notification myNotification;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("Created")) {
            manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

            System.out.println("GOT THE INTENT");

            Intent notify = new Intent(context,MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notify, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            builder.setAutoCancel(true);
            builder.setContentTitle("Share Your Recipe");
            builder.setContentText("New Recipe Created");
            builder.setSmallIcon(R.drawable.notify);
            builder.setContentIntent(pendingIntent);
            builder.build();

            myNotification = builder.getNotification();
            manager.notify(11, myNotification);
        }
    }
}