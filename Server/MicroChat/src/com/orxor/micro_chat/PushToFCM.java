package com.orxor.micro_chat;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class PushToFCM extends Thread {

    private static final String SERVER_KEY="key=AIzaSyCpDXL6ASTgh6-O0j-e-0aM-pZ2d_IagYU";
    private Message message;

    public PushToFCM(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        super.run();
        URL url=null;
        HttpURLConnection connection=null;
        InputStream inputStream=null;
        OutputStream outputStream=null;

        try {
            Gson gson = new Gson();
            HashMap<String,String> payload = new HashMap<>();
            payload.put("message", gson.toJson(message));
            FcmDto fcmDto = new FcmDto("/topics/MicroChat", payload);
            url=new URL("https://fcm.googleapis.com/fcm/send");
            connection= (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization",SERVER_KEY);
            connection.connect();
            outputStream=connection.getOutputStream();
            outputStream.write(gson.toJson(fcmDto).getBytes(StandardCharsets.UTF_8));
            inputStream = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int actuallyRead=0;
            StringBuilder builder=new StringBuilder();
            while ((actuallyRead=inputStream.read(buffer))!=-1){
                builder.append(new String(buffer,0,actuallyRead,StandardCharsets.UTF_8));
            }
            System.out.println(builder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
