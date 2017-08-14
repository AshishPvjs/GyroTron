package com.example.lenovo.gyrotron;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import mr.go.sgfilter.SGFilter;


public class MainActivity extends Activity implements SensorEventListener{

    private TextView xText, yText, zText;
    private Sensor mySensor;
    private SensorManager SM;
    private Button start;
    private GraphView graphView;
    private String data="";
    private int temp=1;
    private int i,l;
    int on=0;

    private ArrayList<Float> data_points;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create our Sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);



        // Assign TextView
        xText = (TextView)findViewById(R.id.textView5);
        yText = (TextView)findViewById(R.id.textView6);
        zText = (TextView)findViewById(R.id.textView7);
        start =(Button)findViewById(R.id.start_button);
        graphView=(GraphView)findViewById(R.id.grap_plot);
        //stop=(Button)findViewById(R.id.stop_button);

        data_points = new ArrayList<Float>();
    }

    public void start_session(View sb){
        on=1-on;

        if(on==0) {
            LineGraphSeries<DataPoint> series =new LineGraphSeries<>();
            LineGraphSeries<DataPoint> filtered=new LineGraphSeries<>();
            SGFilter filt = new SGFilter(3, 3);
            l = data_points.size();
            float[] filtered_points = new float[l];
            for (i = 0; i < l; i++) {
                filtered_points[i] = data_points.get(i);
            }
            float[] filtered_data = filt.smooth(filtered_points, SGFilter.computeSGCoefficients(3, 3, 3));


            for(i=0;i < filtered_points.length;i++){
                series.appendData(new DataPoint(i,filtered_points[i]),true,100);
            }
            for(i=0;i < filtered_data.length;i++){
                filtered.appendData(new DataPoint(i,filtered_data[i]),true,100);
            }
            graphView.addSeries(series);
            graphView.addSeries(filtered);
            String s = "Data_Set" + temp + ".txt";
            temp = temp + 1;
            writeDataToFile(getApplicationContext(), s, data);
        }
    }
    /*public void stop_session(View stb)
    {
        on=false;
        return;
    }*/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float magnitude;

        if (on == 1) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            magnitude = (float) Math.sqrt((x * x) + (y * y) + (z * z));
            data_points.add(magnitude);
            String x1 = String.format("%f", event.values[0]);
            String y1 = String.format("%f", event.values[1]);
            String z1 = String.format("%f", event.values[2]);
            data = data + x1 + " " + y1 + " " + z1 + " " + System.currentTimeMillis() + "\n";
            xText.setTextColor(Color.BLACK);
            yText.setTextColor(Color.BLACK);
            zText.setTextColor(Color.BLACK);
            if (x > y && x > z) {
                xText.setTextColor(Color.RED);
            }
            if (y > x && y > z) {
                yText.setTextColor(Color.RED);
            }
            if (z > y && z > x) {
                zText.setTextColor(Color.RED);
            }
            xText.setText("" + event.values[0]);
            yText.setText("" + event.values[1]);
            zText.setText("" + event.values[2]);
        }

    }



    public void writeDataToFile(Context context, String File_Name, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Sensor_Data/");
            root.mkdirs();
            File gpxfile = new File(root, File_Name);
            gpxfile.createNewFile();
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error"+e, Toast.LENGTH_LONG).show();
        }
    }

}