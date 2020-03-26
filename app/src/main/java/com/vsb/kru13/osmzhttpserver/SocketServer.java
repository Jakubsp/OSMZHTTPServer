package com.vsb.kru13.osmzhttpserver;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    private int maxClients = 5;
    boolean bRunning;
    private Handler mHandler;
    private Semaphore semaphore;


    public SocketServer(Handler mHandler) {
        this.mHandler = mHandler;
        semaphore = new Semaphore(maxClients);
    }

    private void SendMessage(String message, long transByt) {
        Message m = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("SERVER", message);
        b.putLong("LENGTH", transByt);
        m.setData(b);
        mHandler.sendMessage(m);
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    public void run() {
        try {
            Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;

            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                final Socket s = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");

                if (semaphore.tryAcquire()) {
                    Thread clientThread = new ClientSocket(s, mHandler, semaphore);
                    clientThread.start();
                }
                else {
                    SendMessage("Server couldn't accept incoming client:Too busy", 0);
                }
            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        }
        finally {
            serverSocket = null;
            bRunning = false;
        }
    }

}
