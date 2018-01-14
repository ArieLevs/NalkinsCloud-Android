package cloud.nalkins.nalkinscloud;

import android.app.ProgressDialog;
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

/**
 * Created by Arie on 12/31/2017.
 *
 */

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = ForgotPasswordActivity.class.getSimpleName();
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
        setContentView(R.layout.activity_forgot_password);

        final TextInputLayout emailWrapper = (TextInputLayout) findViewById(R.id.forgotPassEmailWrapper);
        emailWrapper.setHint("Email");

        //Set forgot password button
        Button registerButton = (Button) findViewById(R.id.btnForgotPass);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.hideDialog(pDialog);

                String email = emailWrapper.getEditText().getText().toString();

                if ((Functions.validateEmail(email))) { // Check if email valid
                    emailWrapper.setErrorEnabled(false);

                    requestServerForPasswordReset(email);
                } else
                    emailWrapper.setError("Invalid email");
            }
        });
    }

    /**
     * Send server request to reset password
     * @param email the email address to reset
     */
    private void requestServerForPasswordReset(String email) {
        Log.d(TAG, "Running 'requestServerForPasswordReset' function");

        String tag_password_reset = "req_forgot_password";

        pDialog.setMessage("Please wait ...");
        Functions.showDialog(pDialog);

        // Session manager
        sharedPreferences = new SharedPreferences(getApplicationContext());

        // Send these JSON parameters, also include CLIENT_ID and CLIENT_SECRET to identify the app
        Map<String, String> params = new HashMap<>();
        params.put("client_secret", AppConfig.OAUTH_CLIENT_SECRET);
        params.put("email", email);

        JSONObject jsonObject = new JSONObject(params);
        Log.d(TAG, jsonObject.toString());
        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_FORGOT_PASSWORD, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Forgot password: " + response.toString());
                Functions.hideDialog(pDialog);

                try {
                    String status = response.getString("status");
                    // Check if status is success
                    if (status.equals("success")) {
                        Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();

                        // Back to previous activity
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
        });
        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_password_reset, true);
    }
}
