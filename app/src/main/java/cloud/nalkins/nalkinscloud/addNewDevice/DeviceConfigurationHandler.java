package cloud.nalkins.nalkinscloud.addNewDevice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cloud.nalkins.nalkinscloud.AppConfig;
import cloud.nalkins.nalkinscloud.Functions;
import cloud.nalkins.nalkinscloud.HandleWifiConnection;
import cloud.nalkins.nalkinscloud.MainActivity;
import cloud.nalkins.nalkinscloud.MqttClient;
import cloud.nalkins.nalkinscloud.NetworkRequests;
import cloud.nalkins.nalkinscloud.R;
import cloud.nalkins.nalkinscloud.SharedPreferences;


/**
 * Created by Arie on 3/19/2017.
 *
 *
 */
public class DeviceConfigurationHandler extends AppCompatActivity {
    private static final String TAG = DeviceConfigurationHandler.class.getSimpleName();
    private ProgressDialog pDialog; // 'Processing' dialog
    private SharedPreferences session;

    private String devicePassword = "";

    Handler uiHandler;

    final int SHOW_CONFIGURE_DIALOG = 1;
    final int HIDE_DIALOG = 0;

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.back_only_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_configuration);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Set UI Handler to send actions to UI
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SHOW_CONFIGURE_DIALOG:
                        pDialog.setMessage("Configuring " + AppConfig.APP_NAME + " device ...");
                        Functions.showDialog(pDialog);
                        break;
                    case HIDE_DIALOG:
                        Functions.hideDialog(pDialog);
                        break;
                }
            }
        };

        // session manager
        session = new SharedPreferences(getApplicationContext());

        TextView deviceName = (TextView) findViewById(R.id.device_name_set);
        TextView wifiSSIDSelected = (TextView) findViewById(R.id.device_config_ssid_set);
        TextView wifiPassSelected = (TextView) findViewById(R.id.device_config_pass_set);

        deviceName.setText(DeviceSetNameActivity.getDeviceName());
        wifiSSIDSelected.setText(GetWifiCredentialsActivity.getSelectedSSID());
        wifiPassSelected.setText(GetWifiCredentialsActivity.getSelectedPassword());

        //Set next button
        Button confirmButton = (Button) findViewById(R.id.confirmButton);

        // Login button function
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If device has network connection
                if (HandleWifiConnection.isNetworkConnected(getApplicationContext()) != 0) {

                    // Get new password for the device from server, and sent server the device name to pair
                    getDevicePasswordFromServer(DeviceAddNewActivity.getDeviceId());
                }
                else {
                    Toast.makeText(getApplicationContext(), "No network connection detected", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /**
     * Send request to the Server, With client secret, device id, and the current access token
     * If all is verified by server logic, return a new password for the device to use
     */
    public void getDevicePasswordFromServer(String device_id) {
        Log.d(TAG, "Starting 'getDevicePasswordFromServer'");
        Log.d(TAG, "Processing device: " + device_id);
        Message showDialog =
                uiHandler.obtainMessage(SHOW_CONFIGURE_DIALOG, pDialog);
        showDialog.sendToTarget();

        String tag_device_pass_req = "req_device_pass";

        // Send these JSON parameters,
        Map<String, String> params = new HashMap<>();
        params.put("device_id", device_id); // Scanned QR code

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
            AppConfig.URL_GET_DEVICE_PASS, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Password Response: " + response.toString());
                Message hideDialog = uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                try {
                    // Check if the token contains any values
                    if (response.getString("status").equals("success")) {
                        Log.d(TAG, "New Device password confirmed");
                        devicePassword = response.getString("message");
                        // When password received move to device configuration
                        // Send response message to next function

                        testDeviceMqttConnection();
                    } else {
                        // Send hide dialog box to UI Thread
                        hideDialog.sendToTarget();
                        // Error in password request. Get the error message
                        String errorMsg = response.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // Send hide dialog box to UI Thread
                    hideDialog.sendToTarget();
                    // JSON error
                    Log.d(TAG, "Json error: " + e.toString());
                    Toast.makeText(getApplicationContext(), "GetDevicePass Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Functions.hideDialog(pDialog);
                // Send hide dialog box to UI Thread
                Message hideDialog = uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                hideDialog.sendToTarget();
                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Server Time out error or no connection");
                    Toast.makeText(getApplicationContext(),
                            "Timeout error! Server is not responding for password request",
                            Toast.LENGTH_LONG).show();
                } else {
                    String body;
                    //get status code here
                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                    Log.e(TAG, "Server response code: " + statusCode);
                    Toast.makeText(getApplicationContext(), statusCode, Toast.LENGTH_LONG).show();
                    //get response body and parse with appropriate encoding
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Device pass error: " + body);
                            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                String auth = "Bearer " + session.getToken();
                header.put("Authorization", auth);
                return header;
            }
        };
        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_device_pass_req, true);
    }

    private void testDeviceMqttConnection() {
        Thread thread = new Thread() {
            // Set counter timeout int
            int counter = 0;

            @Override
            public void run() {
                Message hideDialog = uiHandler.obtainMessage(HIDE_DIALOG, pDialog);

                MqttClient handleMQTT = new MqttClient(getApplicationContext(),
                        DeviceAddNewActivity.getDeviceId(), devicePassword);
                handleMQTT.connectToMQTTServer();
                try {

                    while (!handleMQTT.isClientConnected()) {
                        Log.d(TAG, "Counter is: " + counter);
                        counter++;
                        // If reached to counter limit (30)
                        Log.d(TAG, "Device simulation counter " + counter);
                        if (counter == 30) {
                            Log.d(TAG, "Device simulation Failed");

                            handleMQTT.disconnectMQTTClient();
                            // Send hide dialog box to UI Thread
                            hideDialog.sendToTarget();
                            return;
                        }

                        //Wait to connect
                        Thread.sleep(1000);
                    }
                    handleMQTT.disconnectMQTTClient();
                    Log.d(TAG, "Device simulation passed successfully");
                    connectToDevice(GetWifiCredentialsActivity.getSelectedSSID(),
                           GetWifiCredentialsActivity.getSelectedPassword(),
                            devicePassword);

                } catch (Exception e) {
                    // Send hide dialog box to UI Thread
                    hideDialog.sendToTarget();
                    Log.d(TAG, "connectToDevice Thread error: " + e.toString());
                }
            }
        };
        thread.start();
    }


    /**
     * Function will execute a network connection to the device
     *
     * All below parameters are moving to a next function called 'configureDevice'
     * @see DeviceConfigurationHandler#configureDevice(String, String, String)
     *
     * @param local_ssid local SSID the device should use for communication
     * @param local_wifi_pass local wireless LAN password the device should use to connect to SSID
     * @param device_pass the password the device should authenticate himself to mqtt server
     */
    public void connectToDevice(final String local_ssid, final String local_wifi_pass, final String device_pass) {
        Log.d(TAG, "Starting 'connectToDeviceAP'");

        HandleWifiConnection.setupWifiManager(getApplicationContext());

        // Start connection to device access point
        HandleWifiConnection.connectToWifiNetwork(getApplicationContext(),
                AppConfig.DEVICE_AP_SSID, AppConfig.DEVICE_AP_PASS);

        Thread thread = new Thread() {
            // Set counter timeout int
            int counter = 0;

            @Override
            public void run() {
                Message hideDialog = uiHandler.obtainMessage(HIDE_DIALOG, pDialog);

                try {
                    // check if connected to AppConfig.DEVICE_AP_SSID
                    Log.d(TAG, "Waiting for connection to device AP");
                    // Get connection to device status for first time
                    int tempInt = HandleWifiConnection.isConnectedToSSID(getApplicationContext()
                            , AppConfig.DEVICE_AP_SSID);
                    // While app is NOT connected to 'DEVICE_AP_SSID' do:
                    while (tempInt != 1) {
                        Log.d(TAG, "Counter is: " + counter);
                        counter++;
                        // If reached to counter limit (30), or got connected to some wifi that is not 'DEVICE_AP_SSID'
                        if ((counter == 30) || (tempInt == -1)) {
                            Log.d(TAG, "Failed to connect to: " + AppConfig.DEVICE_AP_SSID);
                            // Function will stop connection to device AP
                            Functions.stopWifiConnectionProcedure(getApplication());
                            // Send hide dialog box to UI Thread
                            hideDialog.sendToTarget();
                            return;
                        }

                        //Wait to connect
                        Thread.sleep(1000);
                        // Get new value to tempInt regarding the connection status
                        tempInt = HandleWifiConnection.isConnectedToSSID(getApplicationContext()
                                , AppConfig.DEVICE_AP_SSID);
                    }

                    // Start HTTP connection with the device and send relevant values
                    // Start device configuration
                    configureDevice(local_ssid, local_wifi_pass, device_pass);
                } catch (Exception e) {
                    // Send hide dialog box to UI Thread
                    hideDialog.sendToTarget();
                    Log.d(TAG, "connectToDevice Thread error: " + e.toString());
                }
            }
        };
        thread.start();
    }


    /**
     * Start HTTP session with the MQTT device and send it its password, wifiSSID and wifiPASS
     *
     * @param local_ssid local SSID the device should use for communication
     * @param local_wifi_pass local wireless LAN password the device should use to connect to SSID
     * @param device_pass the password the device should authenticate himself to mqtt server
     */
    public void configureDevice(String local_ssid, String local_wifi_pass, String device_pass) {
        Log.d(TAG, "Starting 'configureDevice' function");
        String tag_activation_req = "req_device_activation";

        // If user did not entered wifi password (not encrypted wifi) then stop
        if (local_wifi_pass.isEmpty()) {
            Log.e(TAG, "Wifi password is empty");
            return;
        }

        // Send these JSON parameters
        Map<String, String> params = new HashMap<>();
        params.put("ssid", local_ssid);
        params.put("wifi_pass", local_wifi_pass);
        params.put("mqtt_server", AppConfig.MQTT_SERVER_HOST);
        params.put("mqtt_port", AppConfig.MQTT_SERVER_PORT);
        //params.put("server_fingerprint", AppConfig.URL_MQTT_FINGERPRINT);
        //params.put("device_user", session.getUsername());
        params.put("device_pass", device_pass);

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.DEVICE_SETUP, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Message hideDialog = uiHandler.obtainMessage(HIDE_DIALOG, pDialog);

                // Function will stop connection to device AP
                Functions.stopWifiConnectionProcedure(getApplication());
                Log.d(TAG, "Device Response: " + response.toString());
                try {
                    String status = response.getString("status");
                    // Check if the token contains any values
                    if (status.equals("success")) {
                        Log.d(TAG, "Device wifi connection check passed");

                        while(!HandleWifiConnection.isOnline(getApplicationContext())) {
                            try {
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                        activateDeviceOnServer(DeviceAddNewActivity.getDeviceId(), DeviceSetNameActivity.getDeviceName());
                    } else {
                        // Send hide dialog box to UI Thread
                        hideDialog.sendToTarget();
                        // Error in configuration. Get the error message
                        Log.d(TAG, response.getString("message"));
                        Toast.makeText(getApplicationContext(), response.getString("message") + ", Probably wrong password", Toast.LENGTH_LONG).show();
                        // Take user back to Wifi configuration activity
                        Intent intent = new Intent(DeviceConfigurationHandler.this, GetWifiCredentialsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                } catch (JSONException e) {
                    // Send hide dialog box to UI Thread
                    hideDialog.sendToTarget();
                    // JSON error
                    Log.d(TAG, "Json error: " + e.toString());
                    Toast.makeText(getApplicationContext(), "Device Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message hideDialog = uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                // Send hide dialog box to UI Thread
                hideDialog.sendToTarget();

                // Function will stop connection to device AP
                Functions.stopWifiConnectionProcedure(getApplication());
                Log.e(TAG, "onErrorResponse is: " + error);
                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Device Time out error or no connection");
                    Toast.makeText(getApplicationContext(), "Timeout error! Device is not responding", Toast.LENGTH_LONG).show();
                } else {
                    String body;
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Activation Error: " + body);
                            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            }
        });
        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_activation_req, false);
    }


    /**
     * Send request to Server the client secret, device id, device name and the current access token
     * If all is verified by server logic, activate new device
     */
    public void activateDeviceOnServer(String device_id, String device_name) {
        Log.d(TAG, "Starting 'activateDeviceOnServer'");
        String tag_device_pass_req = "req_device_pass";

        // Send these JSON parameters,
        Map<String, String> params = new HashMap<>();
        params.put("device_id", device_id); // Scanned QR code
        params.put("device_name", device_name); // The device name, user choose

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_ACTIVATION, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Message hideDialog = uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                // Send hide dialog box to UI Thread
                hideDialog.sendToTarget();

                Log.d(TAG, "Server Response: " + response.toString());
                try {
                    // Check if the token contains any values
                    if (response.getString("status").equals("success")) {
                        Log.d(TAG, "Device Successfully activated");

                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "Activation successfully completed", Toast.LENGTH_LONG).show();
                        // Launch main activity
                        Intent intent = new Intent(DeviceConfigurationHandler.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Close all previous activities
                        startActivity(intent);
                        finish();

                    } else {
                        // Error in password request. Get the error message
                        String errorMsg = response.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    Log.d(TAG, "Json error: " + e.toString());
                    Toast.makeText(getApplicationContext(), "Activate Device  Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Message hideDialog = uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                // Send hide dialog box to UI Thread
                hideDialog.sendToTarget();

                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Server Time out error or no connection");
                    Toast.makeText(getApplicationContext(),
                            "Timeout error! Server is not responding for password request",
                            Toast.LENGTH_LONG).show();
                } else {
                    String body;
                    //get status code here
                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                    Log.e(TAG, "Server response code: " + statusCode);
                    Toast.makeText(getApplicationContext(), statusCode, Toast.LENGTH_LONG).show();
                    //get response body and parse with appropriate encoding
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Device activation error: " + body);
                            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                String auth = "Bearer " + session.getToken();
                header.put("Authorization", auth);
                return header;
            }
        };

        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_device_pass_req, true);
    }
}
