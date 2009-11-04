package com.tastycactus.ultimaterps;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Random;

public class RPSActivity extends Activity
{
    private ViewSwitcher switcher = null;
    private SensorManager mgr = null;
    private SensorEventListener listener = null;
    private TextView accel_view = null;
    private ImageView image_view = null;
    private float previous_accel = 0;
    private boolean debouncing = false;
    private final double threshold = (0.80 * SensorManager.GRAVITY_EARTH) * (0.80 * SensorManager.GRAVITY_EARTH);

    Random generator = new Random();

    private final int images[] = { R.drawable.rock, R.drawable.paper, R.drawable.scissors };
    private final String labels[] = { "Ready!", "1", "2", "" };

    private static final int READY_STATE = 0;
    private static final int ONE_STATE = 1;
    private static final int TWO_STATE = 2;
    private static final int IMAGE_STATE = 3;

    private int state = READY_STATE;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        setContentView(R.layout.main);

        switcher = (ViewSwitcher)findViewById(R.id.switcher);

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
        image_view = (ImageView)findViewById(R.id.image);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        boolean first_run = sp.getBoolean("FirstRun", true);
        if (first_run) {
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setTitle("About Ultimate RPS");
            d.setMessage("Welcome to Ultimate Rock-Paper-Scissors!\n\nTo play UltimateRPS, bring the phone in a downward motion three times.");
            d.show();
            //Editor e = getPreferences(Context.MODE_PRIVATE).edit();
            //e.putBoolean("FirstRun", false);
        }
        mgr.registerListener(listener, mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        state = READY_STATE;
        showText();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mgr.unregisterListener(listener);
    }

    private void updateAcceleration(float accel) {
        if (debouncing && accel < 0.80 * threshold) {
            debouncing = false;
        }

        if (!debouncing && previous_accel != 0 && accel > threshold && previous_accel < threshold) {
            // Was going down, but stopped
            if (state == READY_STATE) {
                state = ONE_STATE;
                showText();
            } else if (state == ONE_STATE) {
                state = TWO_STATE;
                showText();
            } else if (state == TWO_STATE) {
                state = IMAGE_STATE;
                showImage();
            } else if (state == IMAGE_STATE) {
                state = ONE_STATE;
                showText();
            }
            debouncing = true;
        }
        previous_accel = accel;
    }

    private void showText() {
        accel_view.setText(labels[state]);
        if (switcher.getCurrentView() != accel_view) {
            switcher.showNext();
        }
    }

    private void showImage() {
        int idx = generator.nextInt(3);
        image_view.setImageResource(images[idx]);
        if (switcher.getCurrentView() != image_view) {
            switcher.showNext();
        }
    }
}
