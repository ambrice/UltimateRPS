package com.tastycactus.ultimaterps;

import android.app.Activity;

import android.os.Bundle;

import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageView;

import java.util.Random;

public class ImageActivity extends Activity
{
    Random generator = new Random();

    private final int images[] = { R.drawable.rock, R.drawable.paper, R.drawable.scissors };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        setContentView(R.layout.image);

        ImageView image_view = (ImageView)findViewById(R.id.image);
        int idx = generator.nextInt(3);
        image_view.setImageResource(images[idx]);
    }
}
