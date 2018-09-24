package cloud.nalkins.nalkinscloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Arie on 3/8/2017.
 *
 */

public class Functions {

    private static final String TAG = Functions.class.getSimpleName();

    public static void showDialog(ProgressDialog pDialog) {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    public static void hideDialog(ProgressDialog pDialog) {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    static boolean validateUsername(String username) {
        return (!username.isEmpty() && username.length() >= 6 && isAlphanumeric(username));
    }

    public static boolean validateEmail(String email) {
        return (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static boolean isValidPassword(String password) {
        return (password.length() >= 8 && password.length() <= 16);
    }

    // Redirect user to NalkinsCloud Project readme page
    public static void helpFunction(Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.NALKINS_CLOUD_ANDROID_README_URL));
        context.startActivity(browserIntent);
    }

    // Redirect user to NalkinsCloud Project license page
    public static void legalFunction(Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.NALKINS_CLOUD_ANDROID_LICENSE_URL));
        context.startActivity(browserIntent);
    }

    public static boolean isAlphanumeric(String str) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c) && c!=' ')
                return false;
        }
        return true;
    }


    /**
     * Check if input string contains alphabet characters only
     *
     * @param value input string to validate
     * @return True if input string contains alphabet characters only or False if not
     */
    public static boolean isAlphabet(String value) {
        for (int i=0; i<value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isLetter(c))
                return false;
        }
        return true;
    }


    /**
     * Stop WiFi connection to device AP
     * Stop UI dialog, disconnect from device AP, and forget the network
     *
     * @param context context we are working on
     */
    public static void stopWifiConnectionProcedure(Context context) {
        Log.d(TAG, "Running 'stopConnectionProcedure'");

        // After configuration done, disconnect
        HandleWifiConnection.disconnectFromWifi(context, HandleWifiConnection.wifiManager);

        // Forget the network
        HandleWifiConnection.forgetNetwork(HandleWifiConnection.wifiManager,
                HandleWifiConnection.deviceNetworkId);

        // Reconnect to previous Wifi, if was connected
        HandleWifiConnection.reconnectToLocalNetwork();
    }

    static void revokeToken(Context context) {
        Log.d(TAG, "Running 'revokeToken' function");
        // Tag used to cancel the request
        String tag_revoke_token = "req_revoke_token";

        final SharedPreferences sharedPreferences = new SharedPreferences(context);

        // Start new StringRequest (HTTP)
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REVOKE_TOKEN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Revoke Token Response: " + response);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Server Time out error or no connection");
                } else {

                    String body;
                    //get status code here
                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                    Log.e(TAG, "Server response code: " + statusCode);
                    //get response body and parse with appropriate encoding
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Login Error: " + body);
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("client_id", AppConfig.OAUTH_CLIENT_ID);
                params.put("client_secret", AppConfig.OAUTH_CLIENT_SECRET);
                params.put("token", sharedPreferences.getToken());
                return params;
            }
        };

        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_revoke_token, true);
    }


}
