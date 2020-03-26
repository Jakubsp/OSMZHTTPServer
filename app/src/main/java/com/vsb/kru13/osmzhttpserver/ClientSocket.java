package com.vsb.kru13.osmzhttpserver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.MainThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClientSocket extends Thread {
    public final Socket s;
    private Handler mHandler;
    public final String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HTTPServer";
    private String ipClient = "";
    private Semaphore semaphore;

    public ClientSocket(Socket s, Handler mHandler, Semaphore semaphore) {
        this.s = s;
        this.mHandler = mHandler;
        this.semaphore = semaphore;
    }


    private void SendMessage(String message, long transByt) {
        Message m = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("SERVER", message);
        b.putLong("LENGTH", transByt);
        m.setData(b);
        mHandler.sendMessage(m);
    }

    @Override
    public void run() {
        try {
            Log.d("SERVER", "Creating client thread");
            String resp_http = "";

            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            ipClient = s.getInetAddress().toString() + ":" + s.getPort();

            String tmp = in.readLine();
            String filePath = null;
            String fileType = null;
            File f;

            while (!tmp.isEmpty()) {
                Log.d("SERVER", tmp);
                String[] tmp_array = tmp.split(" ");
                if (tmp_array[0].equals("GET")) {
                    filePath = tmp_array[1];
                }
                tmp = in.readLine();
            }

            /* Finding filetype */
            String[] tmp_filetype = null;
            tmp_filetype = filePath.split("\\.");
            if (tmp_filetype.length > 0)
                fileType = tmp_filetype[tmp_filetype.length - 1];

            /* Finding a file */
            if (fileType.equals("/")) {
                f = new File(sdPath + filePath + "index.htm");
                fileType = "htm";
            }
            else
                f = new File(sdPath + filePath);

            /* Checking if file exists */
            if (!f.exists()) {
                resp_http = "HTTP/1.1 404 NotFound\n" +
                            "Content-type: text/html\n\n" +
                            "<html><h1>non</h1></html>\n";
                out.write(resp_http);
                out.flush();
            }
            else {
                resp_http = "HTTP/1.1 200 OK\n" +
                            "Content-type: ";
                /* File is a text */
                if (fileType.equals("htm")) {
                    resp_http += "text/html\n" +
                                 "Content_length: " + f.length() + "\n\n";
                    FileInputStream fis = new FileInputStream(f);
                    int content;
                    while ((content = fis.read()) != -1) {
                        resp_http += (char)content;
                    }
                    out.write(resp_http);

                    out.flush();
                }
                /* File is a image */
                else {
                    resp_http += "image/" + fileType + "\n" +
                                 "Content_length: " + f.length() + "\n\n";
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
            SendMessage(ipClient + " requested address '" + f.getAbsolutePath().toString() +
                    "' with total bytes " + f.length(), f.length());
            /*if (filePath.equals("/")) {
                File f = new File(sdPath + "/index.htm");
                if (f.exists()) {
                    if (f.isFile()) {
                        SendMessage("Client requested index.html", f.length());
                        resp_http = "HTTP/1.1 200 OK\n" +
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
                    resp_http = "HTTP/1.1 404 NotFound\n" +
                            "           Content-type: text/html\n\n" +
                            "<html><h1>non</h1></html>\n";
                }
                out.write(resp_http);

                out.flush();
            }
            else {
                File f = new File(sdPath + filePath);
                if (f.exists()) {
                    if (f.isFile() && fileType.equals("image/webp")) {
                        SendMessage("Client requested image " + filePath, f.length());
                        resp_http = "HTTP/1.1 200 OK\n" +
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
            }*/
            Log.d("SERVER", "Client thread ending");
            s.close();
            Log.d("SERVER", "Socket Closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            semaphore.release();
        }
    }
}
