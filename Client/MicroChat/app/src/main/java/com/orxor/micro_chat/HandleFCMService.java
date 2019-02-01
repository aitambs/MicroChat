package com.orxor.micro_chat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class HandleFCMService extends FirebaseMessagingService {

    static final String TAG="FCM";
    public static final String CHANNEL_ID = "MicroChatNotifications";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (ChatActivity.isActive ==null || !ChatActivity.isActive){
            Intent intent = new Intent(this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Gson gson = new Gson();
            Message message = gson.fromJson(remoteMessage.getData().get("message"),Message.class);
            intent.putExtra("refresh", String.valueOf(message.getId()-1));

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Microchat")
                    .setContentText("You have new Message(s)")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MicroChat", importance);
                channel.setDescription("Microchat");
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, mBuilder.build());
        } else {
            Intent intent = new Intent("MicroChat_new_message");
            intent.putExtra("message", remoteMessage.getData().get("message"));
            sendBroadcast(intent);
        }
        Log.e(TAG,remoteMessage.getData().get("message"));
    }

    @Override
    public void onNewToken(String s) {
        Log.e(TAG,s);
    }


}
