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
public class DynamicLayoutDistillery {

    private Context context;

    private boolean _isDeviceOnline;

    private String deviceId;
    private String deviceType;
    private String deviceName;

    private TextView deviceStatus;
    private TextView deviceNameText;

    private LinearLayout deviceOptionsLayout;

    private ImageView deviceOptionsIcon;
    private ImageView deviceRemoveIcon;

    //Automation scheduler part
    public ImageView startScheduler;
    public ImageView statusScheduler;
    public ImageView startTemperatureConf;
    public ImageView statusTemperatureConf;

    private ToggleButton toggle;

    public ImageView tempIcon;
    private TextView temp_status_text_view;
    private TextView temperature;

    private View v;

    // DynamicLayoutDistillery constructor
    public DynamicLayoutDistillery(Context context, String deviceId, String deviceType, String deviceName) {

        this.context = context; // Layout context

        this._isDeviceOnline = false;

        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.deviceName = deviceName;

        LayoutInflater inflater = LayoutInflater.from(context);
        this.v = inflater.inflate(R.layout.distillery_layout, null);

        this.deviceOptionsLayout = v.findViewById(R.id.settingsLayout);

        this.deviceNameText = v.findViewById(R.id.device_name_text_view);
        deviceNameText.setText(deviceName);

        this.deviceStatus = v.findViewById(R.id.device_status); // Set 'online' \ 'offline' status

        // Initialize start scheduler button
        this.startScheduler = v.findViewById(R.id.scheduler_start_icon);
        // Initialize set scheduler status icon
        this.statusScheduler = v.findViewById(R.id.scheduler_set_icon);

        // Initialize start startTemperatureConf button
        this.startTemperatureConf = v.findViewById(R.id.temp_start_icon);
        // Initialize start StatusTemperatureConf button
        this.statusTemperatureConf = v.findViewById(R.id.temp_set_icon);

        this.deviceOptionsIcon = v.findViewById(R.id.device_options); // Define and set the layouts side icon
        this.deviceRemoveIcon = v.findViewById(R.id.device_remove_icon); // Define and set the layouts side icon

        // Initialize the layouts main toggle button
        this.toggle = v.findViewById(R.id.distillery_toggle_button);

        this.tempIcon = v.findViewById(R.id.temp_icon);
        this.temp_status_text_view = v.findViewById(R.id.temp_status_text_view);
        this.temperature = v.findViewById(R.id.device_temp_var);
    }

    public void setStatusValue(String value) {
        this.deviceStatus.setText(value);
        if (value.equals("online"))
            _isDeviceOnline = true;
        else if (value.equals("offline"))
            _isDeviceOnline = false;
    }

    public ImageView getOptionsIcon() {
        return deviceOptionsIcon;
    }

    public ImageView getRemoveIcon() {
        return deviceRemoveIcon;
    }

    public LinearLayout getDeviceOptionsLayout() {
        return this.deviceOptionsLayout;
    }

    public boolean isDeviceOnline() {
        return this._isDeviceOnline;
    }

    public void setStatusTextColor(int value) {
        this.deviceStatus.setTextColor(ContextCompat.getColor(context, value));
    }

    // Function sets icon for the layout
    public void setTemperatureConfStatusIcon(int iconId) {
        this.statusTemperatureConf.setImageResource(iconId);
    }

    public void setTemperatureIcon(int iconId) {
        this.tempIcon.setImageResource(iconId);
    }

    // Function return the View object of relevant layout
    public View getView() {
        return v;
    }

    public void setDistilleryToggleButton(boolean checked) {
        this.toggle.setChecked(checked);
    }

    public ToggleButton getDistilleryToggleButton() {
        return this.toggle;
    }


    // Function sets a value for the device name
    public void setDeviceNameText(String value) {
        this.deviceNameText.setText(value);
    }

    public TextView getDeviceNameText() {
        return deviceNameText;
    }


    public void setDeviceTemperatureStatusText(String value) {
        this.temp_status_text_view.setText(value);
    }

    public void setDeviceTemperatureText(String value) {
        this.temperature.setText(value);
    }
}