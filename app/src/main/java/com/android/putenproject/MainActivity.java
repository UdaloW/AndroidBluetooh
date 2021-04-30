package com.android.putenproject;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static Handler handlerMain;

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    Toast toaster;
    BluetoothAdapter bluetooth;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ASK_GPS = 2;
    TextView statusBT;
    TextView messageTW;

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetooth = BluetoothAdapter.getDefaultAdapter();

        stateBT();

        editText = findViewById(R.id.editTextSent);

        handlerMain = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch (msg.what){
                    case MessageConstants.MESSAGE_READ:
                        String readMessage = (String)msg.obj;
                        if (msg.arg1 > 0) {
                            messageTW = findViewById(R.id.messageTextView);
                            messageTW.clearComposingText();
                            messageTW.setText(readMessage);
                            showToast("Принято сообщение: \"" + readMessage + "\"");
                        }
                        break;
                    case MessageConstants.MESSAGE_WRITE:
                        showToast("Отправка сообщения");
                        break;
                    case MessageConstants.MESSAGE_TOAST:
                        showToast("Ошибка отправки");
                        break;
                }

            }
        };
    }

    //метод показа высплывающего уведомления
    public void showToast(String message) {
        toaster = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toaster.show();
    }

    //Проверка наличия модуля Bluetooth
    boolean checkModuleBT() {
        if (bluetooth != null) {
            // bluetooth модуль присутствует
            return true;
        } else {
            // bluetooth модуль отсутствует/не поддерживается
            showToast("Ваше устройство не поддерживает технологию Bluetooth");
            return false;
        }
    }

    //Отображение статуса Bluetooth
    void stateBT() {
        if (checkModuleBT()) {
            int state = bluetooth.getState();
            statusBT = findViewById(R.id.statusBTTextView);

            if (state == 10) {
                statusBT.setText("Bluetooth OFF");
                statusBT.setTextColor(Color.parseColor("#FFFF0000"));
            } else if (state == 12) {
                statusBT.setText("Bluetooth ON");
                statusBT.setTextColor(Color.parseColor("#FF00FF04"));
                if (ConnectThread.getNameDevice() != null && ConnectThread.getSuccess() == true) {
                    statusBT = findViewById(R.id.statusBTTextView);
                    statusBT.setText("Bluetooth connected to " + ConnectThread.getNameDevice());
                    statusBT.setTextColor(Color.parseColor("#FF00FF04"));
                } else {
                    //statusBT.setText("Bluetooth disconnected" );
                }
            } else {
                //ничего не делать
            }
        } else {
            statusBT = findViewById(R.id.statusBTTextView);
            statusBT.setText("Your device not support Bluetooth");
            statusBT.setTextColor(Color.parseColor("#FFFF0000"));
        }

    }

    //Событие на кнопку "Change Device"
    public void onClickSearchBT(View view) {
        if (checkModuleBT()) {
            if (bluetooth.isEnabled()) {
                //Bluetooth включен. Работаем.
                askAccept(); //Вызов метода запроса доступа к местоположению
            } else {
                //Bluetooth выключен. Пердложим пользователю включить его.
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
                stateBT();
            }
        }
    }

    //События при включенном Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            askAccept();        //вызов запроса на доступ к GPS
        } else {
            //ничего не делать
        }
    }

    //События, происходящие при развертывании приложения
    @Override
    protected void onResume() {
        super.onResume();
        stateBT();
    }

    //Запрос на доступ к местоположению
    void askAccept() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //если Android Q
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_ASK_GPS);
        } else {
            //если другая версия Android
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_ASK_GPS);
        }
    }

    //Проверка активности GPS-модуля
    public static boolean checkGPSStatus(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return statusOfGPS;
    }

    //События при предоставлении доступа к метоположению
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ASK_GPS && grantResults[0] == 0) {
            //проверка активности модуля GPS
            if (!checkGPSStatus(this)) {
                //если GPS выключен, то вызвать активность GPS для включения
                Intent gpsOptionsIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(gpsOptionsIntent);
            } else {
                //если GPS включен, то перейти в активность BT_Devices_Activity
                Intent intent = new Intent(MainActivity.this, BT_Devices_Activity.class);
                startActivity(intent);
            }
        }
    }

    //Событие по кнопке Отправить
    public void onClickButtonSent(View view)  {
        ////
        if (ConnectThread.getSuccess()) {
            String message = editText.getText().toString();
            ConnectedThread.write(message);
            Log.i("TESTBT", "edittext = " + message);
        } else {
            showToast("Нет соединения с удаленным устройством");
        }
    }
}