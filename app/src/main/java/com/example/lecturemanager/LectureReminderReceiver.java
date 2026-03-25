package com.example.lecturemanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class LectureReminderReceiver extends BroadcastReceiver {

@Override
public void onReceive(Context context, Intent intent) {

    String lectureName = intent.getStringExtra("lectureName");

    NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    String channelId = "lecture_channel";

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Lecture Reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        manager.createNotificationChannel(channel);
    }

    Notification notification = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_add_alert_24)
            .setContentTitle("תזכורת להרצאה")
            .setContentText("יש לך הרצאה: " + lectureName)
            .setAutoCancel(true)
            .build();

    manager.notify((int) System.currentTimeMillis(), notification);
}
}
