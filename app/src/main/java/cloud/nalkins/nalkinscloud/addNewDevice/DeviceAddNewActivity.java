package cloud.nalkins.nalkinscloud.addNewDevice;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import cloud.nalkins.nalkinscloud.AppConfig;
import cloud.nalkins.nalkinscloud.Functions;
import cloud.nalkins.nalkinscloud.HandleWifiConnection;
import cloud.nalkins.nalkinscloud.NetworkRequests;
import cloud.nalkins.nalkinscloud.R;

/**
 * Created by Arie on 4/8/2017.
 * <p>
 * This activity is the first step of adding a new device,
 * It will give the user a brief instruction,
 * and and option to continue or cancel the process
 */
public class DeviceAddNewActivity extends AppCompatActivity {

    private static final String TAG = DeviceAddNewActivity.class.getSimpleName();

    // Represent the location permission status
    private boolean locationPermFlag = true;
    final int RequestLocationPermissionID = 1002;

    private ProgressDialog pDialog; // 'Processing' dialog

    private static String deviceID; // Will store the return device id from the device
    private static String deviceType; // Will store the return device type from the device

    Handler uiHandler;

    final int SHOW_CONNECT_DIALOG = 2;
    final int SHOW_SEARCH_DIALOG = 1;
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
        setContentView(R.layout.activity_add_device_start);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Set UI Handler to send actions to UI
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SHOW_SEARCH_DIALOG:
                        pDialog.setMessage("Searching " + AppConfig.APP_NAME + " device ...");
                        Functions.showDialog(pDialog);
                        break;
                    case SHOW_CONNECT_DIALOG:
                        pDialog.setMessage("Connecting to " + AppConfig.APP_NAME + " device ...");
                        Functions.showDialog(pDialog);
                        break;
                    case HIDE_DIALOG:
                        Functions.hideDialog(pDialog);
                        break;
                }
            }
        };

        //Set buttons
        Button btn_cancel = findViewById(R.id.cancelButton);
        Button btn_continue = findViewById(R.id.confirmButton);

        // Cancel button function
        btn_cancel.setOnClickListener((View v) -> finish());

        // Continue button function
        btn_continue.setOnClickListener((View v) -> {
            // Start wifi scan and check if device found
            searchNewDevice();
        });

        //HandleWifiConnection.setupWifiManager(getApplicationContext());
        //HandleWifiConnection.bindToDeviceWifiNetwork(getApplicationContext(), AppConfig.DEVICE_AP_SSID);
    }


    /**
     * Initiate a simple wifi scan
     */
    public void searchNewDevice() {
        Message showDialog =
                uiHandler.obtainMessage(SHOW_SEARCH_DIALOG, pDialog);
        showDialog.sendToTarget();

        /* If no permission granted for ACCESS_FINE_LOCATION request it
           The permission is a MUST in order to use wifi network scan
           If the user did not granted the permission, auto wifi connection cannot be established
         */
        if (ContextCompat.checkSelfPermission(DeviceAddNewActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeviceAddNewActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, RequestLocationPermissionID);
        }

        // If user provided location permission then scan for networks
        if (locationPermFlag) {
            Log.d(TAG, "Starting wifi networks scan");
            HandleWifiConnection.setupWifiManager(getApplicationContext());
            // Initiate a wifi scan
            HandleWifiConnection.scanWifiNetworks(getApplicationContext());

            // Start new thread, that will run outside the UI thread
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Message hideDialog =
                            uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                    try {
                        // Set counter timeout int
                        int counter = 0;
                        Log.d(TAG, "Waiting for device on near wifi area");
                        // While device list is null do:
                        while (HandleWifiConnection.isScannedNetworksListEmpty()) {
                            counter++;
                            // If 15 seconds passed, or ScanCompleted then return
                            if (counter == 15 || HandleWifiConnection.isNetworkScanCompleted()) {
                                Toast.makeText(getApplicationContext(), "No networks found", Toast.LENGTH_LONG).show();

                                // Send hide dialog box to UI Thread
                                hideDialog.sendToTarget();
                                return; // Return to main UI
                            }
                            //Wait for response
                            Thread.sleep(1000);
                        }

                        // If scanned network list contains values then
                        final int N = HandleWifiConnection.scannedNetworks.size();
                        boolean foundDevice = false;

                        // Iterate each network, and search if our AppConfig.DEVICE_AP_SSID found
                        for (int i = 0; i < N; ++i) {
                            if (AppConfig.DEVICE_AP_SSID.equals(HandleWifiConnection.scannedNetworks.get(i).SSID))
                                foundDevice = true;
                        }

                        if (foundDevice) {
                            Log.d(TAG, AppConfig.DEVICE_AP_SSID + " Found, moving on");

                            // Create vibrate
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrator != null) {
                                try {
                                    vibrator.vibrate(150);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }


                            // Make sound
                            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                            toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 100);

                            // Start 'connectToDevice'
                            //Functions.hideDialog(pDialog); // Stop dialog
                            connectToDevice();
                        } else {
                            Log.d(TAG, AppConfig.DEVICE_AP_SSID + " Was not found, going back");

                            // Send hide dialog box to UI Thread
                            hideDialog.sendToTarget();
                            Toast.makeText(getApplicationContext(), AppConfig.DEVICE_AP_SSID +
                                    "Was not found, Make sure device is on configuration mode", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        // Send hide dialog box to UI Thread
                        hideDialog =
                                uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                        hideDialog.sendToTarget();
                        Log.d(TAG, "searchNewDevice Thread error: " + e.toString());
                    }
                }
            };
            thread.start();

        } else {
            Log.d(TAG, "activateDevice - locationPermFlag is: FALSE");
            Toast.makeText(getApplicationContext(), "Location permission must be granted to scan Wifi", Toast.LENGTH_LONG).show();
        }
    }


    // Request Wifi permissions if not granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            // Request permissions to use wifi scan (location permissions is needed)
            case RequestLocationPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted");
                    locationPermFlag = true;
                    //addNewDevice();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(DeviceAddNewActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(TAG, "Location permission was not granted");
                        Toast.makeText(getApplicationContext(), "Location permission must be granted to scan Wifi", Toast.LENGTH_LONG).show();
                        locationPermFlag = false;

                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceAddNewActivity.this);
                        builder.setTitle("Critical permission required")
                                .setMessage("This permission in needed to scan wifi networks")
                                .setNegativeButton("OK", null)
                                //        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                //    public void onClick(DialogInterface dialog, int id) {
                                //        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestLocationPermissionID);
                                //   }
                                //});
                                .create()
                                .show();
                    } else {
                        Log.d(TAG, "Location permission was not granted, using 'never ask again'");
                        locationPermFlag = false;
                        Toast.makeText(getApplicationContext(), "Location permission must be granted to scan Wifi", Toast.LENGTH_LONG).show();
                        //Never ask again and handle app without permission.
                    }
                }
            }
            break;
        }
    }


    /**
     * Function will run from within 'searchNewDevice'
     * Only if a device was found this function will execute
     */
    public void connectToDevice() {
        Log.d(TAG, "Starting 'connectToDeviceAP'");

        // Change the dialog text and send to UI Thread
        Message completeMessage =
                uiHandler.obtainMessage(SHOW_CONNECT_DIALOG, pDialog);
        completeMessage.sendToTarget();

        // Start connection to device access point
        HandleWifiConnection.connectToWifiNetwork(getApplicationContext(),
                AppConfig.DEVICE_AP_SSID,
                AppConfig.DEVICE_AP_PASS);

        Thread thread = new Thread() {

            // Set counter timeout int
            int counter = 0;

            @Override
            public void run() {
                Message hideDialog =
                        uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
                try {
                    // check if connected to AppConfig.DEVICE_AP_SSID
                    Log.d(TAG, "Waiting for connection to device AP");

                    // Get connection to device status for first time
                    int tempInt = HandleWifiConnection.isConnectedToSSID(getApplicationContext()
                            , AppConfig.DEVICE_AP_SSID);
                    // While app is NOT connected to 'DEVICE_AP_SSID' do:
                    while (tempInt != 1) {
                        Log.d(TAG, "isConnectedToSSID Counter is: " + counter);
                        counter++;
                        // If reached to counter limit (40), or got connected to some wifi that is not 'DEVICE_AP_SSID'
                        if ((counter == 40) || (tempInt == -1)) {
                            Log.e(TAG, "Failed to connect to: " + AppConfig.DEVICE_AP_SSID);

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
                    // If this step of the function reached, then connection was established
                    /*
                    while (!HandleWifiConnection.isDeviceSSIDBind) {
                        Log.d(TAG, "isDeviceSSIDBind Counter is: " + counter);
                        counter++;
                        if (counter == 40) {
                            // Send hide dialog box to UI Thread
                            hideDialog.sendToTarget();
                            return;
                        }
                        Thread.sleep(1000);
                    }*/

                    // Start TCP\IP connection with the device and send relevant values
                    // Start device configuration
                    getDeviceIdFromDevice();
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
     * Start HTTP session with the MQTT device and get its device ID
     */
    public void getDeviceIdFromDevice() {
        Log.d(TAG, "Starting 'getDeviceIdFromDevice' function");

        String tag_activation_req = "req_device_id";

        // Send these JSON parameters
        Map<String, Boolean> params = new HashMap<>();
        params.put("give_id", true);

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.DEVICE_GET_ID, new JSONObject(params), (JSONObject response) -> {

            Log.d(TAG, "Device Response: " + response.toString());

            // Send hide dialog box to UI Thread
            Message hideDialog =
                    uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
            hideDialog.sendToTarget();
            try {
                String status = response.getString("status");
                // Check if response contains any values
                if (status.equals("success")) {
                    // device successfully returned its id
                    deviceID = response.getString("device_id");
                    deviceType = response.getString("device_type");

                    Functions.stopWifiConnectionProcedure(getApplication());

                    HandleWifiConnection.destroyWifiManager();

                    // Launch get Wifi credentials activity
                    Intent intent = new Intent(DeviceAddNewActivity.this,
                            DeviceSetNameActivity.class);
                    startActivity(intent);

                } else {
                    // Error in configuration. Get the error message
                    Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                    // Function will stop connection to device AP
                    Functions.stopWifiConnectionProcedure(getApplication());
                }
            } catch (JSONException e) {
                // JSON error
                Log.d(TAG, "Json error: " + e.toString());
                Toast.makeText(getApplicationContext(), "Device Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // Function will stop connection to device AP
                Functions.stopWifiConnectionProcedure(getApplication());
            }
        }, (VolleyError error) -> {
            Log.e(TAG, "onErrorResponse is: " + error);

            // Send hide dialog box to UI Thread
            Message hideDialog =
                    uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
            hideDialog.sendToTarget();

            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                Log.e(TAG, "Device Time out error or no connection");
                Toast.makeText(getApplicationContext(), "Timeout error! Device is not responding", Toast.LENGTH_LONG).show();
            } else {

                String body;
                if (error.networkResponse.data != null) {
                    try {
                        body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e(TAG, "Activation Error: " + body);
                        Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            // Function will stop connection to device AP
            Functions.stopWifiConnectionProcedure(getApplication());
        });

        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_activation_req, false);
    }


    /**
     * Return private string 'deviceId'
     *
     * @return String Return current device id
     */
    public static String getDeviceId() {
        return deviceID;
    }


    /**
     * Return private string 'deviceType'
     *
     * @return String Return current device type
     */
    public static String getDeviceType() {
        return deviceType;
    }
}
