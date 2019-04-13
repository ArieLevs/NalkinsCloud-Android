
package cloud.nalkins.nalkinscloud.deviceLayouts;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.StringTokenizer;

import cloud.nalkins.nalkinscloud.MainActivity;
import cloud.nalkins.nalkinscloud.MqttClient;
import cloud.nalkins.nalkinscloud.R;
import cloud.nalkins.nalkinscloud.SharedPreferences;


/**
 * Created by Arie on 5/9/2017.
 *
 */

public class DynamicLayoutDistilleryTemperatureSet extends AppCompatActivity {

    private final String TAG = MqttClient.class.getSimpleName();

    private final int UPDATE_DEVICE_UI = 3;

    SharedPreferences sharedPreferences;

    // Declare variables for spinners
    Spinner spinnerMainHeaterSSR;
    Spinner spinnerWaterCoolerSSR1;
    Spinner spinnerAirCoolerSSR2;
    Spinner spinnerToBarrelDisposalSSR;

    // Declare the array spinner that will hold temperature values
    // 1 to 100
    String[] arraySpinner;
    ArrayAdapter<String> adapter;

    static int[] tempArray = new int[4];
    /*
    SparseArray<String> temperatureValuesArray = new SparseArray<String>() {
        {
            put(0, "94");
            put(1, "60");
            put(2, "80");
            put(3, "80");
        }
    };*/


    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        //return true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.back_only_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.distillery_temperature_layout);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out); // Set animation when activity starts/ends

        // Create new sharedPreferences manager object
        sharedPreferences = new SharedPreferences(getApplicationContext());

        // Get custom temp values from shared preferences
        StringTokenizer st = new StringTokenizer(sharedPreferences.getCustomTempValues(), ",");
        for (int i = 0; i < tempArray.length; i++) {
            tempArray[i] = Integer.parseInt(st.nextToken());
        }

        setupSpinners();

        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        Button defaultButton = (Button) findViewById(R.id.defaultButton);

        // Set 'Confirm' button on click action
        // Once button is clicked all selected values from spinners will get saved
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send message to UI at MainActivity
                Bundle extras = getIntent().getExtras();
                Message msg = MainActivity.uiHandler.obtainMessage();
                msg.what = UPDATE_DEVICE_UI;
                msg.obj = "{\"topic\":\"" + extras.getString("DEVICE_ID") + "/distillery/update_temp_settings" + "\"," +
                        "\"message\": \"" + 1 + "\"}";
                MainActivity.uiHandler.sendMessage(msg);

                sharedPreferences.setIsCustomTempConfigured(true);
                // Save temperature configured values
                sharedPreferences.setCustomTempValues(tempArray);
                finish();
            }
        });

        // Set 'default' button on click action
        // Once button clicked, default values will be copied to 'temperatureValuesArray'
        defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // { "main heater", "disposal ssr1", "disposal ssr2", "secondary ssr", "cooler ssr" }
                int[] originalArrayValues = {94, 60, 80, 80};

                // Send message to UI at MainActivity
                Bundle extras = getIntent().getExtras();
                Message msg = MainActivity.uiHandler.obtainMessage();
                msg.what = UPDATE_DEVICE_UI;
                msg.obj = "{\"topic\":\"" + extras.getString("DEVICE_ID") + "/distillery/update_temp_settings" + "\"," +
                        "\"message\": \"" + 0 + "\"}";
                MainActivity.uiHandler.sendMessage(msg);

                sharedPreferences.setIsCustomTempConfigured(false);
                // Save temperature configured values
                sharedPreferences.setCustomTempValues(originalArrayValues);
                finish();
            }
        });
    }


    private void setupSpinners() {
        Log.d(TAG, "Running 'setupSpinners' function");
        // Declare Spinner value array (degrees from 50 to 100 C)
        arraySpinner = new String[101];
        for(int i=0; i <= 100; i++) {
            this.arraySpinner[i] = String.valueOf(i);
        }

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Declare Main Heater spinner array
        spinnerMainHeaterSSR = (Spinner) findViewById(R.id.spinner_main_heater);
        spinnerMainHeaterSSR.setAdapter(adapter);
        spinnerMainHeaterSSR.setSelection(tempArray[0]); // This is default value of '84'
        spinnerMainHeaterSSR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                try {
                    // Set value to relevant array cell
                    tempArray[0] = pos;
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Declare first disposal spinner array
        spinnerWaterCoolerSSR1 = (Spinner) findViewById(R.id.spinner_water_cooler);
        spinnerWaterCoolerSSR1.setAdapter(adapter);
        spinnerWaterCoolerSSR1.setSelection(tempArray[1]); // This is default value of '84'
        spinnerWaterCoolerSSR1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Try parsing String to Int
                try {
                    // Set value to relevant array cell, from 'pos' (position of spinner)
                    tempArray[1] = pos;
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Declare secondary spinner array
        spinnerAirCoolerSSR2 = (Spinner) findViewById(R.id.spinner_air_cooler_ssr);
        spinnerAirCoolerSSR2.setAdapter(adapter);
        spinnerAirCoolerSSR2.setSelection(tempArray[2]); // This is default value of '84'
        spinnerAirCoolerSSR2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Try parsing String to Int
                try {
                    // Set value to relevant array cell
                    tempArray[2] = pos;
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Declare To Barrel spinner array
        spinnerToBarrelDisposalSSR = (Spinner) findViewById(R.id.spinner_to_barrel_ssr);
        spinnerToBarrelDisposalSSR.setAdapter(adapter);
        spinnerToBarrelDisposalSSR.setSelection(tempArray[3]); // This is default value of '84'
        spinnerToBarrelDisposalSSR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // Try parsing String to Int
                try {
                    // Set value to relevant array cell
                    //temperatureValuesArray[4] = parent.getItemAtPosition(pos).toString();
                    tempArray[3] = pos;
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
