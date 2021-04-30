package com.android.putenproject;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ConnectThread extends Thread {
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    
    private static BluetoothSocket bluetoothSocket = null;
    private static boolean success = false;
    private static ConnectedThread connectedThread;

    final int STATUS_CONNECTION_SUCCESS_TRUE = 0;
    final int STATUS_CONNECTION_SUCCESS_FALSE = 1;

    //////////////////////
    public static String getNameDevice(){
        try{
            Log.i("TESTBT", "getName = " + bluetoothSocket.getRemoteDevice().getName());
            return bluetoothSocket.getRemoteDevice().getName();
        } catch (Exception e){
            return null;
        }
    }
    /////////////////////
    public static boolean getSuccess(){
        return success;
    }
/////////////////////////////////
    public ConnectThread(BluetoothDevice device) {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            Log.i("TESTBT", "bluetoothSocket = " + bluetoothSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            bluetoothSocket.connect();
            success = true;
            Log.i("TESTBT", "Cоединение установлено");
            BT_Devices_Activity.handler.sendEmptyMessage(STATUS_CONNECTION_SUCCESS_TRUE);
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
            Log.i("TESTBT", "Не удалось соединиться");
            BT_Devices_Activity.handler.sendEmptyMessage(STATUS_CONNECTION_SUCCESS_FALSE);

            cancel();
        }

        if (success) {
            //TODO создаем экземлпяр класса
            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();

            Log.i("TESTBT", "Соединение установлено с : " + bluetoothSocket.getRemoteDevice().getName());
            Log.i("TESTBT", "Программа работает");

        }
    }

    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

