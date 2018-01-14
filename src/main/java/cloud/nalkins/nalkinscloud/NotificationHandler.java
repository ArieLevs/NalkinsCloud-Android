package cloud.nalkins.nalkinscloud;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.StringTokenizer;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Arie on 8/11/2017.
 *
 */

class NotificationHandler {

    private static final String TAG = NotificationHandler.class.getSimpleName();

    /*private static NotificationHandler notificationHandlerInstance;

    public static NotificationHandler getNotificationHandlerInstance() {
        return notificationHandlerInstance;
    }*/

    /**
     * Brakes down the topic and compare the last section to detect if notification update is needed
     *
     * @param context the context to be passed to next function (no use in this function)
     * @param topic the topic that should get braked down
     * @param message the message body (payload)
     */
    static void identifyNotificationRequest(Context context, String topic, String message) {
        Log.d(TAG, "Running 'identifyNotificationRequest'");

        // Brake down the topic into tokens
        // Since the topic is in the form of "device_id/type/sensor" the delimiter is '/'
        StringTokenizer tokens = new StringTokenizer(topic, "/");
        String topic_device_id = tokens.nextToken(); //
        String topic_device_type = tokens.nextToken(); //
        String topic_general = tokens.nextToken(); //

        if(topic_general.equals("alarm") || topic_general.equals("automation_error")) {
            String title = topic_device_type + " : " + topic_general;
            addNotification(context, title, message);
            return;
        }
        Log.d(TAG, "No notification case detected using: " +
                topic_device_id + " - " +
                topic_device_type + " - " +
                topic_general);
    }

    /**
     * Display a simple notification
     *
     * @param context the context to apply to
     * @param title the title to be displayed on the notification
     * @param message the body of the notification
     */
    private static void addNotification(Context context, String title, String message) {
        Log.d(TAG, "Running 'addNotification'");
        Log.d(TAG, "title is:: " + title);
        Log.d(TAG, "message: " + message);

        String content = message + "\nPress to go back to application";

        //Define sound URI

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Log.d(TAG, "running 'addNotification' function");
        NotificationCompat.Builder builder = (android.support.v7.app.NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.power_main_64)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(soundUri);
        //.setSound(Uri.parse("android.resource://" +
        //        context.getApplicationContext().getPackageName() +
        //        "/" + R.raw.alarm_sound));


        // Set intent action to the activity we want to go back to when notification pressed
        Intent notificationIntent = new Intent(context, LogoActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}
