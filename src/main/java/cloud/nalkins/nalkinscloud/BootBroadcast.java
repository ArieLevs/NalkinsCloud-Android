package cloud.nalkins.nalkinscloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Arie on 8/8/2017.
 *
 * BOOT_COMPLETED BroadcastReceiver,
 * onReceive code starts once android complete boot
 */

public class BootBroadcast extends BroadcastReceiver {

    private static final String TAG = BootBroadcast.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "running 'onReceive' Function");
        MqttService.startMQTTService(context);
    }
}
