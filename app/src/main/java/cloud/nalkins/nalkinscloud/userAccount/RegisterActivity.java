package cloud.nalkins.nalkinscloud.userAccount;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import cloud.nalkins.nalkinscloud.NetworkRequests;
import cloud.nalkins.nalkinscloud.R;

/**
 * Created by Arie on 3/8/2017.
 *
 * RegisterActivity will perform registration process to the server
 */

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        //return true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.register_menu, menu);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_help:
                // Start the 'registerHelpFunction' and send the activity context
                Functions.helpFunction(getApplicationContext());
                return(true);
            case R.id.action_legal:
                // Start the 'legalFunction' and send the activity context
                Functions.legalFunction(getApplicationContext());
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Set name email and password text fields
        final TextInputLayout first_nameWrapper = (TextInputLayout) findViewById(R.id.first_nameWrapper);
        final TextInputLayout last_nameWrapper = (TextInputLayout) findViewById(R.id.last_nameWrapper);
        final TextInputLayout emailWrapper = (TextInputLayout) findViewById(R.id.emailWrapper);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);
        final TextInputLayout validate_passwordWrapper = (TextInputLayout) findViewById(R.id.validate_passwordWrapper);

        //Set error hints when validation fail
        first_nameWrapper.setHint("First Name");
        last_nameWrapper.setHint("Last Name");
        emailWrapper.setHint("Email");
        passwordWrapper.setHint("Password");
        validate_passwordWrapper.setHint("Validate Password");

        //Set register buttons
        Button registerButton = (Button) findViewById(R.id.btnRegister);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Functions.hideDialog(pDialog);

                String first_name = first_nameWrapper.getEditText().getText().toString();
                String last_name = last_nameWrapper.getEditText().getText().toString();
                String email = emailWrapper.getEditText().getText().toString();
                String password = passwordWrapper.getEditText().getText().toString();
                String validate_password = validate_passwordWrapper.getEditText().getText().toString();

                Log.d(TAG, "Running form validation");
                //Check form validation
                passwordWrapper.setErrorEnabled(false);

                if (!first_name.isEmpty() && Functions.isAlphabet(first_name)) {
                    first_nameWrapper.setErrorEnabled(false);
                    if (first_name.length() >= 2 && first_name.length() <= 32) { // Check if first name valid
                        first_nameWrapper.setErrorEnabled(false);
                        if (!last_name.isEmpty() && Functions.isAlphabet(last_name)) {
                             last_nameWrapper.setErrorEnabled(false);
                             if (last_name.length() >= 2 && last_name.length() <= 32) { // Check if last name is valid
                                 last_nameWrapper.setErrorEnabled(false);
                                     if ((Functions.validateEmail(email))) { // Check if email valid
                                         emailWrapper.setErrorEnabled(false);
                                         if ((Functions.isValidPassword(password))) { // Check if password valid
                                             passwordWrapper.setErrorEnabled(false);
                                             if (Functions.isValidPassword(validate_password)) { // Check if second password valid
                                                 validate_passwordWrapper.setErrorEnabled(false);
                                                 if (password.equals(validate_password)) { // Check if both passwords math
                                                     Log.d(TAG, "Validation form passed");
                                                     doRegistration(first_name, last_name, email, password); // Start registration process
                                                 } else
                                                     Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_SHORT).show();
                                             } else
                                                 validate_passwordWrapper.setError("Not a valid password!");
                                         } else
                                             passwordWrapper.setError("Not a valid password!");
                                     } else
                                         emailWrapper.setError("Invalid email");

                             } else
                                 last_nameWrapper.setError("Last name must be 2 - 32 characters long");
                         } else
                            last_nameWrapper.setError("Last name must be valid alphabet");
                    } else
                        first_nameWrapper.setError("First name must be 2 - 32 characters long");
                } else
                    first_nameWrapper.setError("First name must be valid alphabet");
            }
        });
    }


    /**
     * Perform registration request to the server
     *
     * @param firstName inputted first name
     * @param lastName inputted last name
     * @param email inputted email
     * @param password inputted password
     */
    private void doRegistration(String firstName, String lastName, String email, String password) {
        Log.d(TAG, "Running 'doRegistration' function");
        String tag_registration_req = "req_registration";

        pDialog.setMessage("Registering ...");
        Functions.showDialog(pDialog);

        // Send these JSON parameters, also include CLIENT_ID and CLIENT_SECRET to identify the app
        Map<String, String> params = new HashMap<>();
        params.put("client_secret", AppConfig.OAUTH_CLIENT_SECRET);
        params.put("first_name", firstName);
        params.put("last_name", lastName);
        params.put("email", email);
        params.put("password", password);

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new JSONObject(params), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Login Response: " + response.toString());
                Functions.hideDialog(pDialog);

                try {
                    String status = response.getString("status");
                    // Check if the token contains any values
                    if (status.equals("success")) {
                        // user successfully logged in
                        Toast.makeText(getApplicationContext(), "Registration successfully completed", Toast.LENGTH_LONG).show();

                        // Run registration success activity
                        Intent intent = new Intent(getApplicationContext(),
                                RegisterConfirmActivity.class);
                        startActivity(intent);
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = response.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(),
                            "Timeout error! Server is not responding",
                            Toast.LENGTH_LONG).show();
                } else {

                    String body;
                    //get status code here
                    try {
                        String statusCode = String.valueOf(error.networkResponse.statusCode);
                        Log.e(TAG, "Server response code: " + statusCode);
                        Toast.makeText(getApplicationContext(), statusCode, Toast.LENGTH_LONG).show();

                        if (error.networkResponse.data != null) {
                            try {
                                body = new String(error.networkResponse.data, "UTF-8");
                                Log.e(TAG, "Login Error: " + body);
                                Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (NullPointerException e) {
                        Log.e(TAG, "Login Error: " + e.toString());
                    }

                }
                Functions.hideDialog(pDialog);
            }
        });

        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_registration_req, true);
    }
}


