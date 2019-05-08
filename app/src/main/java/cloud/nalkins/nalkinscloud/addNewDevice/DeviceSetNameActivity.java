package cloud.nalkins.nalkinscloud.addNewDevice;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cloud.nalkins.nalkinscloud.Functions;
import cloud.nalkins.nalkinscloud.MainActivity;
import cloud.nalkins.nalkinscloud.R;

/**
 * Created by Arie on 4/9/2017.
 */

public class DeviceSetNameActivity extends AppCompatActivity {
    private static String deviceName;

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
        setContentView(R.layout.activity_add_device_set_name);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        //Set buttons
        Button btn_cancel = findViewById(R.id.cancelButton);
        Button btn_continue = findViewById(R.id.confirmButton);

        // Set device name text
        TextView deviceType = findViewById(R.id.device_type_value);
        deviceType.setText(DeviceAddNewActivity.getDeviceType());

        // Configure the device name text field
        final TextInputLayout deviceNameWrapper = findViewById(R.id.device_nameWrapper);
        //Set error hints when validation fail
        deviceNameWrapper.setHint("Set Device Name");
        // On first run set false hint to 'don't show'
        deviceNameWrapper.setErrorEnabled(false);

        // Cancel button function
        btn_cancel.setOnClickListener((View v) -> {
            // Launch main activity
            Intent intent = new Intent(DeviceSetNameActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Close all previous activities
            startActivity(intent);
            finish();
        });

        // Continue button function
        btn_continue.setOnClickListener((View v) -> {
            // Get text from device name field
            if (deviceNameWrapper.getEditText() != null)
                deviceName = deviceNameWrapper.getEditText().getText().toString();

            // Check if input device name is alpha-numeric
            if (!deviceName.isEmpty() && Functions.isAlphanumeric(deviceName)) { // Check if username valid
                // If the name is valid, then check for max length
                if (deviceName.length() <= 32) {
                    deviceNameWrapper.setErrorEnabled(false);

                    // Launch get Wifi credentials activity
                    Intent intent = new Intent(DeviceSetNameActivity.this,
                            GetWifiCredentialsActivity.class);
                    startActivity(intent);

                } else {
                    deviceNameWrapper.setError("Device name max length is 32 characters long");
                }
            } else {
                deviceNameWrapper.setError("Device name most be alpha-numeric");
            }
        });
    }

    // Function will return the private string 'deviceName'
    public static String getDeviceName() {
        return deviceName;
    }
}
