package com.vsb.kru13.osmzhttpserver;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
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

public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean bRunning;

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
                File sdPath = Environment.getExternalStorageDirectory();
                File f = new File(sdPath.getAbsoluteFile() + "/index.htm");

                if (f.exists()) {

                }
                else {
                    break;
                }

                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");

                OutputStream o = s.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                String tmp = in.readLine();

                while (!tmp.isEmpty()) {
                    Log.d("SERVER", tmp);
                    tmp = in.readLine();
                }

                String resp = "HTTP/1.0 200 OK\n" +
                        "Content-Type: text/html\n" +
                        "\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "<h1>Happy New Millennium!</h1>\n" +
                        "</body>\n" +
                        "</html>";

                out.write(resp);
                //out.write(tmp.toUpperCase());
                out.flush();

                s.close();
                Log.d("SERVER", "Socket Closed");
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

