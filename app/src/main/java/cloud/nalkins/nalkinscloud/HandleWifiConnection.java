package cloud.nalkins.nalkinscloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

/**
 * Created by Arie on 3/19/2017.
 *
 */
public class HandleWifiConnection extends AppCompatActivity {

    private static final String TAG = HandleWifiConnection.class.getSimpleName();
    public static WifiManager wifiManager;

    public static int deviceNetworkId;
    private static WifiInfo localNetworkInfo;

    public static List<ScanResult> scannedNetworks;
    public static boolean isNoNetworksFoundFlag;

    public static boolean isDeviceSSIDBind = false;

    private static ConnectivityManager mConnectivityManager;
    private static ConnectivityManager.NetworkCallback mNetworkCallback;

    public static WifiManager getWifiManager() {
        return wifiManager;
    }

    /**
     * Reconnect to a pre-remembered wifi network
     *
     * @return  True - successfully reconnected to pre-remembered network
     *          False - device was not connected to wifi network
     */
    public static boolean reconnectToLocalNetwork() {
        Log.d(TAG, "Running 'reconnectToLocalNetwork'");
        // If any info in 'localNetworkInfo' (then app was connected to wifi before add device procedure)
        if (localNetworkInfo != null) {
            // Enable (set active) the 'localNetworkInfo'
            if (!wifiManager.enableNetwork(localNetworkInfo.getNetworkId(), true)) {
                Log.d(TAG, "Failed to enable wifi network");
                return false;
            }
            // Connect to the Wifi configured above
            if (!wifiManager.reassociate()) { // Using re-associate
                Log.d(TAG, "Failed to connect to: " + AppConfig.DEVICE_AP_SSID);
                return false;
            }
            Log.d(TAG, "Successfully reconnected to: " + localNetworkInfo.getSSID());

            return true;
        }
        return false;
    }


    /**
     * Connect to access point
     * Note - function does not support open networks (password protected only)
     *
     * @param context Context of activity which initiated this function
     * @param ssid the SSID to connect to
     * @param password the password to use with the SSID
     *
     * @return  True - successfully connected to given SSID
     *          False - Failed to connect to input SSID
     */
    public static boolean connectToWifiNetwork(final Context context, String ssid, String password) {
        Log.d(TAG, "Running 'connectToDeviceAP'");

        // If android is currently connected to wifi network then remember it
        if(isNetworkConnected(context) == 1) {
            // Save current network configuration
            localNetworkInfo = wifiManager.getConnectionInfo();
            Log.d(TAG, "Current network configuration saved as: " + localNetworkInfo.getSSID());

            // Disconnect from current wifi
            if(!wifiManager.disconnect()) {
                Log.d(TAG, "Failed to disconnect from network: " + localNetworkInfo.getSSID());
                return false;
            }
            else
                Log.d(TAG, "Successfully disconnected from network: " + localNetworkInfo.getSSID());
        }

        // Set wifi configurations
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", password);

        // Remember current network id
        deviceNetworkId = wifiManager.addNetwork(wifiConfig);
        Log.d(TAG, "Set wifi configs (based on the device AP settings), " +
                "\nSSID: " + wifiConfig.SSID +
                "\nPASS: " + wifiConfig.preSharedKey +
                "\nNetworkID: " + deviceNetworkId);

        // Enable (set active) the above configured wifi
        if(!wifiManager.enableNetwork(deviceNetworkId, true)) {
            Log.d(TAG, "Failed to enable wifi network: " + deviceNetworkId);
            return false;
        }
        else
            Log.d(TAG, "Successfully enabled wifi network: " + deviceNetworkId);


        // Connect to the Wifi configured above
        if(!wifiManager.reconnect()) {
            Log.d(TAG, "Failed to connect to: " + AppConfig.DEVICE_AP_SSID);
            return false;
        }
        else
            Log.d(TAG, "Successfully connected to: " + AppConfig.DEVICE_AP_SSID);

        return true;
    }

    /*public static void bindToDeviceWifiNetwork(final Context context, final String ssid) {
        Log.d(TAG, "Running 'bindToDeviceWifiNetwork' function" );
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        //set the transport type do WIFI
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.d(TAG, "Crrent network is: " + wifiManager.getConnectionInfo().getSSID());
                String tmp = String.format("\"%s\"", AppConfig.DEVICE_AP_SSID);
                Log.d(TAG, "TMP IS: "+ tmp + " Current network is: " + wifiManager.getConnectionInfo().getSSID());
                Log.d(TAG, network.toString());
                if (wifiManager.getConnectionInfo().getSSID().equals(tmp)) {
                    connectivityManager.bindProcessToNetwork(network);
                    isDeviceSSIDBind = true;
                    Log.d(TAG, "isDeviceSSIDBind ser to TRUE" );
                } else {
                    connectivityManager.bindProcessToNetwork(null);
                    isDeviceSSIDBind = false;
                    Log.d(TAG, "isDeviceSSIDBind ser to FALSE" );
                }
                /*try {

                    //isDeviceSSIDBind = false;
                    //Log.d(TAG, "network bind removed successful" );
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                //connectivityManager.unregisterNetworkCallback(this);
            }
        });
    }*/


    /**
     * Disconnect from input 'WifiManager'
     *
     * @param context Context of activity which initiated this function
     * @param wifi The wifi the function will disconnect from
     */
    public static void disconnectFromWifi(Context context, WifiManager wifi) {
        Log.d(TAG, "Running 'disconnectFromWifi'");
        if(isNetworkConnected(context) == 1) { // If connected to TYPE_WIFI network
            wifi.disconnect();
            Log.d(TAG, "Disconnected from: " + wifi.getConnectionInfo().getSSID());
            return;
        }
        Log.d(TAG, "Android was not connected to wifi");
    }


    /**
     * Remove a given network ID from a given WifiManger
     *
     * @param wifi a WifiManager we want to removed the network from
     * @param networkId the network id to remove
     *
     * @return  True - Succeeded to remove the current network
     *          False - Not succeeded to remove the current network
     */
    public static boolean forgetNetwork(WifiManager wifi, int networkId) {
        Log.d(TAG, "Running 'forgetDeviceAP'");
        String networkToRemove = wifi.getConnectionInfo().getSSID();
        if(wifi.removeNetwork(networkId)) { // If succeeded to remove the current network
            wifi.saveConfiguration();
            Log.d(TAG, "Network with ID: " + networkId + ", SSID: " + networkToRemove + " Removed");
            return true;
        }
        Log.d(TAG, "Failed to removed network: " + networkToRemove + "with ID: " + networkId);
        return false;
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }


    /**
     * Check if network connection is available
     *
     * @param context Context of activity which initiated this function
     * @return  1 - Wifi connection
     *          2 - Cellular connection
     *          0 - No connection
     */
    public static int isNetworkConnected(Context context) {
        Log.d(TAG, "Running 'isNetworkAvailable'");
        NetworkInfo activeNetwork = getNetworkInfo(context);
        if (activeNetwork != null && activeNetwork.isConnected()) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, "Found TYPE_WIFI (1)");
                return 1;
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.d(TAG, "Found TYPE_MOBILE (2)");
                return 2;
            }
        }
        Log.d(TAG, "No network connected");
        return 0;
    }


    /**
     * Check if current wifi connection, is connected to input SSID
     * @param context Context of activity which initiated this function
     * @param ssid SSID to check connection to
     * @return  1 - wifi is connected to input SSID (param)
     *          -1 - wifi is connected to other SSID (then the input one)
     *          0 - no wifi connection
     */
    public static int isConnectedToSSID(Context context, String ssid) {
        Log.d(TAG, "Running 'isConnectedToDevice'");

        // If android connected to wifi
        if(isNetworkConnected(context) == 1) { // If 1 then current connection is wifi connection
            // Get current network info
            WifiInfo info = wifiManager.getConnectionInfo();
            // Print current network (by removing the "" on the sides)
            Log.d(TAG, "Current network is: " + info.getSSID().replace("\"", ""));
            // If the connected network is the device then
            if(info.getSSID().replace("\"", "").equals(ssid))
                return 1;
            // Else, if app connected to some other wifi
            return -1;
        }
        Log.d(TAG, "No wifi Connection detected");
        return 0;
    }


    /**
     * Scan for available wifi networks around
     *
     * @param context Context of activity which initiated this function
     */
    public static void scanWifiNetworks (Context context) {
        Log.d(TAG, "Running 'scanWifiNetworks'");
        final WifiManager mWifiManager = getWifiManager();
        if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            // First reset the list
            scannedNetworks = null;
            // Reset the flag to false
            isNoNetworksFoundFlag = false;

            // register WiFi scan results receiver
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

            context.registerReceiver(new BroadcastReceiver(){
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Set the scanned results to global 'scannedNetworks' variable
                    scannedNetworks = mWifiManager.getScanResults();
                    isNoNetworksFoundFlag = true; // Set the flag to true to indicate a scan has completed
                }
            }, filter);

            // start WiFi Scan
            mWifiManager.startScan();
        }
    }


    public static boolean isScannedNetworksListEmpty () {
        return (HandleWifiConnection.scannedNetworks == null);
    }

    public static boolean isNetworkScanCompleted () {
        return isNoNetworksFoundFlag;
    }

    public static void setupWifiManager (Context context) {
        // Setup wifi manager
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) { // If wifi not running, start wifi
            wifiManager.setWifiEnabled(true);
            Log.d(TAG, "Wifi is disabled, enabling Wifi");
        }
        Log.d(TAG, "WifiManager Configured");
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        // ON airplane mode activeNetwork will be null
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    public static void destroyWifiManager() {
        wifiManager = null;
    }

    private static void unregisterNetworkCallback() {
        if (mNetworkCallback != null) {
            Log.d(TAG, "Unregistering network callback");
            mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            mNetworkCallback = null;
        }
    }

    public static void createConnectivityManager(Context context) {
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    public static void bindToDeviceWifiNetwork(final Context context) {
        // Before requesting a high-bandwidth network, ensure prior requests are invalidated.
        unregisterNetworkCallback();

        Log.d(TAG, "Requesting high-bandwidth network");

        // Requesting an unmetered network may prevent you from connecting to the cellular
        // network on the user's watch or phone; however, unless you explicitly ask for permission
        // to a access the user's cellular network, you should request an unmetered network.
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(final Network network) {
                Log.d(TAG, "BLAAAAAAAAAAAAAa");
                setupWifiManager(context);
                if (getWifiManager().getConnectionInfo().getSSID().equals(AppConfig.DEVICE_AP_SSID)) {
                    if (!mConnectivityManager.bindProcessToNetwork(network)) {
                        Log.e(TAG, "ConnectivityManager.bindProcessToNetwork()"
                                + " requires android.permission.INTERNET");
                    } else {
                        Log.d(TAG, "Network available: " + network.toString());
                    }
                }
            }

            @Override
            public void onCapabilitiesChanged(Network network,
                                              NetworkCapabilities networkCapabilities) {
                Log.d(TAG, "Network capabilities changed");
            }

            @Override
            public void onLost(Network network) {
                Log.d(TAG, "Network lost");
            }
        };

        // requires android.permission.CHANGE_NETWORK_STATE
        mConnectivityManager.requestNetwork(request, mNetworkCallback);

    }

    private void releaseDeviceWifiNetwork() {
        mConnectivityManager.bindProcessToNetwork(null);
        unregisterNetworkCallback();
    }
}
