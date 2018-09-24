package cloud.nalkins.nalkinscloud.deviceLayouts;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cloud.nalkins.nalkinscloud.R;

/**
 * Created by Arie on 4/25/2017.
 *
 * This class is basically a object that represents a layout,
 * This object will allow to modify the layout programmatically
 */
public class DynamicLayoutMagnet {

    Context context;

    private boolean _isDeviceOnline;

    //private String deviceId;
    //private String deviceType;
    //private String deviceName;

    private LinearLayout deviceOptionsLayout;

    private ImageView deviceIcon;
    private ImageView wifiSignalStrengthIcon;
    private TextView deviceStatus;
    private TextView deviceNameText;
    private TextView deviceIsTriggeredReleasedText;
    private TextView deviceIsOpenedClosedText;
    private View v;

    /**
     * DynamicLayoutMagnet constructor
     *
     * @param context the context from the layout this object was called
     * @param deviceId the ID of this object
     * @param deviceName Name of this object
     * @param deviceType Type of this object
     */
    public DynamicLayoutMagnet(Context context, String deviceId, String deviceType, String deviceName) {

        this.context = context; // Layout context

        this._isDeviceOnline = false;
        //this.deviceId = deviceId;
        //this.deviceType = deviceType;
        //this.deviceName = deviceName;

        LayoutInflater inflater = LayoutInflater.from(context);
        this.v = inflater.inflate(R.layout.magnet_device, null); // Inflate the 'temperature_device' layout

        this.deviceOptionsLayout = (LinearLayout) v.findViewById(R.id.settingsLayout);

        this.deviceStatus = (TextView)v.findViewById(R.id.device_status); // Set 'online' \ 'offline' status

        this.deviceIcon = (ImageView) v.findViewById(R.id.device_icon); // Define and set the layouts side icon
        deviceIcon.setImageResource(R.drawable.magnet_main_64);

        this.deviceNameText =(TextView)v.findViewById(R.id.device_name); // Set device name text view area
        deviceNameText.setText(deviceName);

        this.deviceIsTriggeredReleasedText =(TextView)v.findViewById(R.id.device_locked_status); // Set device status text view area

        this.deviceIsOpenedClosedText =(TextView)v.findViewById(R.id.device_current_lock_status); // Set device current state text view area
    }

    // Function return the View object of relevant layout
    public View getView(){
        return v;
    }

    // Function sets icon for the layout
    public void setDeviceIcon(int iconId) {
        this.deviceIcon.setImageResource(iconId);
    }
    public ImageView getDeviceIcon() {
        return deviceIcon;
    }

    public LinearLayout getDeviceOptionsLayout() {
        return this.deviceOptionsLayout;
    }

    public void setWifiSignalStrengthIcon(int iconId) {
        this.wifiSignalStrengthIcon.setImageResource(iconId);
    }
    ImageView getWifiSignalStrengthIcon() {
        return deviceIcon;
    }


    // Function set 'Triggered' or 'Released' status
    public void setIsTriggeredReleasedStatusText(String value) {
        this.deviceIsTriggeredReleasedText.setText(value);
    }

    public void setIsTriggeredReleasedStatusTextColor(int value) {
        this.deviceIsTriggeredReleasedText.setTextColor(ContextCompat.getColor(context, value));
    }

    public TextView getIsTriggeredReleasedStatusText() {
        return deviceIsTriggeredReleasedText;
    }



    // Function sets current magnet status text, 'opened' or 'closed'
    public void setIsOpenedLockedStateText(String value) {
        this.deviceIsOpenedClosedText.setText(value);
    }

    public void setIsOpenedLockedStateTextColor(int value) {
        this.deviceIsOpenedClosedText.setTextColor(ContextCompat.getColor(context, value));
    }




    public void setDeviceStatus(String value) {
        this.deviceStatus.setText(value);
        if(value.equals("online"))
            _isDeviceOnline = true;
        else if(value.equals("offline"))
            _isDeviceOnline = false;
    }

    public boolean isDeviceOnline() { return this._isDeviceOnline; }

    public void setStatusTextColor(int value) {
        this.deviceStatus.setTextColor(ContextCompat.getColor(context, value));
    }

}
