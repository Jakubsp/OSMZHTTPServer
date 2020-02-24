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
    public final String folder = "HTTPServer";
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
                String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + folder;
                String resp_http = "";

                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");

                OutputStream o = s.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                String tmp = in.readLine();
                String filePath = null;
                String fileType = null;

                while (!tmp.isEmpty()) {
                    Log.d("SERVER", tmp);
                    String[] tmp_array = tmp.split(" ");
                    if (tmp_array[0].equals("GET")) {
                        filePath = tmp_array[1];
                    }
                    else if (tmp_array[0].equals(("Accept:"))) {
                        fileType = tmp_array[1].split(",")[0];
                    }
                    tmp = in.readLine();
                }

                if (filePath.equals("/")) {
                    File f = new File(sdPath + "/index.htm");
                    if (f.exists()) {
                        if (f.isFile()) {
                            resp_http = "HTTP/1.0 200 OK\n" +
                                    "     Content-type: text/html\n" +
                                    "    Content_length: " + f.length() + "\n\n";
                            FileInputStream fis = new FileInputStream(f);
                            int content;
                            while ((content = fis.read()) != -1) {
                                resp_http += (char)content;
                            }
                        }

                    }
                    else {
                        resp_http = "HTTP/1.0 404 NotFound\n" +
                                "           Content-type: text/html\n" +
                                "<html><h1>non</h1></html>\n";
                    }
                    out.write(resp_http);

                    out.flush();
                }
                else {
                    File f = new File(sdPath + filePath);
                    if (f.exists()) {
                        if (f.isFile() && fileType.equals("image/webp")) {
                            resp_http = "HTTP/1.0 200 OK\n" +
                                    "    Content-type: image/jpeg\n" +
                                    "    Content_length: " + f.length() + "\n\n";

                            out.write(resp_http);
                            out.flush();

                            FileInputStream fis = new FileInputStream(f);
                            byte[] buff = new byte[fis.available()];
                            while ((fis.read(buff)) != -1) {
                            }

                            o.write(buff);
                            o.flush();
                        }

                    }
                }

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

