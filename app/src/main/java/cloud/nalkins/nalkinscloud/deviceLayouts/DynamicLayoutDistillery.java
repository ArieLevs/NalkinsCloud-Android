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
    public ImageView statusTemperatureConf ;

    private ToggleButton toggle;

    public ImageView tempIcon;
    private TextView temp_status_text_view;
    private TextView temperature;

    public ImageView switchIcon1;
    private TextView device_switch_status_var_1;

    public ImageView switchIcon2;
    private TextView device_switch_status_var_2;

    public ImageView switchIcon3;
    private TextView device_switch_status_var_3;

    public ImageView switchIcon4;
    private TextView device_switch_status_var_4;

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

        this.deviceOptionsLayout = (LinearLayout) v.findViewById(R.id.settingsLayout);

        this.deviceNameText =(TextView)v.findViewById(R.id.device_name_text_view);
        deviceNameText.setText(deviceName);

        this.deviceStatus = (TextView)v.findViewById(R.id.device_status); // Set 'online' \ 'offline' status

        // Initialize start scheduler button
        this.startScheduler = (ImageView) v.findViewById(R.id.scheduler_start_icon);
        // Initialize set scheduler status icon
        this.statusScheduler = (ImageView) v.findViewById(R.id.scheduler_set_icon);

        // Initialize start startTemperatureConf button
        this.startTemperatureConf = (ImageView) v.findViewById(R.id.temp_start_icon);
        // Initialize start StatusTemperatureConf button
        this.statusTemperatureConf = (ImageView) v.findViewById(R.id.temp_set_icon);

        this.deviceOptionsIcon = (ImageView) v.findViewById(R.id.device_options); // Define and set the layouts side icon

        this.deviceRemoveIcon = (ImageView) v.findViewById(R.id.device_remove_icon); // Define and set the layouts side icon

        // Initialize the layouts main toggle button
        this.toggle = (ToggleButton) v.findViewById(R.id.distillery_toggle_button);


        this.tempIcon = (ImageView) v.findViewById(R.id.temp_icon);
        this.temp_status_text_view =(TextView)v.findViewById(R.id.temp_status_text_view);
        this.temperature =(TextView)v.findViewById(R.id.device_temp_var);

        this.switchIcon1 = (ImageView) v.findViewById(R.id.device_icon_1);
        this.device_switch_status_var_1 =(TextView)v.findViewById(R.id.device_switch_status_var_1);

        this.switchIcon2 = (ImageView) v.findViewById(R.id.device_icon_2);
        this.device_switch_status_var_2 =(TextView)v.findViewById(R.id.device_switch_status_var_2);

        this.switchIcon3 = (ImageView) v.findViewById(R.id.device_icon_3);
        this.device_switch_status_var_3 =(TextView)v.findViewById(R.id.device_switch_status_var_3);

        this.switchIcon4 = (ImageView) v.findViewById(R.id.device_icon_4);
        this.device_switch_status_var_4 =(TextView)v.findViewById(R.id.device_switch_status_var_4);
    }

    public void setStatusValue(String value) {
        this.deviceStatus.setText(value);
        if(value.equals("online"))
            _isDeviceOnline = true;
        else if(value.equals("offline"))
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
    public View getView(){
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


    public void setDeviceSsrNo1Uptime(String value) {
        this.device_switch_status_var_1.setText(value);
    }
    public void setDeviceSsrStatusNo1(int iconId) {
        this.switchIcon1.setImageResource(iconId);
    }



    public void setDeviceSsrNo2Uptime(String value) {
        this.device_switch_status_var_2.setText(value);
    }
    public void setDeviceSsrStatusNo2(int iconId) {
        this.switchIcon2.setImageResource(iconId);
    }



    public void setDeviceSsrNo3Uptime(String value) {
        this.device_switch_status_var_3.setText(value);
    }
    public void setDeviceSsrStatusNo3(int iconId) {
        this.switchIcon3.setImageResource(iconId);
    }



    public void setDeviceSsrNo4Uptime(String value) {
        this.device_switch_status_var_4.setText(value);
    }
    public void setDeviceSsrStatusNo4(int iconId) {
        this.switchIcon4.setImageResource(iconId);
    }
}
