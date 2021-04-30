package com.android.putenproject;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.android.putenproject.MainActivity.handlerMain;

public class ConnectedThread extends Thread {
    private static final String TAG = "MY_APP_DEBUG_TAG";

    private static InputStream inputStream = null;
    private static OutputStream outputStream = null;

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }


    public ConnectedThread(BluetoothSocket bluetoothSocket) {
        try {
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        //Считывание информации с BT
        byte[] mmBuffer = new byte[256];      // Буффер для хранения потока
        int numBytes;                   // количество байт, возвращаемые из потока read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = inputStream.read(mmBuffer);
                String readMessage = new String(mmBuffer, 0, numBytes);
                // Send the obtained bytes to the UI activity.
                handlerMain.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1,
                        readMessage).sendToTarget();

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    //Метод отправкии сообщений
    public static void write(String command) {
        byte[] bytes = command.getBytes();
        if (outputStream != null){
            try {
                outputStream.write(bytes);
                outputStream.flush();
                // Share the sent message with the UI activity.
                handlerMain.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, bytes).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                handlerMain.obtainMessage(MessageConstants.MESSAGE_TOAST, -1, -1, bytes).sendToTarget();
            }
        }
    }

    public void cancel() {
        try {
            inputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}

