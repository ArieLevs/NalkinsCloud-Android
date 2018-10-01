package cloud.nalkins.nalkinscloud.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cloud.nalkins.nalkinscloud.AppConfig;
import cloud.nalkins.nalkinscloud.userAccount.ForgotPasswordActivity;
import cloud.nalkins.nalkinscloud.Functions;
import cloud.nalkins.nalkinscloud.MainActivity;
import cloud.nalkins.nalkinscloud.NetworkRequests;
import cloud.nalkins.nalkinscloud.R;
import cloud.nalkins.nalkinscloud.userAccount.RegisterActivity;
import cloud.nalkins.nalkinscloud.SharedPreferences;

/**
 * Class handles the login action of the app
 *
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName(); //Set TAG for logs
    int loginAttemptCounter = 3; // Counter for failed login attempts
    private ProgressDialog pDialog; // 'Loading' dialog

    Handler uiHandler;
    final int SHOW_LOGIN_DIALOG = 1;
    final int HIDE_DIALOG = 0;

    private SharedPreferences sharedPreferences; // Store info to shared preferences

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        //return true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_help:
                // Start the 'loginHelpFunction' and send the activity context
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
        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // Set animation when activity starts/ends

        //Set email email text fields
        final TextInputLayout emailWrapper = (TextInputLayout) findViewById(R.id.emailWrapper);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);

        //Set error hints when validation fail
        emailWrapper.setHint("Email");
        passwordWrapper.setHint("Password");

        //Set login register forgot buttons
        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button registerButton = (Button) findViewById(R.id.registerButton);
        Button forgotPassButton = (Button) findViewById(R.id.forgotPasswordButton);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Set UI Handler to send actions to UI
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SHOW_LOGIN_DIALOG:
                        pDialog.setMessage("Logging in ...");
                        Functions.showDialog(pDialog);
                        break;
                    case HIDE_DIALOG:
                        Functions.hideDialog(pDialog);
                        break;
                }
            }
        };

        // Set session manager
        sharedPreferences = new SharedPreferences(getApplicationContext());

        // Login button function
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                // Get the email and data from text fields
                String email = emailWrapper.getEditText().getText().toString();
                String password = passwordWrapper.getEditText().getText().toString();

                passwordWrapper.setErrorEnabled(false); //Set password error to false
                if (Functions.validateEmail(email)) { // If email is valid then
                    emailWrapper.setErrorEnabled(false); //Set email error to false
                    if (Functions.isValidPassword(password)) { // If password name is valid then
                        passwordWrapper.setErrorEnabled(false);
                        requestNewAccessToken(email, password); //Perform login operation
                    } else
                        passwordWrapper.setError("Not a valid password!");
                } else
                    emailWrapper.setError("Invalid email");

                //If validation failed for 3 times, then
                loginAttemptCounter--;
                if (loginAttemptCounter == 0) {
                    Toast.makeText(getApplicationContext(), "Forgot your Password?", Toast.LENGTH_SHORT).show();
                    loginAttemptCounter = 3;
                // b1.setEnabled(false);
                }
            }
        });

        //Register button function
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(regIntent);
            }
        });

        //Forgot password button function
        forgotPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }


    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public void requestNewAccessToken(final String username, final String password) {
        Log.d(TAG, "Running 'requestNewAccessToken' function");
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        final Message showDialog =
                uiHandler.obtainMessage(SHOW_LOGIN_DIALOG, pDialog);
        final Message hideDialog =
                uiHandler.obtainMessage(HIDE_DIALOG, pDialog);
        showDialog.sendToTarget();

        // Start new StringRequest (HTTP)
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_AUTHENTICATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                hideDialog.sendToTarget();
                Log.d(TAG, "Login Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    String token = jObj.getString("access_token");
                    // Check if the token contains any values
                    if (!token.equals("")) {
                        // user successfully logged in
                        // Create login session
                        sharedPreferences.setToken(token);
                        sharedPreferences.setRefreshToken(jObj.getString("refresh_token"));
                        sharedPreferences.setUsername(username);

                        // Launch main activity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        getApplicationContext().startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        Log.d(TAG, "Error message: " + jObj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), "Log in failed", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.d(TAG, "doLogin Thread error: " + e.toString());
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
                    try {
                        //get status code here
                        String statusCode = String.valueOf(error.networkResponse.statusCode);
                        Log.e(TAG, "Server response code: " + statusCode);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    //get response body and parse with appropriate encoding
                    try {
                        body = new String(error.networkResponse.data, "UTF-8");
                        Log.e(TAG, "Login Error: " + body);
                        Toast.makeText(getApplicationContext(),
                                "Wrong credentials or email was not verified",
                                Toast.LENGTH_LONG).show();
                    } catch (UnsupportedEncodingException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                hideDialog.sendToTarget();
            }
        }) {
            // Set the OAUTH2 header to hold the client ID + client secret
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String auth = "Basic "
                        + Base64.encodeToString((AppConfig.OAUTH_CLIENT_ID
                                + ":" + AppConfig.OAUTH_CLIENT_SECRET).getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("grant_type","password");
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        // Adding request to request queue
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_string_req, true);
    }


    // Override the back button option
    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }
}
