package cloud.nalkins.nalkinscloud;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * Created by Arie on 4/4/2017.
 *
 * This class holds an object that its basically a MQTT client,
 * all request to the MQTT broker will be done through this object
 */
public class MqttClient {
    private final String TAG = MqttClient.class.getSimpleName();

    private MqttAndroidClient _mqttAndroidClient;
    private MqttConnectOptions _mqttConnectOptions;
    private SharedPreferences _sharedPreferences;
    //private Context _context;

    // Object to store persistent data to external storage memory
    private MqttDefaultFilePersistence _dataStore; // Defaults to FileStore
    // Object to store persistent data to memory
    private MemoryPersistence _memStore; // On Fail use MemoryStore

    private final int UPDATE_DEVICE_UI = 3;

    /**
     * MqttClient Client Constructor,
     * This object will hold all information regarding the mqtt client,
     * In addition to the params, will hold info regarding
     * Server URI, SSL Certificate
     *
     * @param context The context to use
     * @param clientId The username to use to connect
     * @param password The password to use to connect
     */
    public MqttClient(final Context context, final String clientId, String password) {
        Log.d(TAG, "running 'MqttClient' constructor");
        //this._context = context;
        // Create new session manager object
        _sharedPreferences = new SharedPreferences(context);

        // First try accessing external storage to store persistent data to disk
        try {
            _dataStore = new MqttDefaultFilePersistence((context.getCacheDir().getAbsolutePath()));
        } catch(Exception e) {
            // In case opening the persistent file fails (permissions etc)
            // use memory
            e.printStackTrace();
            _dataStore = null;
            _memStore = new MemoryPersistence();
        }

        // Create a new MQTT android client
        // @param _context application context
        // @param AppConfig.URL_MQTT_SERVER_APP mqtt server URI
        // @param clientId the current user connected (email)
        // @param persistence is a MqttClientPersistence object
        if(_dataStore != null) {
            Log.d(TAG,"Connecting with DataStore");
            _mqttAndroidClient = new MqttAndroidClient(context, AppConfig.MQTT_SERVER_URI,
                    clientId, _dataStore);
        } else {
            Log.d(TAG,"Connecting with MemStore");
            _mqttAndroidClient = new MqttAndroidClient(context, AppConfig.MQTT_SERVER_URI,
                    clientId, _memStore);
        }

        // Set up a callback for the just created client
        _mqttAndroidClient.setCallback(new MqttCallbackExtended() {

            // Run once connection to MQTT server completed
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "Re-connected to: " + serverURI);
                    MainActivity a = new MainActivity();
                    a.subscribeAllDevices(true);
                } else {
                    Log.d(TAG, "Connected to: " + serverURI);
                }
            }

            // Do when connection lost
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "Connection to " +
                        AppConfig.MQTT_SERVER_URI +
                        " lost, setAutomaticReconnect function should be 'true'");
            }

            // Do when message arrived
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                Log.d(TAG, "Message Arrived: " + topic + " Message: " + payload);

                // Send message to UI at MainActivity
                Message msg = MainActivity.uiHandler.obtainMessage();
                msg.what = UPDATE_DEVICE_UI;

                msg.obj = "{\"topic\":\"" + topic + "\"," +
                           "\"message\": \"" + payload + "\"}";

                msg.obj = topic + "-" + payload;
                MainActivity.uiHandler.sendMessage(msg);

                NotificationHandler.identifyNotificationRequest(context, topic, new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        // Set up the connection options
        _mqttConnectOptions = new MqttConnectOptions();
        _mqttConnectOptions.setUserName(clientId);
        _mqttConnectOptions.setPassword(password.toCharArray());
        _mqttConnectOptions.setAutomaticReconnect(true);
        _mqttConnectOptions.setCleanSession(false);

        // Try opening the .bks file from 'assets' folder
        try {
            InputStream input = context.getAssets().open(AppConfig.MQTT_SERVER_BKS_FILE);
            // Set the .bks file to handle the SSL connection
            _mqttConnectOptions.setSocketFactory(_mqttAndroidClient.getSSLSocketFactory(input, AppConfig.MQTT_SERVER_BKS_PASSWORD));

        } catch (MqttException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to open file: " + AppConfig.MQTT_SERVER_BKS_FILE);
        }
    }


    /**
     * Connect the client to the MQTT server
     */
    public void connectToMQTTServer() {
        Log.d(TAG, "Running 'connectToMQTTServer' Function");

        if(this._mqttAndroidClient.isConnected()) { // First check if the client is already connected
            Log.d(TAG, "MQTT client is already connected");
            return;
        }
        try {
            // Start connection to MQTT server
            Log.d(TAG, "Trying to connect with user: " + this._mqttAndroidClient.getClientId());
            // Connect to server, using _mqttConnectOptions connect options
            // All connections information should already be stored in clients object variables
            final IMqttToken connectionToken = this._mqttAndroidClient.connect(this._mqttConnectOptions);

            //connectionToken.waitForCompletion(AppConfig.REQESTS_TIMEOUT);
            connectionToken.setActionCallback(new IMqttActionListener() {
                // Once connection was successful
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Set up disconnection options
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    _mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    Log.d(TAG, "Connection to " + AppConfig.MQTT_SERVER_URI + " successful");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Connection to server failed, trying to reconnect.");
                    Log.e(TAG, asyncActionToken.toString());
                    Log.e(TAG, exception.toString());

                    //NotificationHandler.addNotification(context, "MQTT SERVICE", "NOT CONNECTED");
                }
            });

        } catch (MqttException e) {
            Log.e(TAG, "Error while connecting to MQTT server: " + e.toString());
        }
    }


    /**
     * Disconnect the client from the MQTT server
     */
    public void disconnectMQTTClient() {
        Log.d(TAG, "Running 'disconnectMQTTClient' Function");

        if(this.isClientConnected()) { // First check if the client is connected
            try {
                Log.d(TAG, "Trying to disconnect user: " + this._mqttAndroidClient.getClientId());

                IMqttToken disconnectToken = this._mqttAndroidClient.disconnect();
                disconnectToken.waitForCompletion(AppConfig.REQUESTS_TIMEOUT);
                disconnectToken.setActionCallback(new IMqttActionListener() {

                    // Do when disconnect successful
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Disconnection from " + AppConfig.MQTT_SERVER_URI + " successful");
                    }

                    // Do when disconnect failed
                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        Log.d(TAG, "Disconnection from " + AppConfig.MQTT_SERVER_URI + "Interrupted" +
                                ", process (client) probably disconnected");
                        exception.printStackTrace();
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "Error while disconnecting from MQTT server: " + e.toString());
                e.printStackTrace();
            }
        }
        else {
            Log.d(TAG, "MQTT client was not connected");
        }
    }

    /**
     * Check if client is connected to broker
     *
     * @return true if connected, false otherwise
     */
    public boolean isClientConnected () {
        Log.d(TAG, "Running 'isClientConnected': " + this._mqttAndroidClient.isConnected());
        return  this._mqttAndroidClient.isConnected();
    }

    /**
     * Function will subscribe to an input topic
     *
     * @param topic the topic to which the client will subscribe to
     * @param qos the QOS level the topic should subscribe with
     */
    void subscribeToTopic(final String topic, int qos){
        Log.d(TAG, "running 'subscribeToTopic' function for topic: " + topic);
        try {
            // Set subscription token, with the subscription, set with the topic and qos
            IMqttToken subToken = this._mqttAndroidClient.subscribe(topic, qos);
            //subToken.waitForCompletion(AppConfig.REQESTS_TIMEOUT);
            subToken.setActionCallback(new IMqttActionListener() {

                // Do when subscription succeed
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed to: " + topic);
                }

                // Do when subscription failed
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed subscribed to: " + topic);
                    if(exception != null)
                        exception.printStackTrace();
                }
            });

        } catch (MqttException e){
            Log.e(TAG, "Error while subscribing: " + e.toString());
            e.printStackTrace();
        }
    }


    /**
     *  Un-subscribe from a given topic
     *
     * @param topic the topic the function will unsubscribe from
     */
    void unsubscribeFromTopic(final String topic){
        Log.d(TAG, "running 'unsubscribeFromTopic' function for topic: " + topic);
        try {
            // Set un-subscription token
            IMqttToken unsubscribeToken = this._mqttAndroidClient.unsubscribe(topic);

            unsubscribeToken.waitForCompletion(AppConfig.REQUESTS_TIMEOUT);
            unsubscribeToken.setActionCallback(new IMqttActionListener() {

                // Do when unsubscription succeed
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "unsubscribed from: " + topic);
                }

                // Do when unsubscription failed
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to unsubscribe from: " + topic);
                    exception.printStackTrace();
                }
            });

        } catch (MqttException e){
            Log.e(TAG, "Error while unsubscribing: " + e.toString());
            e.printStackTrace();
        }
    }


    /**
     * Function will publish a message to a specific topic
     *
     * @param topic the topic to which the client will publish to
     * @param payload the message body (the message itself)
     * @param isRetainedMessage determine if to publish a retained message or not
     */
    void publishMessage(final String topic, final String payload, boolean isRetainedMessage){
        Log.d(TAG, "Running 'publishMessage' function");
        Log.d(TAG, "Trying to publish, topic: " + topic + " - payload: " + payload);

        if(isClientConnected()) {
            try {
                byte[] encodedPayload = payload.getBytes("UTF-8");

                /*
                 * Parameters for 'publish' function:
                 * @topic - to deliver the message to, for example "testdeviceid/special/temperature".
                 * @payload - the byte array to use as the payload
                 * @qos - the Quality of Service to deliver the message at. Valid values are 0, 1 or 2.
                 * @retained - whether or not this message should be retained by the server.
                 * @userContext - optional object used to pass context to the callback. Use null if not required.
                 * @callback - optional listener that will be notified when message delivery hsa completed to the requested quality of service
                 */
                IMqttToken publishToken = this._mqttAndroidClient.publish(topic, encodedPayload, AppConfig.MESSAGE_QOS_1,
                        isRetainedMessage);

                //publishToken.waitForCompletion(AppConfig.REQESTS_TIMEOUT);
                publishToken.setActionCallback(new IMqttActionListener() {

                    // Do when publish succeed
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Publish successful");
                    }

                    // Do when publish failed
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Publish Failed");
                        Log.e(TAG, exception.toString());
                    }
                });


            } catch (UnsupportedEncodingException | MqttException e) {
                Log.e(TAG, "Error on publish attempt: " + e.toString());
            }
        } else {
            Log.e(TAG, "Error on publish attempt: MQTT not connected");
        }
    }

    MqttAndroidClient getMQTTclient() {
        return this._mqttAndroidClient;
    }
}
