package cloud.nalkins.nalkinscloud;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Arie on 5/8/2017.
 *
 */
public class DateTimePickerActivity extends AppCompatActivity {
    private static final String TAG = DateTimePickerActivity.class.getSimpleName(); //Set TAG for logs

    DatePickerDialog datePickerDialog;
    SharedPreferences sharedPreferences;

    private ProgressDialog pDialog; // 'Processing' dialog
    static Handler uiHandler;
    final int SHOW_UPDATE_SERVER_DIALOG = 1;
    final int HIDE_DIALOG = 0;

    // Define AtomicBoolean since we need to use it in an inner class
    // Holds a boolean indication if the 'Once' or 'Repeat' selected
    private AtomicBoolean isRepeatJobSelected;
    // Holds a boolean indication if the 'On' or 'Off' selected
    private AtomicBoolean isStartJobActionSelected;

    // Define AtomicBoolean since we need to use it in an inner class,
    // Represents if an end time should be set
    final AtomicBoolean isEndDateLayoutVisible = new AtomicBoolean(false);

    // Declare 7 AtomicBoolean vars for days of week, 0 - sunday, 1 - monday, etc
    // { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" }
    private AtomicBoolean[] daysOfWeek = new AtomicBoolean[7];

    private SparseArray<String> dayOfWeekArray = new SparseArray<String>() {
        {
            append(0,"sunday");
            append(1,"monday");
            append(2,"tuesday");
            append(3,"wednesday");
            append(4,"thursday");
            append(5,"friday");
            append(6,"saturday");
        }
    };

    Date startDate = null, endDate = null;
    private boolean isStartDateSelected = false, isStartTimeSelected = false;
    private boolean isEndDateSelected = false, isEndTimeSelected = false;

    // Below variables will store the exact start and end date\time selected for scheduling
    private int _start_year, _start_monthOfYear, _start_dayOfMonth, _start_hours, _start_minute;
    private int _end_year, _end_monthOfYear, _end_dayOfMonth, _end_hours, _end_minute;

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        //return true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.back_only_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.action_help:
            // Start the 'loginHelpFunction' and send the activity context
            Functions.helpFunction(getApplicationContext());
            return(true);
    }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time_picker);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // Set animation when activity starts/ends

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Set UI Handler to send actions to UI
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SHOW_UPDATE_SERVER_DIALOG:
                        pDialog.setMessage("Setting up Job ...");
                        Functions.showDialog(pDialog);
                        break;
                    case HIDE_DIALOG:
                        Functions.hideDialog(pDialog);
                        break;
                }
            }
        };

        // Get current time, and set it to each of the date \ time vars
        // These variables will be user later to remember calendar pickers
        Calendar currentTime = Calendar.getInstance();

        _start_year = currentTime.get(Calendar.YEAR);
        _start_monthOfYear = currentTime.get(Calendar.MONTH);
        _start_dayOfMonth = currentTime.get(Calendar.DAY_OF_MONTH);
        _start_hours = currentTime.get(Calendar.HOUR_OF_DAY);
        _start_minute = currentTime.get(Calendar.MINUTE);

        _end_year = currentTime.get(Calendar.YEAR);
        _end_monthOfYear = currentTime.get(Calendar.MONTH);
        _end_dayOfMonth = currentTime.get(Calendar.DAY_OF_MONTH);
        _end_hours = currentTime.get(Calendar.HOUR_OF_DAY);
        _end_minute = currentTime.get(Calendar.MINUTE);

        // Day of week choose part
        // Retrieve the layout which holds the 'repeat'
        // By default its invisible
        final LinearLayout repeatLayout = (LinearLayout) findViewById(R.id.day_choose_layout);
        isRepeatJobSelected = new AtomicBoolean(false);
        // Initialize the radio group for the repeat selection
        final RadioGroup repeatRadioGroup = (RadioGroup) findViewById(R.id.repeated_radio_group);

        // What happens when the radio buttons are clicked
        repeatRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = repeatRadioGroup.findViewById(checkedId);
                int index = repeatRadioGroup.indexOfChild(radioButton);

                switch (index) {
                    case 0: // 'Once' button selected
                        repeatLayout.setVisibility(View.GONE);// Hide the layout
                        isRepeatJobSelected.set(false);
                        break;
                    case 1: // 'Repeat' button selected
                        repeatLayout.setVisibility(View.VISIBLE);// Show the layout
                        isRepeatJobSelected.set(true);
                        break;
                }
            }
        });

        // Start date selection part
        isStartJobActionSelected = new AtomicBoolean(true);
        // Initialize the radio group for the On \ Off selection
        final RadioGroup startFunctionRadioGroup = (RadioGroup) findViewById(R.id.start_function_radio_group);
        final TextView endFunctionTextView = (TextView) findViewById(R.id.end_turn_on_off_text_set);

        // What happens when the 'start' radio buttons are clicked
        startFunctionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = startFunctionRadioGroup.findViewById(checkedId);
                int index = startFunctionRadioGroup.indexOfChild(radioButton);

                switch (index) {
                    case 0: // 'On' button selected
                        // Set that when time comes device will
                        isStartJobActionSelected.set(true);
                        endFunctionTextView.setText(R.string.off);
                        endFunctionTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                        break;
                    case 1: // 'Off' button selected
                        isStartJobActionSelected.set(false);
                        endFunctionTextView.setText(R.string.on);
                        endFunctionTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                        break;
                }
            }
        });

        // Initialize all 'day of week' checkboxes
        setupActivitiesCheckbox();

        // Declare date time areas
        setupActivitiesDateSelectors();

        Button confirmButton = (Button) findViewById(R.id.confirmButton);

        // End date selection part
        // Retrieve the layout which holds the 'end date / time'
        // By default its invisible
        final LinearLayout endDateLayout = (LinearLayout) findViewById(R.id.end_date_layout);

        // Declare the check box that will make the above layout visible
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox_end_date);
        // Set this checkbox check listener
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If the checkbox is checked
                if(isEndDateLayoutVisible.get()) {
                    endDateLayout.setVisibility(View.GONE);// Hide the layout
                    isEndDateLayoutVisible.set(false); // Set flag to false (invisible)
                } else {
                    endDateLayout.setVisibility(View.VISIBLE);// Show the layout
                    isEndDateLayoutVisible.set(true); // Set flag to false (visible)
                }
            }
        });

        // Set 'Confirm' button on click action
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check for valid login when pressing confirm
                if(performConfirmButtonCheck())
                    sendScheduleToServer(); // If all went OK, sent a new scheduled job to server
            }
        });
    }


    /**
     * Perform validation once 'confirmButton' is clicked
     * Check serial of validations (details inside function)
     *
     * @return  True - All validation checks pass OK
     *          False - NON of the validation check passed
     */
    private boolean performConfirmButtonCheck() {
        // First check if 'Repeat' is checked
        if(isRepeatJobSelected.get()) {
            boolean isCheckboxSelected = false;
            // If 'Repeat' is check, at least one day must be checked
            for (int i=0; i < 7; i++) {
                if(daysOfWeek[i].get()){
                    isCheckboxSelected = true;
                }
            }
            // If no checkbox was selected, then no repeat action can be made
            if(!isCheckboxSelected) {
                Toast.makeText(getApplicationContext(), "At least one day must be selected", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Next check if user selected start date and start time
        if(!isStartDateSelected || !isStartTimeSelected) {
            Toast.makeText(getApplicationContext(), "Start point must be selected", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Get current date
        Date trialTime = new Date();

        startDate = new GregorianCalendar(_start_year, _start_monthOfYear, _start_dayOfMonth, _start_hours, _start_minute).getTime();

        // If current date is after start date, the schedule is in the future and its wrong
        if(trialTime.after(startDate)) {
            Toast.makeText(getApplicationContext(), "Time travel is not yet possible" +
                    ", Date must be in the future", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(isEndDateLayoutVisible.get()) {
            // Next check if user selected start date and start time
            if(!isEndDateSelected || !isEndTimeSelected) {
                Toast.makeText(getApplicationContext(), "End point must be selected", Toast.LENGTH_SHORT).show();
                return false;
            }
            endDate = new GregorianCalendar(_end_year, _end_monthOfYear, _end_dayOfMonth, _end_hours, _end_minute).getTime();

            if (startDate.after(endDate)) {
                Toast.makeText(getApplicationContext(), "Start date cannot be after end date", Toast.LENGTH_SHORT).show();
                return false;
            }
            Toast.makeText(getApplicationContext(), endDate.toString(), Toast.LENGTH_SHORT).show();
        }

        // If function did not returned false so far, it must be true
        return true;
    }


    /**
     * Define and initialize the activities check boxes
     * All activities checkboxes are set below
     */
    private void setupActivitiesCheckbox() {
        Log.d(TAG, "Running 'setupActivitiesCheckbox' function");
        // { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" }
        for (int i=0; i < daysOfWeek.length; i++) {
            daysOfWeek[i] = new AtomicBoolean(false);
        }

        // Get all days check boxes
        CheckBox chkSunday = (CheckBox) findViewById(R.id.chk_sunday);
        CheckBox chkMonday = (CheckBox) findViewById(R.id.chk_monday);
        CheckBox chkTuesday = (CheckBox) findViewById(R.id.chk_tuesday);
        CheckBox chkWednesday = (CheckBox) findViewById(R.id.chk_wednesday);
        CheckBox chkThursday = (CheckBox) findViewById(R.id.chk_thursday);
        CheckBox chkFriday = (CheckBox) findViewById(R.id.chk_friday);
        CheckBox chkSaturday = (CheckBox) findViewById(R.id.chk_saturday);

        chkSunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for current checkbox status
                if (((CheckBox) v).isChecked())
                    daysOfWeek[0].set(true);
                else
                    daysOfWeek[0].set(false);
            }
        });

        chkMonday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for current checkbox status
                if (((CheckBox) v).isChecked())
                    daysOfWeek[1].set(true);
                else
                    daysOfWeek[1].set(false);
            }
        });

        chkTuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for current checkbox status
                if (((CheckBox) v).isChecked())
                    daysOfWeek[2].set(true);
                else
                    daysOfWeek[2].set(false);
            }
        });

        chkWednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for current checkbox status
                if (((CheckBox) v).isChecked())
                    daysOfWeek[3].set(true);
                else
                    daysOfWeek[3].set(false);
            }
        });

        chkThursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for current checkbox status
                if (((CheckBox) v).isChecked())
                    daysOfWeek[4].set(true);
                else
                    daysOfWeek[4].set(false);
            }
        });

        chkFriday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for current checkbox status
                if (((CheckBox) v).isChecked())
                    daysOfWeek[5].set(true);
                else
                    daysOfWeek[5].set(false);
            }
        });

        chkSaturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for current checkbox status
                if (((CheckBox) v).isChecked())
                    daysOfWeek[6].set(true);
                else
                    daysOfWeek[6].set(false);
            }
        });
    }


    /**
     * Set the four activities data selector,
     * there are four data selector, two for start data and time,
     * and two for end date and time
     */
    private void setupActivitiesDateSelectors() {
        Log.d(TAG, "Running 'setupActivitiesDateSelectors' function");
        final EditText startDateText = (EditText) findViewById(R.id.startDate);
        final EditText startTimeText = (EditText) findViewById(R.id.startTime);
        final EditText endDateText = (EditText) findViewById(R.id.endDate);
        final EditText endTimeText = (EditText) findViewById(R.id.endTime);

        // perform click event on 'startDateWrapper'
        startDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // date picker dialog
                datePickerDialog = new DatePickerDialog(DateTimePickerActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Log.d(TAG, "startDate selected");
                                // set day of month , month and year value in the edit text
                                _start_year = year;
                                _start_monthOfYear = monthOfYear;
                                _start_dayOfMonth = dayOfMonth;
                                isStartDateSelected = true;
                                startDateText.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);
                            }
                        }, _start_year, _start_monthOfYear, _start_dayOfMonth); // Set last selected params to display on the picker
                datePickerDialog.show();
            }
        });

        startTimeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TimePickerDialog timePicker = new TimePickerDialog(DateTimePickerActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        Log.d(TAG, "startTime selected");
                        _start_hours = selectedHour;
                        _start_minute = selectedMinute;
                        isStartTimeSelected = true;
                        startTimeText.setText(selectedHour + ":" + selectedMinute);
                    }
                }, _start_hours, _start_minute, true); // Set last selected params to display on the picker, True for 24 time set
                timePicker.show();

            }
        });

        // perform click event on 'startDateWrapper'
        endDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // date picker dialog
                datePickerDialog = new DatePickerDialog(DateTimePickerActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Log.d(TAG, "endDate selected");
                                // set day of month , month and year value in the edit text
                                _end_year = year;
                                _end_monthOfYear = monthOfYear;
                                _end_dayOfMonth = dayOfMonth;
                                isEndDateSelected = true;
                                endDateText.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                        }, _end_year, _end_monthOfYear, _end_dayOfMonth); // Set last selected params to display on the picker
                datePickerDialog.show();
            }
        });


        endTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePicker = new TimePickerDialog(DateTimePickerActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        Log.d(TAG, "endTime selected");
                        _end_hours = selectedHour;
                        _end_minute = selectedMinute;
                        isEndTimeSelected = true;
                        endTimeText.setText(selectedHour + ":" + selectedMinute);
                    }
                }, _end_hours, _end_minute, true); // Set last selected params to display on the picker, True for 24 time set
                timePicker.show();
            }
        });
    }


    /**
     * Register scheduled job to server
     */
    private void sendScheduleToServer(){
        Log.d(TAG, "Running 'sendScheduleToServer' function");
        String tag_registration_req = "req_scheduled_job";

        Message showDialog =
                uiHandler.obtainMessage(SHOW_UPDATE_SERVER_DIALOG, pDialog);
        showDialog.sendToTarget();

        final Message hideDialog =
                uiHandler.obtainMessage(HIDE_DIALOG, pDialog);

        sharedPreferences = new SharedPreferences(getApplicationContext());

        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                AppConfig.URL_SET_SCHEDULED_JOB, buildJsonArray(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Scheduled job Response: " + response.toString());
                hideDialog.sendToTarget();

                try {
                    String status = response.getString("status");
                    // Check if the token contains any values
                    if (status.equals("success")) {
                        Toast.makeText(getApplicationContext(), "Scheduled job successfully set", Toast.LENGTH_LONG).show();

                        // Finish the activity
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = response.getString("message");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    Log.e(TAG, e.toString());
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
                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                    Log.e(TAG, "Server response code: " + statusCode);
                    Toast.makeText(getApplicationContext(), statusCode, Toast.LENGTH_LONG).show();
                    //get response body and parse with appropriate encoding
                    if (error.networkResponse.data != null) {
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            Log.e(TAG, "Scheduled job Error: " + body);
                            Toast.makeText(getApplicationContext(), body, Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
                hideDialog.sendToTarget();
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
        Log.d(TAG, "Sending: " + strReq);
        NetworkRequests.getInstance().addToRequestQueue(strReq, tag_registration_req, true);
    }


    /**
     * Since we use a large amount of of data to send to server,
     * The function will take all date that's been collected (and stored in this class)
     * And build a single JSON object from it
     *
     * @return JSONObject - A JSON object containing user and device info and this class information
     */
    private JSONObject buildJsonArray() {
        Log.d(TAG, "Running 'buildJsonArray' function");

        JSONObject scheduledJobJsonObject = new JSONObject();

        Bundle extras = getIntent().getExtras();
        try {
            // Build json object with main info of the current device
            JSONObject jsonObjectAppParams = new JSONObject();
            jsonObjectAppParams.put("device_id", extras.getString("DEVICE_ID"));
            jsonObjectAppParams.put("topic", extras.getString("TOPIC"));

            // Build json object that represents a 'repeated job'
            JSONObject jsonObjectRepeatJob = new JSONObject();
            JSONObject jsonObjectRepeatJobDays = new JSONObject();
            for (int i = 0; i < 7; i++) {
                // dayOfWeekArray.get(i) returns the day of week as string
                // daysOfWeek[i] returns true or false for that specific day
                jsonObjectRepeatJobDays.put(dayOfWeekArray.get(i), daysOfWeek[i].get()); // Boolean var
            }

            // Now daysOfWeekJsonMap hold boolean values for each day
            // Put daysOfWeekJsonMap to an Json object
            jsonObjectRepeatJob.put("repeat_job", isRepeatJobSelected.get()); // Boolean var
            jsonObjectRepeatJob.put("repeat_days", jsonObjectRepeatJobDays);

            // Build json object that holds start date
            JSONObject jsonObjectStartDateTimeValues = new JSONObject();

            jsonObjectStartDateTimeValues.put("start_date_time_selected", true); // Always true
            jsonObjectStartDateTimeValues.put("start_date_time_values",
                    buildDateObject(_start_year,_start_monthOfYear,_start_dayOfMonth,
                            _start_hours,_start_minute));

            // Build json object that holds end date
            JSONObject jsonObjectEndDateTimeValues = new JSONObject();

            jsonObjectEndDateTimeValues.put("end_date_time_selected", isEndDateLayoutVisible.get());
            // Build a 'Date' object with relevant end vars
            jsonObjectEndDateTimeValues.put("end_date_time_values",
                    buildDateObject(_end_year,_end_monthOfYear,_end_dayOfMonth,
                            _end_hours,_end_minute));

            // Build final jsonObject from all above
            scheduledJobJsonObject.put("app_params", jsonObjectAppParams);
            scheduledJobJsonObject.put("repeated_job", jsonObjectRepeatJob);
            scheduledJobJsonObject.put("job_action", isStartJobActionSelected.get());
            scheduledJobJsonObject.put("start_date_time", jsonObjectStartDateTimeValues);
            scheduledJobJsonObject.put("end_date_time", jsonObjectEndDateTimeValues);

            /*
            Final Json object should look like this:
                {
                   "app_params": {
                      "device_id": "",
                      "topic": ""
                   },
                   "repeated_job": {
                      "repeat_job": true,
                      "repeat_days": {
                         "Sunday": false,
                         "Monday": true,
                         "Tuesday": false,
                         "Wednesday": false,
                         "Thursday": true,
                         "Friday": false,
                         "Saturday": false
                      }
                   },
                   "job_action": true,
                   "start_date_time": {
                      "start_date_time_selected": true,
                      "start_date_time_values": "2017-05-31 17:31:00+0300"
                   },
                   "end_date_time": {
                      "end_date_time_selected": false,
                      "end_date_time_values": "2017-05-21 17:31:00+0300"
                   }
                }*/
        }
        catch (JSONException e) {
            Log.e(TAG, "JSONException error: " + e);
        }

        Log.d(TAG, "Json Object generated is: " + scheduledJobJsonObject.toString());
        return scheduledJobJsonObject;
    }


    /**
     * Function will build a 'SimpleDateFormat' by taking integers as input and put put date object
     *
     * @param year input year as integer
     * @param monthOfYear input month of the year as integer
     * @param dayOfMonth input day of the month as integer
     * @param hour input hour as integer
     * @param minute input minute as integer
     *
     * @return  String - Returns a string date like '2017-05-31 06:29:00+0300'
     *          Note - UTC is taken from phone local time setting
     */
    private String buildDateObject(int year, int monthOfYear, int dayOfMonth, int hour, int minute) {
        Log.d(TAG, "Running 'buildDateObject' function");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // This simple date will look like '2017-05-31 06:29:00+0300'
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.getDefault());
        // Return the date as string
        Log.d(TAG, "Date/Time generated: " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }

}