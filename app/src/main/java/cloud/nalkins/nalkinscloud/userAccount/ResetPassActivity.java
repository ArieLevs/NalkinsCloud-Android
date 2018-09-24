package cloud.nalkins.nalkinscloud.userAccount;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
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
import cloud.nalkins.nalkinscloud.MqttService;
import cloud.nalkins.nalkinscloud.NetworkRequests;
import cloud.nalkins.nalkinscloud.R;
import cloud.nalkins.nalkinscloud.SharedPreferences;
import cloud.nalkins.nalkinscloud.login.LoginActivity;

/**
 * Created by Arie on 1/2/2018.
 *
 */

public class ResetPassActivity extends AppCompatActivity {
    private static final String TAG = ResetPassActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    SharedPreferences sharedPreferences;

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
        setContentView(R.layout.activity_restart_password);

        //Set password text fields
        final TextInputLayout currentPassWrapper = (TextInputLayout) findViewById(R.id.resetCurrentPassWrapper);
        final TextInputLayout newPassWrapper = (TextInputLayout) findViewById(R.id.resetPassNewWrapper);
        final TextInputLayout confirmPassWrapper = (TextInputLayout) findViewById(R.id.resetPassConfirmWrapper);

        //Set error hints when validation fail
        currentPassWrapper.setHint("Current Password");
        newPassWrapper.setHint("New Password");
        confirmPassWrapper.setHint("Confirm Username");

        Button registerButton = (Button) findViewById(R.id.btnResetPass);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = currentPassWrapper.getEditText().getText().toString();
                String newPassword = newPassWrapper.getEditText().getText().toString();
                String confirmNewPassword = confirmPassWrapper.getEditText().getText().toString();


                Functions.hideDialog(pDialog);
                if ((Functions.isValidPassword(currentPassword))) { // Check if current password valid
                    currentPassWrapper.setErrorEnabled(false);
                    if ((Functions.isValidPassword(newPassword))) { // Check if new password valid
                        newPassWrapper.setErrorEnabled(false);
                        if (Functions.isValidPassword(confirmNewPassword)) { // Check if second password valid
                            confirmPassWrapper.setErrorEnabled(false);
                            if (newPassword.equals(confirmNewPassword)) { // Check if both passwords math
                                Log.d(TAG, "Validation form passed");

                                doPasswordRestart(currentPassword, newPassword);
                            } else
                                Toast.makeText(getApplicationContext(), "Passwords does not match", Toast.LENGTH_SHORT).show();
                        } else
                            confirmPassWrapper.setError("Not a valid password!");
                    } else
                        newPassWrapper.setError("Not a valid password!");
                } else
                    currentPassWrapper.setError("Not a valid password!");
            }
        });
    }

    private void doPasswordRestart(String currentPassword, String newPassword) {
        Log.d(TAG, "Running 'doPasswordRestart' function");

        String tag_password_reset = "req_password_reset";

        pDialog.setMessage("Please wait ...");
        Functions.showDialog(pDialog);

        // Session manager
        sharedPreferences = new SharedPreferences(getApplicationContext());

        // Send these JSON parameters, also include CLIENT_ID and CLIENT_SECRET to identify the app
        Map<String, String> params = new HashMap<>();
        params.put("current_password", currentPassword);
        params.put("new_password", newPassword);

        JSONObject jsonObject = new JSONObject(params);
        Log.d(TAG, jsonObject.toString());
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_RESET_PASSWORD, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Password Restart: " + response.toString());
                Functions.hideDialog(pDialog);
                try {
                    String status = response.getString("status");
                    // Check if status is success
                    if (status.equals("success")) {
                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();

                        sharedPreferences.removeToken(); // Remove access token from shared preferences
                        sharedPreferences.removeUsername(); // Remove username from shared preferences

                        // Stop MQTT service
                        MqttService.stopMQTTService(getApplicationContext());

                        // Launching the login activity
                        Intent intent = new Intent(ResetPassActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    Log.d(TAG, "Json error: " + e.toString());
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Server Time out error or no connection");
                } else {
                    String body;
                    //get response body and parse with appropriate encoding
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Error: " + body);
                            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // If connection error occurred during forgot password return with error
                Toast.makeText(getApplicationContext(), "Connection error occurred" +
                        ", Could not execute task", Toast.LENGTH_LONG).show();
                Functions.hideDialog(pDialog);
                finish();
            }
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
}
