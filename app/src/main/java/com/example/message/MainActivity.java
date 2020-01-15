package com.example.message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;

    private EditText mEditTextInput;
    private TextView mTextViewCountDown;
    private CountDownTimer mCountDownTimer;
    private Button mButtonSet;
    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private  long mTimeLeftInMillis = mStartTimeInMillis;
    EditText start;
    EditText number;
    EditText destination;
    Button send;
    Button mButtonEnd;


    SensorManager sensorManager;
    Sensor proximitySensor;
    boolean hasProximity;
    float maxDistance;

    boolean sensorka = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {




        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY)) {
                hasProximity = true;
                sensorManager = (SensorManager) getSystemService(MainActivity.this.SENSOR_SERVICE);
                proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

                maxDistance = proximitySensor.getMaximumRange();
            } else {
                Toast.makeText(this, "keine Annäherungssensor", Toast.LENGTH_SHORT).show();
            }



        start = findViewById(R.id.inputStart);
        number = findViewById(R.id.inputNumber);
        destination = findViewById(R.id.inputDestination);
        send = findViewById(R.id.buttonSend);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mEditTextInput = findViewById(R.id.edit_text_input);
        mButtonSet = findViewById(R.id.button_set);
        mButtonEnd = findViewById(R.id.buttonEnd);



        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mEditTextInput.getText().toString();
                if(input.length() == 0){
                    Toast.makeText(MainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if(millisInput == 0){
                    Toast.makeText(MainActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);


            }
        });

        send.setEnabled(true);
        if(checkPermission(Manifest.permission.SEND_SMS)){
            send.setEnabled(true);
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(hasProximity){
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(hasProximity){
            sensorManager.unregisterListener(MainActivity.this);
        }
    }

    public void onSend(View v){
        String start2 = start.getText().toString();
        String phoneNumber = number.getText().toString();
        String destination2 = destination.getText().toString();
        String duration = mEditTextInput.getText().toString();





        if(phoneNumber == null || phoneNumber.length() == 0 || destination2 == null || destination2.length() == 0 || start2 == null || start2.length() == 0){
            Toast.makeText(this, "Please fill everything out", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkPermission(Manifest.permission.SEND_SMS)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null,
                    "Ich fahre von " + start2 + " nach " + destination2 + " und werde ungefähr " + duration + " Minuten haben.", null, null);
            Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
            start.setVisibility(View.INVISIBLE);
            number.setVisibility(View.INVISIBLE);
            destination.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
            mEditTextInput.setVisibility(View.INVISIBLE);
            mButtonEnd.setVisibility(View.VISIBLE);

            sensorka = true;




        }else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
        if(mTimerRunning){
            pauseTimer();
        }else{
            startTimer();
        }
       



    }
    public void end(View v){

        String phoneNumber = number.getText().toString();
        String destination2 = destination.getText().toString();
        pauseTimer();
        String timer = mTextViewCountDown.getText().toString();

        if (checkPermission(Manifest.permission.SEND_SMS)) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null,
                    "Ich bin am Standort " + destination2 +  " angekommen. Timer: " + timer, null, null);
            Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
            start.setVisibility(View.VISIBLE);
            start.setText("");
            number.setVisibility(View.VISIBLE);
            number.setText("");
            destination.setVisibility(View.VISIBLE);
            destination.setText("");
            mButtonSet.setVisibility(View.VISIBLE);
            mEditTextInput.setVisibility(View.VISIBLE);
            mEditTextInput.setText("");

            sensorka = false;


        }else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }



        mButtonEnd.setVisibility(View.INVISIBLE);
    }

    private void setTime(long milliseconds){
        mStartTimeInMillis = milliseconds;
        resetTimer();
    }
    private void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountdownText();

    }

    private void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                String timer = mTextViewCountDown.getText().toString();
                String phoneNumber = number.getText().toString();
               
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null,
                            "Nicht angekommen, bitte probieren Sie den Motorradfahrer/in anzurufen.", null, null);
                Toast.makeText(MainActivity.this, "Message Send", Toast.LENGTH_SHORT).show();

            }
        }.start();

        mTimerRunning = true;

    }

    private void pauseTimer(){
        mCountDownTimer.cancel();
        mTimerRunning = false;

    }

    private void updateCountdownText(){
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;

        if(hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        }else{
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);

        }

        mTextViewCountDown.setText(timeLeftFormatted);
    }




    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        if(distance < maxDistance && sensorka == true) {
            String phoneNumber = number.getText().toString();
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null,
                    "Ich werde mich um 10 Minuten verspäten.", null, null);
            Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

  /*  @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("millisLeft", mTimeLeftInMillis);
        outState.putBoolean("timerRunning", mTimerRunning);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTimeLeftInMillis = savedInstanceState.getLong("millisLeft");
        mTimerRunning = savedInstanceState.getBoolean("timerRunning");
        updateCountdownText();


        if(mTimerRunning) {
            startTimer();
        }
    }

   **/
}
