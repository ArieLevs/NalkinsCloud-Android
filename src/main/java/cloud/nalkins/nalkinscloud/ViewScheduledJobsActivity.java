package cloud.nalkins.nalkinscloud;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arie on 5/22/2017.
 *
 */
public class ViewScheduledJobsActivity extends AppCompatActivity {

    private static final String TAG = ViewScheduledJobsActivity.class.getSimpleName();
    private ProgressDialog pDialog; // 'Processing' dialog

    private SharedPreferences sharedPreferences;

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.back_only_menu, menu);

        setTitle("Scheduled jobs"); // Set the action bat title
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.action_help:
            // Start the 'viewScheduledJobHelpFunction' and send the activity context
            Functions.helpFunction(getApplicationContext());
            return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_scheduled_jobs);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // Set animation when activity starts/ends

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Create new session manager object
        sharedPreferences = new SharedPreferences(getApplicationContext());

        getScheduledJobsFromServer();
    }


    /**
     * Initiate the scheduled jobs layout with all future jobs user have
     * It will get a list of scheduled jobs,
     * Depending on each job, set relevant layout template
     */
    private void getScheduledJobsFromServer(){
        Log.d(TAG, "running 'getScheduledJobsFromServer' function");
        pDialog.setMessage("Retrieving scheduled jobs from server ...");
        Functions.showDialog(pDialog);

        String tag_get_scheduled_jobs_req = "req_get_scheduled_jobs";

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_GET_SCHEDULED_JOB, new JSONObject(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "get_scheduled_jobs Response: " + response.toString());

                try {
                    // Check if status response equals success
                    if (response.getString("status").equals("success")) {
                        // scheduled_job successfully returned
                        Log.d(TAG, "Scheduled job(s) successfully returned:");
                        Functions.hideDialog(pDialog);



                        // Set a JSONArray from 'message' response since its returned as an array
                        JSONArray jsonArray = response.getJSONArray("message");
                        Log.d(TAG, jsonArray.toString());

                        // loop through each json object
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Toast.makeText(getApplicationContext(), jsonArray.getJSONObject(i).toString(), Toast.LENGTH_LONG).show();
                            /*
                            JSONObject job = jsonArray.getJSONObject(i);

                            HashMap<String,String> deviceMap =  new HashMap<>();

                            // Put into hash map relevant values
                            deviceMap.put("device_id", job.getString("device_id"));
                            deviceMap.put("device_type", job.getString("device_type"));
                            deviceMap.put("device_name", job.getString("device_name"));

                            // Add current hash map to the array;
                            devicesList.add(deviceMap);
                            */
                        }
                    } else {
                        // Error in scheduled_job. Get the error message
                        String errorMsg = response.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();

                        Functions.hideDialog(pDialog);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Log.d(TAG, "Json error: " + e.toString());
                    Toast.makeText(getApplicationContext(), "get_scheduled_jobs Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Functions.hideDialog(pDialog);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.e(TAG, "Server Time out error or no connection");
                    Toast.makeText(getApplicationContext(),
                            "Timeout error! Server is not responding for get_scheduled_jobs request",
                            Toast.LENGTH_LONG).show();
                } else {

                    String body;
                    //get response body and parse with appropriate encoding
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "get_scheduled_jobs error: " + body);
                            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                }
                Functions.hideDialog(pDialog);
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
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_get_scheduled_jobs_req, true);
    }
}

