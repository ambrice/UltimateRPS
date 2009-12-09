package com.tastycactus.ultimaterps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.view.View;
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
    private int selected_item = 3; // default to random

    private static final double falling_threshold = (0.60 * SensorManager.GRAVITY_EARTH) * (0.60 * SensorManager.GRAVITY_EARTH);
    private static final double stopped_threshold = (0.90 * SensorManager.GRAVITY_EARTH) * (0.90 * SensorManager.GRAVITY_EARTH);

    Random generator = new Random();

    private static final int images[] = { R.drawable.rock, R.drawable.paper, R.drawable.scissors };
    private static final String selections[] = { "Rock", "Paper", "Scissors", "Random" };
    private static final String labels[] = { "Ready!", "1", "2", "" };

    private static final int DIALOG_ABOUT = 0;
    private static final int DIALOG_PICK = 1;

    // state
    private static final int READY_STATE = 0;
    private static final int ONE_STATE = 1;
    private static final int TWO_STATE = 2;
    private static final int IMAGE_STATE = 3;

    private int state = READY_STATE;

    private static final int STOPPED_STATE = 0;
    private static final int FALLING_STATE = 1;

    private int accel_state = STOPPED_STATE;
    private int prev_accel_state = STOPPED_STATE;

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
                    float vector = 0;
                    vector = e.values[0] > 0 ? vector + (e.values[0] * e.values[0]) : vector - (e.values[0] * e.values[0]);
                    vector = e.values[1] > 0 ? vector + (e.values[1] * e.values[1]) : vector - (e.values[1] * e.values[1]);
                    vector = e.values[2] > 0 ? vector + (e.values[2] * e.values[2]) : vector - (e.values[2] * e.values[2]);
                    updateAcceleration(vector);
                }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        accel_view = (TextView)findViewById(R.id.accel);
        image_view = (ImageView)findViewById(R.id.image);
        image_view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mgr.unregisterListener(listener);
                showDialog(DIALOG_PICK);
            }
        });

    }

    @Override
    public void onStart()
    {
        super.onStart();
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        boolean first_run = sp.getBoolean("FirstRun", true);
        showDialog(DIALOG_PICK);
        if (first_run) {
            showDialog(DIALOG_ABOUT);
            Editor e = getPreferences(Context.MODE_PRIVATE).edit();
            e.putBoolean("FirstRun", false);
            e.commit();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mgr.unregisterListener(listener);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        Dialog dialog;
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_ABOUT:
                b.setTitle("About Ultimate RPS")
                    .setMessage(R.string.about);
                dialog = b.create();
                break;
            case DIALOG_PICK:
                b.setTitle("Choose")
                    .setCancelable(false)
                    .setItems(selections, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            RPSActivity.this.onSelection(item);
                        }
                    });
                dialog = b.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    protected void onSelection(int selection)
    {
        selected_item = selection;
        state = READY_STATE;
        accel_state = STOPPED_STATE;
        prev_accel_state = STOPPED_STATE;
        mgr.registerListener(listener, mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        showText();
    }

    private void updateAcceleration(float accel)
    {
        prev_accel_state = accel_state;

        if (accel < falling_threshold) {
            accel_state = FALLING_STATE;
        } else if (accel > stopped_threshold) {
            accel_state = STOPPED_STATE;
        }

        if (prev_accel_state == FALLING_STATE && accel_state == STOPPED_STATE) {
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
            }
        }
    }

    private void showText()
    {
        accel_view.setText(labels[state]);
        if (switcher.getCurrentView() != accel_view) {
            switcher.showNext();
        }
    }

    private void showImage() 
    {
        int idx;
        if (selected_item == 3) {
            idx = generator.nextInt(3);
        } else {
            idx = selected_item;
        }
        image_view.setImageResource(images[idx]);
        if (switcher.getCurrentView() != image_view) {
            switcher.showNext();
        }
    }
}
