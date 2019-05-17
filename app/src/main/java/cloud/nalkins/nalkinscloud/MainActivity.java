package cloud.nalkins.nalkinscloud;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import cloud.nalkins.nalkinscloud.addNewDevice.DeviceAddNewActivity;
import cloud.nalkins.nalkinscloud.deviceLayouts.DynamicLayoutDistillery;
import cloud.nalkins.nalkinscloud.deviceLayouts.DynamicLayoutDistilleryTemperatureSet;
import cloud.nalkins.nalkinscloud.deviceLayouts.DynamicLayoutMagnet;
import cloud.nalkins.nalkinscloud.deviceLayouts.DynamicLayoutSwitch;
import cloud.nalkins.nalkinscloud.deviceLayouts.DynamicLayoutTemperature;
import cloud.nalkins.nalkinscloud.login.LoginActivity;
import cloud.nalkins.nalkinscloud.userAccount.ResetPassActivity;

import static org.json.JSONObject.NULL;

/**
 * Created by Arie on 3/8/2017.
 *
 * @author Arie Levinson
 * @version 03.18.17
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog; // 'Processing' dialog

    public static Handler uiHandler;
    final int UPDATE_DEVICE_UI = 3;
    final int SHOW_GET_DEVICES_DIALOG = 2;
    final int SHOW_UPDATE_SERVER_DIALOG = 1;
    final int HIDE_DIALOG = 0;

    private SharedPreferences sharedPreferences;

    static final int GET_DYNAMIC_LAYOUT_SPECIAL_TEMP_CONF_REQUEST = 2;

    // Hold the last state of the special automation status
    //private boolean isManualConfShowing = false;

    // Each cell in the 'deviceList' holds (device_id, device_type, device_name)
    static ArrayList<HashMap<String, String>> devicesList = new ArrayList<>();
    // Helper flag is needed, to indicate server 'deviceList' response
    // In case 'no devices found' returned, we do not want to insert this message,
    // But still need an indication the the list is "NOT empty"
    boolean deviceListUpdatedByServer = false;
    static HashMap<String, Object> inflatedLayoutsIds = new HashMap<>();

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.action_add_device:
            // Launch the 'DeviceAddNewActivity' activity
            Intent intentA = new Intent(MainActivity.this,
                    DeviceAddNewActivity.class);
            startActivity(intentA);
            return(true);
        case R.id.action_view_scheduled_jobs:
            // Launch the 'ViewScheduledJobsActivity' activity
            Intent intentB = new Intent(MainActivity.this,
                    ViewScheduledJobsActivity.class);
            startActivity(intentB);
            return(true);
        case R.id.action_refresh:
            finish();
            startActivity(getIntent());
            return(true);
        case R.id.action_help:
            // Start the 'registerHelpFunction' and send the activity context
            Functions.helpFunction(getApplicationContext());
            return(true);
        case R.id.action_legal:
            // Start the 'legalFunction' and send the activity context
            Functions.legalFunction(getApplicationContext());
            return(true);
        case R.id.action_reset_pass:
            // Start the 'legalFunction' and send the activity context
            restartPassword();
            return(true);
        case R.id.action_logout:
            // Start the 'exitFunction' and send the activity context
            logoutUser();
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }


    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "Running 'onResume' function");
        // If no token present in shared preferences the logout the user
        if (sharedPreferences.getToken() == NULL) {
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    protected void onStart() {
        super.onStart();

        updateCustomerDeviceWithToken();
    }

    @Override
    protected void onStop() {
        super.onStop();

        uiHandler.removeCallbacks(null);
    }

    @Override
    protected void  onPause() {
        super.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Set UI Handler to send actions to UI
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case UPDATE_DEVICE_UI:
                        String currentString = inputMessage.obj.toString();

                        String topic = "";
                        String message ="";
                        try {
                            JSONObject incomingMessageJson = new JSONObject(currentString);
                            topic = incomingMessageJson.getString("topic");
                            message = incomingMessageJson.getString("message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //String[] separated = currentString.split("-");

                        //handleIncomingMessages(separated[0], separated[1]);
                        handleIncomingMessages(topic, message);
                        break;
                    case SHOW_UPDATE_SERVER_DIALOG:
                        pDialog.setMessage("Updating Server ...");
                        Functions.showDialog(pDialog);
                        break;
                    case SHOW_GET_DEVICES_DIALOG:
                        pDialog.setMessage("Retrieving " + getString(R.string.app_name) + " devices ...");
                        Functions.showDialog(pDialog);
                        break;
                    case HIDE_DIALOG:
                        Functions.hideDialog(pDialog);
                        break;
                }
            }
        };

        // Create new sharedPreferences manager object
        sharedPreferences = new SharedPreferences(getApplicationContext());

        // If no token present in shared preferences the logout the user
        if (sharedPreferences.getToken() == NULL) {
            finish();
        }
        // Print a welcome message
        Toast.makeText(getApplicationContext(), "Welcome " + sharedPreferences.getUsername(), Toast.LENGTH_LONG).show();

        devicesList.clear();

        getDeviceListFromServer();
        subscribeAllDevices(false);
    }

    /**
     * Store to 'devicesList' all devices found on server that are connected to the current token
     */
    public void getDeviceListFromServer() {
        Log.d(TAG, "Running 'getDeviceListFromServer' function");

        Message showDialog =
                uiHandler.obtainMessage(SHOW_GET_DEVICES_DIALOG, pDialog);
        final Message hideDialog =
                uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
        showDialog.sendToTarget();

        deviceListUpdatedByServer = false; // Set Flag to indicate if any value returned from server

        String tag_device_list_req = "req_device_list";

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_DEVICE_LIST, new JSONObject(), (JSONObject response) -> {
                Log.d(TAG, "Device List Response: " + response.toString());

                try {
                    // If status response equals success
                    if (response.getString("status").equals("success")) {

                        // Parse response to JSON Array
                        JSONArray jsonArray = response.getJSONArray("message");

                        // Loop through each object
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject device = jsonArray.getJSONObject(i);

                            HashMap <String,String> deviceMap =  new HashMap<>();

                            // Put into hash map relevant values
                            deviceMap.put("device_id", device.getString("device_id"));
                            deviceMap.put("device_type", device.getString("device_type"));
                            deviceMap.put("device_name", device.getString("device_name"));

                            // Add current hash map to devicesList
                            devicesList.add(deviceMap);
                        }
                        // Once successful, start 'setActiveDevices' function
                        setActiveDevices();
                    } else {
                        hideDialog.sendToTarget();
                        String responseMessage = response.getString("message");
                        // No devices returned
                        Toast.makeText(getApplicationContext(), responseMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    hideDialog.sendToTarget();
                    // JSON error
                    Log.d(TAG, "Json error: " + e.toString());
                    Toast.makeText(getApplicationContext(), "getDeviceListFromServer Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                deviceListUpdatedByServer = true;
        }, (VolleyError error) -> {
                try {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Log.e(TAG, "Server Time out error or no connection");
                        Toast.makeText(getApplicationContext(),
                                "Timeout error! Server is not responding for device list request",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String body;
                        try {
                            //get response body and parse with appropriate encoding
                            if (error.networkResponse.data != null) {
                                try {
                                    body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    Log.e(TAG, "Device list error: " + body);
                                    Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                deviceListUpdatedByServer = true;
                hideDialog.sendToTarget();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                String auth = "Bearer " + sharedPreferences.getToken();
                header.put("Authorization", auth);
                return header;
            }
        };
        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_device_list_req, true);
    }


    /**
     * Threaded function, wait until 'MqttClient' object in 'MqttService' Class is NOT null,
     * Wait until 'devicesList' is not empty,
     * Once conditions are met, loop through 'devicesList' and subscribe each device to MQTT server
     */
    public void subscribeAllDevices(final boolean isReconnectSubscription) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    // check if connected to AppConfig.DEVICE_AP_SSID
                    Log.d(TAG, "Waiting for MQTT object to create");
                    // While 'MqttClient' object in 'MqttService' Class is null or devicesList is empty, wait
                    while(MqttService.getStaticHandleMQTT() == null) {
                        Thread.sleep(2000);
                        Log.d(TAG, "Waiting for MQTT object to create");
                    }
                    while(!MqttService.getStaticHandleMQTT().isClientConnected()) {
                        Thread.sleep(2000);
                        Log.d(TAG, "Waiting for MQTT client to connect");
                    }

                    if(!isReconnectSubscription)
                        while(!deviceListUpdatedByServer) {
                            Thread.sleep(2000);
                            Log.d(TAG, "Waiting for device list");
                        }

                    // Once 'MqttClient' exists, and deviceList contains something
                    MqttClient handleMQTT = MqttService.getStaticHandleMQTT(); // Get mqtt client object

                    for(int i = 0; i < MainActivity.devicesList.size(); i++) {
                        final HashMap<String, String> deviceMap = MainActivity.devicesList.get(i);
                        final String topic = deviceMap.get("device_id") + "/" + deviceMap.get("device_type");

                        // Get the device type from current device, and subscribe to relevant topic
                        switch (deviceMap.get("device_type")) {
                            case "dht": {
                                handleMQTT.subscribeToTopic(topic + "/status", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/temperature", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/humidity", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.publishMessage(topic + "/update_now", "1", AppConfig.NOT_RETAINED_MESSAGE);
                            }
                            break;
                            case "switch": {
                                handleMQTT.subscribeToTopic(topic + "/status", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/from_device_current_status", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.publishMessage(topic + "/update_now", "1", AppConfig.NOT_RETAINED_MESSAGE);
                            }
                            break;
                            case "magnet": {
                                // Subscribe default 'status' topic that will represent LWT messages
                                handleMQTT.subscribeToTopic(topic + "/status", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/wifi_rssi", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/alarm", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/current_status", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/set_lock_status", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.publishMessage(topic + "/update_now", "1", AppConfig.NOT_RETAINED_MESSAGE);
                            }
                            break;
                            case "distillery": { // Special distillery layout
                                // Subscribe default 'status' topic that will represent LWT messages
                                handleMQTT.subscribeToTopic(topic + "/status", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/temperature", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.subscribeToTopic(topic + "/automation_error", AppConfig.MESSAGE_QOS_1);
                                handleMQTT.publishMessage(topic + "/update_now", "1", AppConfig.NOT_RETAINED_MESSAGE);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Subscribe to device topics Thread error: " + e.toString());
                }
            }
        };
        thread.start();
    }


    /**
     * Function will handle all incoming messages cases
     * Depending on the 'topic' the function will update relevant layout section
     *
     * @param topic the topic the message arrived with
     * @param message the message body itself
     */
    public void handleIncomingMessages(String topic, String message) {
        Log.d(TAG, "running 'handleIncomingMessages' function");
        // Brake down the topic into tokens
        // Since the topic is in the form of "device_id/type/sensor" the delimiter is '/'
        StringTokenizer tokens = new StringTokenizer(topic, "/");
        String topicDeviceId = tokens.nextToken(); //
        String topicDeviceType = tokens.nextToken(); //
        String topicGeneral = tokens.nextToken(); //

        Log.d(TAG, "topicDeviceId: " + topicDeviceId);
        Log.d(TAG, "topicDeviceType: " + topicDeviceType);
        Log.d(TAG, "topicGeneral: " + topicGeneral);
        Log.d(TAG, "message: " + message);

        // Run on each object in the device list that was built earlier,
        // And check which object needs to be updated
        for(int i = 0; devicesList.size() > i; i++) {

            // Since each cell from the ArrayList, hold hash map (as described above)
            // Get each array value (HashMap) from devicesList
            HashMap <String,String> deviceMap = devicesList.get(i);
            String currentDeviceID = deviceMap.get("device_id");
            String currentDeviceType = deviceMap.get("device_type");

            // If current device from 'deviceList' is the device that the topic contains then
            if(currentDeviceID.equals(topicDeviceId)) {
                // Trace relevant layout object by its deviceId
                for (HashMap.Entry<String, Object> entry : inflatedLayoutsIds.entrySet()) {
                    // If the current device id equals the id of the current object (layout) then
                    if (currentDeviceID.equals(entry.getKey())) {
                        Log.d(TAG, "Working on device_type: " + currentDeviceType);
                        switch (currentDeviceType) {
                            case "dht": {
                                // Retrieve relevant layout
                                DynamicLayoutTemperature tempLayout = (DynamicLayoutTemperature) entry.getValue();

                                switch (topicGeneral) {
                                    case "status": {
                                        tempLayout.setStatusValue(message);
                                        if (message.equals("online")) {
                                            tempLayout.setStatusTextColor(R.color.green);
                                        }
                                        else if (message.equals("offline")) {
                                            tempLayout.setStatusTextColor(R.color.red);
                                        }
                                    }
                                    break;
                                    case "temperature": {
                                        tempLayout.setDeviceTemperatureText(message + " C");
                                    }
                                    break;
                                    case "humidity": {
                                        tempLayout.setDeviceHumidityText(message + " %");
                                    }
                                    break;
                                }
                            }
                            break;

                            case "switch": {
                                // Set a temp Switch object
                                DynamicLayoutSwitch tempLayout = (DynamicLayoutSwitch) entry.getValue();
                                switch (topicGeneral) {
                                    case "status": {
                                        tempLayout.setStatusValue(message);
                                        if (message.equals("online")) {
                                            tempLayout.setStatusTextColor(R.color.green);
                                            tempLayout.setIsDeviceOnline(true);
                                        }
                                        else if (message.equals("offline")) {
                                            tempLayout.getSwitchToggleButton().setBackgroundResource(R.drawable.button_power_red_64);
                                            tempLayout.setStatusTextColor(R.color.red);
                                            tempLayout.setDeviceSwitchCurrentStatus("Unknown");
                                            tempLayout.setIsDeviceOnline(false);
                                        }
                                    }
                                    break;
                                    case "from_device_current_status": {
                                        tempLayout.setDeviceSwitchCurrentStatus(message);
                                        if (message.equals("1")) {
                                            tempLayout.getSwitchToggleButton().setBackgroundResource(R.drawable.button_power_green_64);
                                        }
                                        else if (message.equals("0")) {
                                            tempLayout.getSwitchToggleButton().setBackgroundResource(R.drawable.button_power_red_64);
                                        }
                                    }
                                    break;
                                }
                            }
                            break;

                            case "magnet": {
                                // Set a temp Magnet object
                                DynamicLayoutMagnet tempLayout = (DynamicLayoutMagnet) entry.getValue();

                                switch (topicGeneral) {
                                    case "status": {
                                        tempLayout.setDeviceStatus(message);
                                        if (message.equals("online")) {
                                            tempLayout.setStatusTextColor(R.color.green);
                                        }
                                        else if (message.equals("offline")) {
                                            tempLayout.setStatusTextColor(R.color.red);
                                            tempLayout.setIsOpenedLockedStateText("Not Available");
                                            tempLayout.setIsOpenedLockedStateTextColor(R.color.red);
                                        }
                                    }
                                    break;
                                    case "set_lock_status": {
                                        if (message.equals("1")) { // Is triggered
                                            tempLayout.setIsTriggeredReleasedStatusText("Triggered");
                                            tempLayout.setIsTriggeredReleasedStatusTextColor(R.color.green);
                                        }
                                        else if (message.equals("0")) { // Is released
                                            tempLayout.setIsTriggeredReleasedStatusText("Released");
                                            tempLayout.setIsTriggeredReleasedStatusTextColor(R.color.red);
                                        }
                                    }
                                    break;
                                    case "alarm": {
                                        if (message.equals("1")) {
                                            tempLayout.setDeviceIcon(R.drawable.warning_64);
                                            tempLayout.setIsTriggeredReleasedStatusText("Alarm");
                                            tempLayout.setIsTriggeredReleasedStatusTextColor(R.color.red);
                                            Log.d(TAG, "Alarm triggered for device: " + deviceMap.get("device_id"));
                                        }
                                    }
                                    break;
                                    case "current_status": {
                                        // If the device is in off line state, do not perform incoming retained message
                                        if(tempLayout.isDeviceOnline()) {
                                            if (message.equals("1")) {

                                                tempLayout.setIsOpenedLockedStateText("Closed");
                                                tempLayout.setIsOpenedLockedStateTextColor(R.color.green);
                                            } else { // Else if equals "0"
                                                tempLayout.setIsOpenedLockedStateText("Opened");
                                                tempLayout.setIsOpenedLockedStateTextColor(R.color.red);
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            break;

                            case "distillery": {
                                // Set a temp Special object
                                DynamicLayoutDistillery temp = (DynamicLayoutDistillery) entry.getValue();

                                switch (topicGeneral) {
                                    case "status": {
                                        temp.setStatusValue(message);
                                        if (message.equals("online")) {
                                            temp.setStatusTextColor(R.color.green);
                                        }
                                        else if (message.equals("offline")) {
                                            temp.setStatusTextColor(R.color.red);
                                        }
                                    }
                                    break;
                                    case "temperature": {
                                        temp.setDeviceTemperatureText(message + " C, from: " +
                                                DateFormat.getDateTimeInstance().format(new Date()));
                                    }
                                    break;
                                    case "automation_error": {
                                        if(message.equals("1")) {
                                            temp.setTemperatureIcon(R.drawable.warning_64);
                                            sharedPreferences.setAutomationError(true);
                                        }
                                    }
                                    break;
                                    case "automation": {
                                        StringTokenizer automationToken = new StringTokenizer(message, ",");
                                        String value = automationToken.nextToken();

                                        if(value.equals("1"))
                                            temp.getDistilleryToggleButton().setChecked(true);
                                        if(value.equals("0"))
                                            temp.getDistilleryToggleButton().setChecked(false);
                                    }
                                    break;
                                    case "update_temp_settings": {
                                        if(message.equals("1"))
                                            // Update the icon to "V" to indicate
                                            temp.setTemperatureConfStatusIcon(R.drawable.v_24);
                                        if(message.equals("0"))
                                            // Update the icon to "X" to indicate
                                            temp.setTemperatureConfStatusIcon(R.drawable.x_24);
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }


    /**
     *  Logout the user, Remove token from shared preferences, stop MQTT service, finish
     * */
    private void logoutUser() {
        Log.d(TAG, "running 'logoutUser' function");

        new AlertDialog.Builder(this)
                .setTitle("Logout from " + getString(R.string.app_name))
                .setMessage("Warning, Android will stop receiving messages from NalkinsCloud. " +
                        "Do you really want to logout?")
                .setIcon(R.drawable.warning_64)
                .setPositiveButton(android.R.string.yes, (DialogInterface dialog, int whichButton) -> {
                        sharedPreferences.removeUsername(); // Remove username from shared preferences
                        sharedPreferences.removeToken();
                        // Stop MQTT service
                        MqttService.stopMQTTService(getApplicationContext());

                        Functions.revokeToken(getApplication());

                        // Launching the login activity
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void restartPassword() {
        Intent intent = new Intent(MainActivity.this, ResetPassActivity.class);
        startActivity(intent);
    }

    private void removeDeviceAlert(final String deviceId) {
        Log.d(TAG, "running 'removeDevice' function");

        new AlertDialog.Builder(this)
                .setTitle("Remove device from " + getString(R.string.app_name))
                .setMessage("Warning, device will be removed from your account, " +
                        "Do you really want to remove this device?")
                .setIcon(R.drawable.warning_64)
                .setPositiveButton(android.R.string.yes, (DialogInterface dialog, int whichButton) -> {
                        // Execute remove device function
                        removeDeviceFromAccount(deviceId);
                    })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void removeDeviceFromAccount(String deviceId) {
        Log.d(TAG, "Running 'removeDeviceFromAccount' function");
        String tag_password_reset = "req_remove_device";

        pDialog.setMessage("Removing device ...");
        Functions.showDialog(pDialog);

        // Send these JSON parameters,
        Map<String, String> params = new HashMap<>();
        params.put("device_id", deviceId); // Access token

        JSONObject jsonObject = new JSONObject(params);
        Log.d(TAG, jsonObject.toString());
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_REMOVE_DEVICE, new JSONObject(params), (JSONObject response) -> {

                Log.d(TAG, "Remove Device response: " + response.toString());
                Functions.hideDialog(pDialog);

                try {
                    String status = response.getString("status");
                    // Check if status is success
                    if (status.equals("success")) {
                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();

                        // Back to previous activity
                        //finish();
                        finish();
                        startActivity(getIntent());

                    } else {
                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    Log.d(TAG, "Json error: " + e.toString());
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

        }, (VolleyError error) -> {
                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Server Time out error or no connection");
                } else {
                    String body;
                    //get response body and parse with appropriate encoding
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Log.e(TAG, "Error: " + body);
                            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                // If connection error occurred during forgot password return with error
                Toast.makeText(getApplicationContext(), "Connection error occurred" +
                        ", Could not execute task", Toast.LENGTH_LONG).show();
                Functions.hideDialog(pDialog);
                finish();
                startActivity(getIntent());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                String auth = "Bearer " + sharedPreferences.getToken();
                header.put("Authorization", auth);
                return header;
            }
        };
        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_password_reset, true);
    }


    /**
     *  Override the back button option
     *  Will force the user to press twice the back button in order to exit app
     */
    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) { // If exit flag is TRUE then
            finish(); // finish activity
        } else { // If exit flag is currently FALSE then
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;

            // If 3 seconds pass then force user to start process again
            new Handler().postDelayed(() -> exit = false, 3 * 1000);
        }
    }


    /**
     * Initiate main layout with all devices user have
     * It will get a list of devices,
     * Depending on each device will set relevant layout template
     */
    public void setActiveDevices() {
        Log.d(TAG, "running 'setActiveDevices' Function");

        // Set parent layout (this is the main layout)
        LinearLayout mainLayout = findViewById(R.id.dynamicDeviceLayout);

        for(int i = 0; i < devicesList.size(); i++) {
            // Get each array value (HashMap) from devicesList
            final HashMap<String, String> deviceMap = devicesList.get(i);
            Log.d(TAG, "Working on device_id: " + deviceMap.get("device_id"));

            // Get the device type from current device, and set parameters accordingly
            switch (deviceMap.get("device_type")) {
                case "dht": {
                    setupDynamicLayoutTemperature(mainLayout, deviceMap);
                }
                break;
                case "switch": {
                    setupDynamicLayoutSwitch(mainLayout, deviceMap);
                }
                break;
                case "magnet": {
                    setupDynamicLayoutMagnet(mainLayout, deviceMap);
                }
                break;

                default: { // Special distillery layout
                    setupDynamicLayoutDistillery(mainLayout, deviceMap);
                }
                break;
            }
        }
        Functions.hideDialog(pDialog); // Stop dialog
    }


    void setupDynamicLayoutTemperature(LinearLayout mainLayout, final HashMap<String, String> deviceMap) {
        Log.d(TAG, "Running 'setupDynamicLayoutTemperature' function");

        // Create new temperature device layout object
        final DynamicLayoutTemperature tempLayout = new DynamicLayoutTemperature(getApplication(), deviceMap.get("device_id")
                , deviceMap.get("device_type"), deviceMap.get("device_name"));


        // Show additional device settings options
        tempLayout.getOptionsIcon().setOnClickListener((View v) -> {
            if (tempLayout.getDeviceOptionsLayout().getVisibility() == View.VISIBLE) { // If the manual conf layout made visible
                tempLayout.getDeviceOptionsLayout().setVisibility(View.GONE);// Show the layout
            } else { // If the manual conf layout made invisible
                tempLayout.getDeviceOptionsLayout().setVisibility(View.VISIBLE);// Show the layout
            }
        });

        // Remove device from application
        tempLayout.getRemoveIcon().setOnClickListener((View v) -> removeDeviceAlert(deviceMap.get("device_id")) );

        tempLayout.getDeviceIcon().setOnClickListener((View v) -> {
              if (tempLayout.isDeviceOnline()) {
                  MqttService.getStaticHandleMQTT().publishMessage(deviceMap.get("device_id") +
                          "/" +
                          deviceMap.get("device_type") +
                          "/update_now", "1", AppConfig.NOT_RETAINED_MESSAGE);
                  Toast.makeText(getApplicationContext(),
                          "Updating " + deviceMap.get("device_id") + " status",
                          Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(getApplicationContext(),
                        "Device cannot be used while in 'offline' state",
                        Toast.LENGTH_SHORT).show();
            }
          });

        mainLayout.addView(tempLayout.getView());
        // And store the device name - to the devices layout object
        inflatedLayoutsIds.put(deviceMap.get("device_id"), tempLayout);
    }

    void setupDynamicLayoutSwitch(LinearLayout mainLayout, final HashMap<String, String> deviceMap) {
        final DynamicLayoutSwitch tempLayout = new DynamicLayoutSwitch(getApplicationContext(), deviceMap.get("device_id")
                , deviceMap.get("device_type"), deviceMap.get("device_name"));

        final String topic = deviceMap.get("device_id") + "/" + deviceMap.get("device_type");

        tempLayout.getOptionsIcon().setOnClickListener((View v) -> {
            if (tempLayout.getDeviceOptionsLayout().getVisibility() == View.VISIBLE){ // If the manual conf layout made visible
                tempLayout.getDeviceOptionsLayout().setVisibility(View.GONE);// Show the layout
            } else { // If the manual conf layout made invisible
                tempLayout.getDeviceOptionsLayout().setVisibility(View.VISIBLE);// Show the layout
            }
        });

        tempLayout.getRemoveIcon().setOnClickListener((View v) -> removeDeviceAlert(deviceMap.get("device_id")));

        tempLayout.getSwitchToggleButton().setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {

                // If device id in offline state, no action should be available
                if (tempLayout.getIsDeviceOnline()) {
                    if (isChecked) {
                        // The toggle is enabled
                        Toast.makeText(getApplicationContext(), "Switch: " + deviceMap.get("device_name")+ ", Turned ON", Toast.LENGTH_LONG).show();
                        // Publish message with relevant topic
                        MqttService.getStaticHandleMQTT().publishMessage(topic + "/change_switch", "1", AppConfig.RETAINED_MESSAGE);
                    } else {
                        // The toggle is disabled
                        Toast.makeText(getApplicationContext(), "Switch: " + deviceMap.get("device_name") + ", Turned OFF", Toast.LENGTH_LONG).show();
                        // Publish message with relevant topic
                        MqttService.getStaticHandleMQTT().publishMessage(topic + "/change_switch", "0", AppConfig.RETAINED_MESSAGE);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Device cannot be used while in 'offline' state",
                            Toast.LENGTH_SHORT).show();
                    // Since the above was not executed (device is offline),
                    // The toggle got checked, so it needs to be unchecked
                    if (isChecked)
                        tempLayout.setSwitchToggleButton(false);
                }

        });

        mainLayout.addView(tempLayout.getView());
        // And store the device name - to the devices layout object
        inflatedLayoutsIds.put(deviceMap.get("device_id"), tempLayout);
    }

    void setupDynamicLayoutMagnet(LinearLayout mainLayout, final HashMap<String, String> deviceMap) {
        Log.d(TAG, "Running 'setupDynamicLayoutMagnet'");

        // Create new 'magnet' device object
        final DynamicLayoutMagnet tempLayout = new DynamicLayoutMagnet(getApplicationContext(), deviceMap.get("device_id")
                , deviceMap.get("device_type"), deviceMap.get("device_name"));

        final String topic = deviceMap.get("device_id") + "/" + deviceMap.get("device_type");

        // Set 'magnet' layout (the object) icon, and set it as clickable
        tempLayout.getDeviceIcon().setOnClickListener((View v) -> {
            // If device id in offline state, no action should be available
            if(tempLayout.isDeviceOnline()) {
                // If current 'DeviceStatus' text is set to 'alarm' then
                if (tempLayout.getIsTriggeredReleasedStatusText().getText().equals("Alarm")) {
                    // Set the 'tmpTopic' to
                    String tmpTopic = topic + "/release_alarm";
                    // And publish the message (to release the alarm)
                    MqttService.getStaticHandleMQTT().publishMessage(tmpTopic, "1", AppConfig.NOT_RETAINED_MESSAGE);
                    // Then switch the icon back to "normal" magnet icon
                    tempLayout.setDeviceIcon(R.drawable.magnet_main_64);

                    // Then once the alarm was stopped, the magnet status needs to change
                    // Set the 'tmpTopic' to
                    tmpTopic = topic + "/set_lock_status";
                    // Set 'DeviceStatus' text to 'released'
                    tempLayout.setIsTriggeredReleasedStatusText("Released");
                    tempLayout.setIsTriggeredReleasedStatusTextColor(R.color.red); // Set text color
                    // And publish to device that its on 'released' state
                    MqttService.getStaticHandleMQTT().publishMessage(tmpTopic, "0", AppConfig.NOT_RETAINED_MESSAGE);

                    tmpTopic = topic + "/alarm";
                    MqttService.getStaticHandleMQTT().publishMessage(tmpTopic, "0", AppConfig.RETAINED_MESSAGE);
                    // Mark that we sent 'released'
                    sharedPreferences.setIsReleasedTriggered("Released");
                } else { // If current 'DeviceStatus' text is NOT set to 'alarm' then
                    // Set the 'tmpTopic' to
                    final String tmpTopic = topic + "/set_lock_status";
                    // If last message sent to device was 'release',
                    // then now (on button click) it should send 'triggered'
                    if (sharedPreferences.getIsReleasedTriggered().equals("Released")) {
                        // Set 'DeviceStatus' text to 'triggered'
                        tempLayout.setIsTriggeredReleasedStatusText("Triggered");
                        tempLayout.setIsTriggeredReleasedStatusTextColor(R.color.green); // Set text color
                        // And publish to device that its on 'triggered' state
                        MqttService.getStaticHandleMQTT().publishMessage(tmpTopic, "1", AppConfig.NOT_RETAINED_MESSAGE);

                        // Once published, save the published state to shared preferences
                        // Mark that we sent 'Triggered'
                        sharedPreferences.setIsReleasedTriggered("Triggered");
                    } else {
                        // Set 'DeviceStatus' text to 'released'
                        tempLayout.setIsTriggeredReleasedStatusText("Released");
                        tempLayout.setIsTriggeredReleasedStatusTextColor(R.color.red); // Set text color
                        // And publish to device that its on 'triggered' state

                        MqttService.getStaticHandleMQTT().publishMessage(tmpTopic, "0", AppConfig.NOT_RETAINED_MESSAGE);

                        // Once published, save the published state to shared preferences
                        // Mark that we sent 'released'
                        sharedPreferences.setIsReleasedTriggered("Released");
                    }
                }
            }
            else
                Toast.makeText(getApplicationContext(),
                        "Device cannot be used while in 'offline' state",
                        Toast.LENGTH_SHORT).show();
        });

        // Add the configured above layout (magnet layout object) to main layout
        mainLayout.addView(tempLayout.getView());
        // And store the device name - to the devices layout object
        inflatedLayoutsIds.put(deviceMap.get("device_id"), tempLayout);
    }

    void setupDynamicLayoutDistillery(LinearLayout mainLayout, final HashMap<String, String> deviceMap) {
        final DynamicLayoutDistillery tempLayout = new DynamicLayoutDistillery(getApplicationContext(), deviceMap.get("device_id")
                , deviceMap.get("device_type"), deviceMap.get("device_name"));

        final String topic = deviceMap.get("device_id") + "/" + deviceMap.get("device_type");

        // Set the 'Start scheduler button' function
        tempLayout.startScheduler.setOnClickListener((View v) -> {
            Intent intent = new Intent(MainActivity.this, DateTimePickerActivity.class);
            // Add the device id and pass it to the scheduler activity
            intent.putExtra("DEVICE_ID", deviceMap.get("device_id"));
            intent.putExtra("TOPIC", topic + "/automation");
            startActivity(intent);
        });

        // Set the 'Start Temperature config button' function
        // On icon click new activity will start
        tempLayout.startTemperatureConf.setOnClickListener((View v) -> {
            Intent intent = new Intent(MainActivity.this, DynamicLayoutDistilleryTemperatureSet.class);
            // Add current 'device id' values, since if returned, main activity
            // Will need to identify which layout to update
            intent.putExtra("DEVICE_ID", deviceMap.get("device_id"));
            // startActivityForResult is executed, since this activity will return
            // A flag whether the user selected manual configurations
            startActivityForResult(intent, GET_DYNAMIC_LAYOUT_SPECIAL_TEMP_CONF_REQUEST, null);
        });

        if(sharedPreferences.getIsCustomTempConfigured())
            tempLayout.setTemperatureConfStatusIcon(R.drawable.v_24);
        else
            tempLayout.setTemperatureConfStatusIcon(R.drawable.x_24);

        tempLayout.tempIcon.setOnClickListener((View v) -> {
            if(sharedPreferences.getAutomationError()) {
                tempLayout.setTemperatureIcon(R.drawable.temperature_icon_32);
                sharedPreferences.setAutomationError(false);
                tempLayout.getDistilleryToggleButton().setChecked(false);
                MqttService.getStaticHandleMQTT().publishMessage(topic + "/automation_error", "0", AppConfig.RETAINED_MESSAGE);
            }
        });

        // Show additional device settings options
        tempLayout.getOptionsIcon().setOnClickListener((View v) -> {
            if (tempLayout.getDeviceOptionsLayout().getVisibility() == View.VISIBLE){ // If the manual conf layout made visible
                tempLayout.getDeviceOptionsLayout().setVisibility(View.GONE);// Show the layout
            } else { // If the manual conf layout made invisible
                tempLayout.getDeviceOptionsLayout().setVisibility(View.VISIBLE);// Show the layout
            }
        });

        // Remove device from application
        tempLayout.getRemoveIcon().setOnClickListener((View v) -> removeDeviceAlert(deviceMap.get("device_id")));

        // Set the 'Automation toggle button'
        tempLayout.getDistilleryToggleButton().setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {

                // If device id in offline state, no action should be available
                if (tempLayout.isDeviceOnline()) {
                    String message = sharedPreferences.getCustomTempValues(); // Set local string to hold the temperature values

                    if (isChecked) {
                        // The toggle is enabled
                        Toast.makeText(getApplicationContext(), "Automation job started", Toast.LENGTH_LONG).show();
                        // Publish message with relevant topic
                        MqttService.getStaticHandleMQTT().publishMessage(topic + "/automation", "1," + message,
                                AppConfig.RETAINED_MESSAGE);
                        // 'Remember' that automation was published to shared preferences
                        sharedPreferences.setAutomation(true);

                    } else {
                        // The toggle is disabled
                        Toast.makeText(getApplicationContext(), "Automation job stopped", Toast.LENGTH_LONG).show();
                        // Publish message with relevant topic
                        MqttService.getStaticHandleMQTT().publishMessage(topic + "/automation", "0," + message,
                                AppConfig.RETAINED_MESSAGE);
                        // 'Remember' that automation was published to shared preferences
                        sharedPreferences.setAutomation(false);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Device cannot be used while in 'offline' state",
                            Toast.LENGTH_SHORT).show();
                    // Since the above was not executed (device is offline),
                    // The toggle got checked, so it needs to be unchecked
                    if (isChecked)
                        tempLayout.setDistilleryToggleButton(false);
                    }

        });

        mainLayout.addView(tempLayout.getView());
        // And store the device name - to the devices layout object
        inflatedLayoutsIds.put(deviceMap.get("device_id"), tempLayout);
    }


    /**
     * Update MQTT DB with a new pass for customers "device"
     */
    private void updateCustomerDeviceWithToken() {
        Log.d(TAG, "Starting 'updateCustomerDeviceWithToken' function");

        Message showDialog =
                uiHandler.obtainMessage(SHOW_UPDATE_SERVER_DIALOG, pDialog);
        showDialog.sendToTarget();

        MqttService.stopMQTTService(getApplicationContext());

        String tag_activation_req = "req_update_customer_device_token";
        final SharedPreferences sharedPreferences = new SharedPreferences(getApplicationContext());
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_DEVICE_PASS, new JSONObject(), (JSONObject response) -> {

                Message hideDialog =
                        uiHandler.obtainMessage(HIDE_DIALOG, pDialog);

                Log.d(TAG, "Server Response: " + response.toString());
                try {
                    String status = response.getString("status");
                    // Check the response
                    if (status.equals("success")) {
                        // device successfully saved configurations
                        Log.d(TAG, "Password successfully updated on mqtt broker");

                        // Once password updated on server side, connect to mqtt broker
                        MqttService.startMQTTService(getApplicationContext());
                    } else {
                        // Error in configuration. Get the error message
                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.d(TAG, "Json error: " + e.toString());
                    Toast.makeText(getApplicationContext(), "Server Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                hideDialog.sendToTarget();

        }, (VolleyError error) -> {
                Message hideDialog =
                        uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                Log.e(TAG, "onErrorResponse is: " + error);
                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Server Time out error or no connection");
                    Toast.makeText(getApplicationContext(), "Timeout error! Server is not responding", Toast.LENGTH_LONG).show();
                } else {
                    String body;
                    if (error.networkResponse.data != null) {
                        try {

                            body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Log.e(TAG, "updateCustomerDeviceWithToken Error: " + body);
                            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
                hideDialog.sendToTarget();


        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                String auth = "Bearer " + sharedPreferences.getToken();
                header.put("Authorization", auth);
                return header;
            }
        };
        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_activation_req, true);
    }
}

