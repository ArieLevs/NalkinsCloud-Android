package cloud.nalkins.nalkinscloud;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Arie on 5/24/2017.
 *
 */
public class DynamicScheduledJob {

    private View view;

    DynamicScheduledJob(Context context, String deviceId, String deviceType, String deviceName) {


        LayoutInflater inflater = LayoutInflater.from(context);
        this.view = inflater.inflate(R.layout.scheduled_job_layout, null); // Inflate the 'temperature_device' layout
    }


    // Function return the View object of relevant layout
    public View getView(){
        return view;
    }
}
