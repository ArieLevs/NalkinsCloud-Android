package cloud.nalkins.nalkinscloud;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by Arie on 8/7/2017.
 *
 */

public class MqttService extends Service {
    SharedPreferences sharedPreferences;

    private static final String TAG = MqttService.class.getSimpleName();

    final String MQTT_THREAD_NAME = "MqttService_thread"; // Handler Thread ID

    private static final String ACTION_START = "START"; // start MqttService
    private static final String ACTION_STOP = "STOP"; // stop MqttService
    private static final String ACTION_RECONNECT = "RECONNECT"; // reconnect MqttService

    private boolean isClientStarted = false; // Is the Client started?
    private Handler _connectionHandler; // Separate Handler thread for networking

    private static MqttClientClass mqttClient; // Mqtt Client

    public static MqttClientClass getStaticHandleMQTT() {
        return mqttClient;
    }


    /**
     * Start MQTT Client service
     *
     * @param context context to start the service with
     */
    public static void startMQTTService(Context context) {
        Intent intent = new Intent(context, MqttService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }


    /**
     * Stop MQTT Client service
     *
     * @param context context to start the service with
     */
    public static void stopMQTTService(Context context) {
        Intent intent = new Intent(context, MqttService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread(MQTT_THREAD_NAME);
        thread.start();
        // Create new session manager object
        sharedPreferences = new SharedPreferences(getApplicationContext());
        // This is the connection handler, connection will run in a new separate thread
        _connectionHandler = new Handler(thread.getLooper());
    }


    @Override
    public void onDestroy() {
        Log.i(TAG,"Running 'onDestroy' function");
        super.onDestroy();
        if(mqttClient.getMQTTclient() != null) {
            try {
                mqttClient.disconnectMQTTClient();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Service onStartCommand
     * Handles the action passed via the Intent
     *
     * @return START_REDELIVER_INTENT
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action = intent.getAction();

        if(action == null) {
            Log.d(TAG,"No action received at 'onStartCommand', doing nothing");
        } else {
            Log.d(TAG,"Function 'onStartCommand' received action " + action);
            switch (action) {
                case ACTION_START: {
                    start();
                }
                break;
                case ACTION_STOP: {
                    stop();
                }
                break;
                case ACTION_RECONNECT: {
                    // If Wifi or Mobile network connected
                    if(HandleWifiConnection.isNetworkConnected(getApplicationContext()) != 0) {
                        reconnectIfNecessary();
                    }
                }
                break;
            }
        }
        return START_REDELIVER_INTENT;
    }


    /**
     * Connect to Mqtt server
     * And listen for Connectivity changes
     * Using ConnectivityManager.CONNECTIVITY_ACTION BroadcastReceiver
     */
    private synchronized void start() {
        if(isClientStarted || mqttClient != null) {
            Log.d(TAG,"Attempt to start while service is running");
            if(mqttClient.isClientConnected())
                Log.d(TAG,"Attempt to connect to MQTT broker while already connected");
            else
                mqttClient.connectToMQTTServer();
            return;
        } else if (sharedPreferences.getUsername().equals("NULL")) {
            Log.d(TAG, "Stopping connection, user not logged in to account");
            return;
        }
        connect();
        registerReceiver(mConnectivityReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    /**
     * Stop the Mqtt client
     */
    private synchronized void stop() {
        if(!isClientStarted) {
            Log.d(TAG,"Attempting to stop connection that is not running");
            return;
        }
        if(mqttClient != null) {
            _connectionHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mqttClient.getMQTTclient().isConnected())
                            mqttClient.getMQTTclient().disconnect();
                    } catch(MqttException e) {
                        Log.e(TAG, e.toString());
                    }
                    mqttClient = null;
                    Log.d(TAG,"Setting isClientStarted (MQTT) to false");
                    isClientStarted = false;
                }
            });
        }
        unregisterReceiver(mConnectivityReceiver);
    }


    /**
     * Create new mqttClient object and call connectToMQTTServer (connecting to MQTT server)
     */
    private synchronized void connect() {
        Log.d(TAG, "Running 'connect' function. Current user name is:" + sharedPreferences.getUsername());
        mqttClient = new MqttClientClass(getApplicationContext(), sharedPreferences.getUsername(), sharedPreferences.getToken());

        _connectionHandler.post(new Runnable() {
            @Override
            public void run() {
                mqttClient.connectToMQTTServer();
                Log.d(TAG, "Setting isClientStarted (MQTT) to true");
                isClientStarted = true;
            }
        });
    }


    /**
     * Check current connectivity and reconnect
     */
    private synchronized void reconnectIfNecessary() {
        if(isClientStarted && mqttClient == null) {
            connect();
        }
    }


    /**
     * Listen for connectivity changes using ConnectivityManager
     */
    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Connectivity Changed");
        }
    };


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}