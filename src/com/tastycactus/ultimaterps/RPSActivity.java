package com.tastycactus.ultimaterps;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.widget.TextView;

import com.tastycactus.ultimaterps.ImageActivity;

public class RPSActivity extends Activity
{
    //private float previous_z = 0;
    private SensorManager mgr = null;
    private SensorEventListener listener = null;
    private TextView accel_view = null;
    private float previous_accel = 0;
    private boolean debouncing = false;
    private final double threshold = (0.80 * SensorManager.GRAVITY_EARTH) * (0.80 * SensorManager.GRAVITY_EARTH);

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

	    listener=new SensorEventListener() {
            public void onSensorChanged(SensorEvent e) {
                if (e.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
                    float vector = (e.values[0] * e.values[0]) + (e.values[1] * e.values[1]) + (e.values[2] * e.values[2]);
                    updateAcceleration(vector);
                }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        accel_view = (TextView)findViewById(R.id.accel);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mgr.registerListener(listener, mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        accel_view.setText("Ready!");
    }

    private void updateAcceleration(float accel) {
        if (debouncing && accel < 0.80 * threshold) {
            debouncing = false;
        }

        if (!debouncing && previous_accel != 0 && accel > threshold && previous_accel < threshold) {
            // Was going down, but stopped
            String curr = (String)accel_view.getText();
            if (curr.equals("Ready!")) {
                accel_view.setText("1");
            } else if (curr.equals("1")) {
                accel_view.setText("2");
            } else if (curr.equals("2")) {
                Intent i = new Intent(this, ImageActivity.class);
                startActivity(i);
                mgr.unregisterListener(listener);
            }
            debouncing = true;
        }
        previous_accel = accel;
    }
}
