package cloud.nalkins.nalkinscloud.deviceLayouts;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import cloud.nalkins.nalkinscloud.R;

/**
 * Created by Arie on 4/25/2017.
 *
 */

public class DynamicLayoutSwitch {

    private Context context;

    private boolean _isDeviceOnline;

    private LinearLayout deviceOptionsLayout;

    private String deviceId;
    private String deviceType;
    private String deviceName;

    private ImageView deviceOptionsIcon;
    private ImageView deviceRemoveIcon;
    private ToggleButton toggle;
    private TextView deviceStatus;
    private TextView deviceNameText;
    private TextView device_switch_current_status;
    private TextView device_consumption_var;
    private View v;

    // DynamicLayoutTemperature constructor
    public DynamicLayoutSwitch(Context context, String deviceId, String deviceType, String deviceName) {

        this.context = context; // Layout context

        this._isDeviceOnline = false;

        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.deviceName = deviceName;

        LayoutInflater inflater = LayoutInflater.from(context);
        this.v = inflater.inflate(R.layout.switch_device, null); // Inflate the 'temperature_device' layout

        this.deviceOptionsLayout = (LinearLayout) v.findViewById(R.id.settingsLayout);

        this.deviceOptionsIcon = (ImageView) v.findViewById(R.id.device_options); // Define and set the layouts side icon

        this.deviceRemoveIcon = (ImageView) v.findViewById(R.id.device_remove_icon); // Define and set the layouts side icon

        this.deviceStatus = (TextView)v.findViewById(R.id.switch_device_status); // Set 'online' \ 'offline' status

        // Initialize the layouts main toggle button
        this.toggle = (ToggleButton) v.findViewById(R.id.switch_toggle_button);

        this.deviceNameText =(TextView)v.findViewById(R.id.switch_name_text_view); // Set device name text view area
        deviceNameText.setText(deviceName);

        this.device_switch_current_status =(TextView)v.findViewById(R.id.device_switch_status_var); // Set device current state text view area
        this.device_consumption_var =(TextView)v.findViewById(R.id.device_switch_consumption_var); // Set device consumption text view area
    }


    public boolean getIsDeviceOnline() {
        return this._isDeviceOnline;
    }

    public LinearLayout getDeviceOptionsLayout() {
        return this.deviceOptionsLayout;
    }

    public ImageView getOptionsIcon() {
        return deviceOptionsIcon;
    }

    public ImageView getRemoveIcon() {
        return deviceRemoveIcon;
    }

    // Function return the View object of relevant layout
    public View getView(){
        return v;
    }

    // Set online / offline status
    public void setStatusValue(String value) {
        this.deviceStatus.setText(value);
    }

    public void setIsDeviceOnline(boolean value) {
        _isDeviceOnline = value;
    }

    // Set online / offline text color
    public void setStatusTextColor(int value) {
        this.deviceStatus.setTextColor(ContextCompat.getColor(context, value));
    }

    public void setSwitchToggleButton(boolean checked) {
        this.toggle.setChecked(checked);
    }

    public ToggleButton getSwitchToggleButton() {
        return this.toggle;
    }

    public void setDeviceSwitchCurrentStatus(String value) {
        this.device_switch_current_status.setText(value);
    }

    public void setDeviceSwitchCurrentStatusColor(int value) {
        this.device_switch_current_status.setTextColor(ContextCompat.getColor(context, value));
    }

    public TextView getDeviceSwitchStateText() {
        return device_switch_current_status;
    }



    public void setDeviceConsumptionText(String value) {
        this.device_consumption_var.setText(value);
    }
    public TextView getDeviceConsumptionText() {
        return device_consumption_var;
    }
}
