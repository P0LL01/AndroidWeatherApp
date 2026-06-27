package com.example.baraweatherapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * Βοηθητική κλάση για τη δημιουργία και εμφάνιση notifications.
 * Χρησιμοποιείται όταν εντοπίζονται επικίνδυνες καιρικές συνθήκες.
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "weather_alert_channel";
    private static final String CHANNEL_NAME = "Weather Alerts";

    /**
     * Δημιουργεί notification channel.
     * Είναι απαραίτητο για Android 8.0 και νεότερες εκδόσεις.
     */
    public static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notifications for dangerous weather conditions");

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Εμφανίζει notification στον χρήστη.
     *
     * @param context Context της εφαρμογής ή του Service.
     * @param title Τίτλος του notification.
     * @param message Περιεχόμενο του notification.
     */
    public static void showNotification(Context context, String title, String message) {

        createNotificationChannel(context);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }
}