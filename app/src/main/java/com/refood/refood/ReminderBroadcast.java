package com.refood.refood;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderBroadcast", "received!");
//        Toast.makeText(context, "Received ", Toast.LENGTH_LONG).show();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mNotifBuilder = new NotificationCompat.Builder(context, "alarmNotify")
                .setSmallIcon(R.drawable.zem1)
                .setContentTitle("Re:Food Re:minder")
                .setContentText("It's time to do the Re:Food Exercise, Re:member?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_launcher_foreground, "Yes!", pendingIntent);
        NotificationManagerCompat notificationMangager = NotificationManagerCompat.from(context);
        notificationMangager.notify(200, mNotifBuilder.build());
    }
}
