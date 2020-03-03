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

public class ClientSocket extends Thread {
    public final Socket s;
    private Handler mHandler;
    public final String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HTTPServer";

    public ClientSocket(Socket s, Handler mHandler) {
        this.s = s;
        this.mHandler = mHandler;
    }


    private void SendMessage(String message, long transByt) {
        Message m = new Message();
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
                        SendMessage("Client requested index.html", f.length());
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
                        SendMessage("Client requested image " + filePath, f.length());
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
            Log.d("SERVER", "Client thread ending");
            s.close();
            Log.d("SERVER", "Socket Closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
