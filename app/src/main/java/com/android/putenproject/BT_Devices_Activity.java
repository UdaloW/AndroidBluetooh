package com.android.putenproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.bluetooth.*;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class BT_Devices_Activity extends AppCompatActivity {

    private ListView listView;
    private ArrayList mDeviceList = new ArrayList<String>();
    private ArrayList <BluetoothDevice> mDeviceListTest = new ArrayList<BluetoothDevice>();

    private BluetoothAdapter mBluetoothAdapter;
    //////////////////
    private ConnectThread connectThread;

    public static Handler handler;

    /////////сообщения Handler
    final int STATUS_CONNECTION_SUCCESS_TRUE = 0;
    final int STATUS_CONNECTION_SUCCESS_FALSE = 1;
    /////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_devices);

        setTitle("SearchDevices");
        listView = (ListView) findViewById(R.id.listViewDevices);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

         handler = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch (msg.what){
                    case STATUS_CONNECTION_SUCCESS_TRUE:
                        showToast("Соединение установлено");
                        break;
                    case STATUS_CONNECTION_SUCCESS_FALSE:
                        showToast("Не удалось соединиться");
                        break;
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
    }

    public void showToast(String message){
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add("..." + device.getName()+ "\n" + device.getAddress());
                mDeviceListTest.add(device);

                listView.setAdapter(new ArrayAdapter<Object>(context, android.R.layout.simple_list_item_1, mDeviceList));
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int pos = position + 1;
                    Log.i("TESTBT",  "Нажатие на номер " + pos + ", имя устройства " + mDeviceList.get(position));
                    mBluetoothAdapter.cancelDiscovery();

                        Log.i("TESTBT", "прошло условие");
                        BluetoothDevice device = mDeviceListTest.get(position);
                        Log.i("TESTBT", "Полученное имя device " + device.getName());

                        if (device != null){
                            //по нажатию на выбранное устройство просиходит создание объекта класса и запуск
                            connectThread = new ConnectThread(device);
                            connectThread.start();
                            Log.i("TESTBT", "Соединение...");

                            showToast("Соединение...");

                        }
                    }
            });
        }
    };
}

