package cloud.nalkins.nalkinscloud;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by Arie on 4/25/2017.
 *
 */
class DynamicLayoutDistillery {

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
    ImageView startScheduler;
    ImageView statusScheduler;
    ImageView startTemperatureConf;
    ImageView statusTemperatureConf ;
    ImageView startManualConf;

    private LinearLayout manualConfHidden;

    private ToggleButton toggle;

    ImageView tempIcon;
    private TextView temp_status_text_view;
    private TextView temperature;

    ImageView switchIcon1;
    private TextView device_status_text_view_1;
    private TextView device_switch_status_var_1;
    private TextView device_consumption_var_1;

    ImageView switchIcon2;
    private TextView device_status_text_view_2;
    private TextView device_switch_status_var_2;
    private TextView device_consumption_var_2;

    ImageView switchIcon3;
    private TextView device_status_text_view_3;
    private TextView device_switch_status_var_3;
    private TextView device_consumption_var_3;

    ImageView switchIcon4;
    private TextView device_status_text_view_4;
    private TextView device_switch_status_var_4;
    private TextView device_consumption_var_4;

    private View v;

    // DynamicLayoutDistillery constructor
    DynamicLayoutDistillery(Context context, String deviceId, String deviceType, String deviceName) {

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


        // Initialize start manual_configuration_start_icon button
        this.startManualConf = (ImageView) v.findViewById(R.id.manual_configuration_start_icon);
        // Initialize the hidden manualConfHidden part
        this.manualConfHidden = (LinearLayout) v.findViewById(R.id.manual_configuration_hidden);

        this.deviceOptionsIcon = (ImageView) v.findViewById(R.id.device_options); // Define and set the layouts side icon

        this.deviceRemoveIcon = (ImageView) v.findViewById(R.id.device_remove_icon); // Define and set the layouts side icon

        // Initialize the layouts main toggle button
        this.toggle = (ToggleButton) v.findViewById(R.id.distillery_toggle_button);


        this.tempIcon = (ImageView) v.findViewById(R.id.temp_icon);
        this.temp_status_text_view =(TextView)v.findViewById(R.id.temp_status_text_view);
        this.temperature =(TextView)v.findViewById(R.id.device_temp_var);

        this.switchIcon1 = (ImageView) v.findViewById(R.id.device_icon_1);
        this.device_status_text_view_1 =(TextView)v.findViewById(R.id.device_status_text_view_1);
        this.device_switch_status_var_1 =(TextView)v.findViewById(R.id.device_switch_status_var_1);
        this.device_consumption_var_1 =(TextView)v.findViewById(R.id.device_consumption_var_1);

        this.switchIcon2 = (ImageView) v.findViewById(R.id.device_icon_2);
        this.device_status_text_view_2 =(TextView)v.findViewById(R.id.device_status_text_view_2);
        this.device_switch_status_var_2 =(TextView)v.findViewById(R.id.device_switch_status_var_2);
        this.device_consumption_var_2 =(TextView)v.findViewById(R.id.device_consumption_var_2);

        this.switchIcon3 = (ImageView) v.findViewById(R.id.device_icon_3);
        this.device_status_text_view_3 =(TextView)v.findViewById(R.id.device_status_text_view_3);
        this.device_switch_status_var_3 =(TextView)v.findViewById(R.id.device_switch_status_var_3);
        this.device_consumption_var_3 =(TextView)v.findViewById(R.id.device_consumption_var_3);

        this.switchIcon4 = (ImageView) v.findViewById(R.id.device_icon_4);
        this.device_status_text_view_4 =(TextView)v.findViewById(R.id.device_status_text_view_4);
        this.device_switch_status_var_4 =(TextView)v.findViewById(R.id.device_switch_status_var_4);
        this.device_consumption_var_4 =(TextView)v.findViewById(R.id.device_consumption_var_4);

    }

    void setStatusValue(String value) {
        this.deviceStatus.setText(value);
        if(value.equals("online"))
            _isDeviceOnline = true;
        else if(value.equals("offline"))
            _isDeviceOnline = false;
    }

    ImageView getOptionsIcon() {
        return deviceOptionsIcon;
    }

    ImageView getRemoveIcon() {
        return deviceRemoveIcon;
    }

    LinearLayout getDeviceOptionsLayout() {
        return this.deviceOptionsLayout;
    }

    boolean isDeviceOnline() {
        return this._isDeviceOnline;
    }

    void setStatusTextColor(int value) {
        this.deviceStatus.setTextColor(ContextCompat.getColor(context, value));
    }


    // Function sets icon for the layout
    void setManualConfStartIcon(int iconId) {
        this.startManualConf.setImageResource(iconId);
    }

    // Function sets icon for the layout
    void setTemperatureConfStatusIcon(int iconId) {
        this.statusTemperatureConf.setImageResource(iconId);
    }

    void setTemperatureIcon(int iconId) {
        this.tempIcon.setImageResource(iconId);
    }

    LinearLayout getHiddenManualConfLayout() {
        return this.manualConfHidden;
    }

    // Function return the View object of relevant layout
    public View getView(){
        return v;
    }

    void setDistilleryToggleButton(boolean checked) {
        this.toggle.setChecked(checked);
    }

    ToggleButton getDistilleryToggleButton() {
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
    void setDeviceTemperatureText(String value) {
        this.temperature.setText(value);
    }


    public void setDeviceSwitchStatus1(String value) {
        this.device_status_text_view_1.setText(value);
    }
    public void setDeviceSwitchState1(String value) {
        this.device_switch_status_var_1.setText(value);
    }
    public void setDeviceSwitchConsumption1(String value) {
        this.device_consumption_var_1.setText(value);
    }



    public void setDeviceSwitchStatus2(String value) {
        this.device_status_text_view_2.setText(value);
    }
    public void setDeviceSwitchState2(String value) {
        this.device_switch_status_var_2.setText(value);
    }
    public void setDeviceSwitchConsumption2(String value) {
        this.device_consumption_var_2.setText(value);
    }


    public void setDeviceSwitchStatus3(String value) {
        this.device_status_text_view_3.setText(value);
    }
    public void setDeviceSwitchState3(String value) {
        this.device_switch_status_var_3.setText(value);
    }
    public void setDeviceSwitchConsumption3(String value) {
        this.device_consumption_var_3.setText(value);
    }


    public void setDeviceSwitchStatus4(String value) {
        this.device_status_text_view_4.setText(value);
    }
    public void setDeviceSwitchState4(String value) {
        this.device_switch_status_var_4.setText(value);
    }
    public void setDeviceSwitchConsumption4(String value) {
        this.device_consumption_var_4.setText(value);
    }
}