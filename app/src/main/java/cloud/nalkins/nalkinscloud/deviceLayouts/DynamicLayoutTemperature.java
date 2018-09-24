package cloud.nalkins.nalkinscloud.deviceLayouts;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cloud.nalkins.nalkinscloud.R;

/**
 * Created by Arie on 4/25/2017.
 *
 */
public class DynamicLayoutTemperature extends AppCompatActivity {

    private Context context;

    private boolean _isDeviceOnline;

    private String deviceId;
    private String deviceType;
    private String deviceName;

    private LinearLayout deviceOptionsLayout;

    private TextView deviceStatus;
    private ImageView deviceOptionsIcon;
    private ImageView deviceRemoveIcon;
    private ImageView deviceIcon;
    private TextView deviceNameText;
    private TextView device_temp_var;
    private TextView device_humidity_var;
    //private EditText editText;
    //private Button btn;
    private View v;

    // DynamicLayoutTemperature constructor
    public DynamicLayoutTemperature(Context context, String deviceId, String deviceType, String deviceName) {

        this.context = context; // Layout context

        this._isDeviceOnline = false;

        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.deviceName = deviceName;

        LayoutInflater inflater = LayoutInflater.from(context);
        this.v = inflater.inflate(R.layout.temperature_device, null); // Inflate the 'temperature_device' layout

        this.deviceOptionsLayout = (LinearLayout) v.findViewById(R.id.settingsLayout);

        this.deviceStatus = (TextView)v.findViewById(R.id.device_status); // Set 'online' \ 'offline' status

        this.deviceIcon = (ImageView) v.findViewById(R.id.device_icon); // Define and set the layouts side icon
        deviceIcon.setImageResource(R.drawable.temperature_main_64);

        this.deviceOptionsIcon = (ImageView) v.findViewById(R.id.device_options); // Define and set the layouts side icon

        this.deviceRemoveIcon = (ImageView) v.findViewById(R.id.device_remove_icon); // Define and set the layouts side icon

        this.deviceNameText =(TextView)v.findViewById(R.id.device_name_text_view); // Set device name text view area
        deviceNameText.setText(deviceName);

        this.device_temp_var =(TextView)v.findViewById(R.id.device_temp_var); // Set device temperature text view area
        this.device_humidity_var =(TextView)v.findViewById(R.id.device_humidity_var); // Set device humidity text view area

        //this.btn =(Button)v.findViewById(R.id.button1);
    }

    public boolean isDeviceOnline() {
        return this._isDeviceOnline;
    }

    public ImageView getOptionsIcon() {
        return deviceOptionsIcon;
    }

    public ImageView getRemoveIcon() {
        return deviceRemoveIcon;
    }

    public ImageView getDeviceIcon() {
        return deviceIcon;
    }

    public LinearLayout getDeviceOptionsLayout() {
        return this.deviceOptionsLayout;
    }

    // Set online / offline status
    public void setStatusValue(String value) {
        this.deviceStatus.setText(value);
        if(value.equals("online"))
            _isDeviceOnline = true;
        else if(value.equals("offline"))
            _isDeviceOnline = false;
    }

    public void setStatusTextColor(int value) {
        this.deviceStatus.setTextColor(ContextCompat.getColor(context, value));
    }

    // Function return the View object of relevant layout
    public View getView(){
        return v;
    }

    public void setDeviceTemperatureText(String value) {
        this.device_temp_var.setText(value);
    }
    public TextView getDeviceTempratureText() {
        return device_temp_var;
    }

    public void setDeviceHumidityText(String value) {
        this.device_humidity_var.setText(value);
    }
    public TextView getDeviceHumidityText() {
        return device_humidity_var;
    }

}
