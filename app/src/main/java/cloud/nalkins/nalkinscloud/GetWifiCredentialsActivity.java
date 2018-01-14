package cloud.nalkins.nalkinscloud;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Arie on 3/25/2017.
 *
 * GetWifiCredentialsActivity should retrieve users local wifi LAN credentials
 */
public class GetWifiCredentialsActivity extends AppCompatActivity {

    private static final String TAG = GetWifiCredentialsActivity.class.getSimpleName();

    ListView listView ;

    private static String selectedSSID;
    private static String selectedPassword;

    // Create the ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.back_only_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "GetWifiCredentialsActivity Activity Started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_get_wifi_credentials);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        // Configure the wifi password text field
        final TextInputLayout wifiPassWrapper = (TextInputLayout) findViewById(R.id.device_nameWrapper);
        //Set error hints when validation fail
        wifiPassWrapper.setHint("Set wifi Password");

        //Set next button
        Button nextButton = (Button) findViewById(R.id.wifiNextButton);

        // Login button function
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedSSID == null) {
                    Toast.makeText(getApplicationContext(), "SSID must be selected", Toast.LENGTH_LONG).show();
                } else {
                    // Save the password
                    selectedPassword = wifiPassWrapper.getEditText().getText().toString();

                    // First assume password was entered, so set error wrapper to false
                    wifiPassWrapper.setErrorEnabled(false);
                    // If the input wifi password is empty then
                    if(selectedPassword.isEmpty()) {
                        // Print wifi password errors
                        wifiPassWrapper.setError("Password cannot be empty");
                        Toast.makeText(getApplicationContext(), "Wifi password cannot be empty," +
                                " The system only supports WPA/WPA2 security protocols", Toast.LENGTH_LONG).show();
                    } else { // If SSID selected, and password was inputted then
                        if(Functions.isValidPassword(selectedPassword)) { // Check for password validation
                            // Launch device configuration activity
                            Intent intent = new Intent(GetWifiCredentialsActivity.this,
                                    DeviceConfigurationHandler.class);
                            startActivity(intent);
                        }
                        else { // Password is invalid
                            wifiPassWrapper.setError("Invalid password");
                            Toast.makeText(getApplicationContext(), "Invalid password," +
                                    " The system only supports WPA/WPA2 security protocols", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
        // And apply the scanned list to the activity
        initiateScannedList();

    }

    /**
     * Initiate wifi networks scan,
     *
     * Function uses 'HandleWifiConnection' class
     * @see HandleWifiConnection
     */
    public void initiateScannedList() {
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.wifiScanResultList);

        // Declare new array list
        ArrayList<String> ssidList = new ArrayList<>();

        // get scannedNetworks size
        final int N = HandleWifiConnection.scannedNetworks.size();
        // Run on every network found, and put SSID to ssidList
        for(int i=0; i < N; ++i) {
            ssidList.add(HandleWifiConnection.scannedNetworks.get(i).SSID);
        }

        /* Use Adapter to provide the data to the ListView
         *
         * Parameters:
         * simple_list_item_1: Android internal layout view
         * android.R.id.text1: In Android internal layout view already defined text fields to show data
         * ssidList:  scanned networks list array.
         */
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, ssidList);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // ListView Clicked item value
                selectedSSID = (String) listView.getItemAtPosition(position);

                // High light the selected SSID
                listView.setSelector(R.color.light_blue);
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            }
        });
    }

    public static String getSelectedSSID() {
        return selectedSSID;
    }

    public static String getSelectedPassword() {
        return selectedPassword;
    }
}


