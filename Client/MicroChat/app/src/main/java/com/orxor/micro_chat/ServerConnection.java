package com.orxor.micro_chat;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ServerConnection extends AsyncTask<ServerAction,Void,ServerResponse> {

    private static final String SPEC = "http://192.168.1.9:8080/MicroChat_war_exploded/MicroChat"; // Default IP for emulator: 10.0.2.2
    private OnActualPostExecute actualPostExecute;

    public void setActualPostExecute(OnActualPostExecute actualPostExecute) {
        this.actualPostExecute = actualPostExecute;
    }

    @Override
    protected ServerResponse doInBackground(ServerAction... serverActions) {
        URL url;
        HttpURLConnection connection=null;
        InputStream inputStream=null;
        OutputStream outputStream=null;
        Gson gson = new Gson();
        ServerAction action = serverActions[0];

        try {
            url = new URL(SPEC);
            connection= (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(10_000);
            connection.connect();
            outputStream=connection.getOutputStream();
            outputStream.write(gson.toJson(action).getBytes(StandardCharsets.UTF_8));
            inputStream=connection.getInputStream();
            byte[] buffer = new byte[1024];
            int actuallyRead;
            StringBuilder data = new StringBuilder();
            while ((actuallyRead=inputStream.read(buffer))!=-1){
                data.append(new String(buffer,0,actuallyRead,StandardCharsets.UTF_8));
            }
            return gson.fromJson(data.toString(),ServerResponse.class);
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
        return new ServerResponse(false,"Connection to server Failed");
    }

    @Override
    protected void onPostExecute(ServerResponse serverResponse) {
        super.onPostExecute(serverResponse);
        if (actualPostExecute != null) {
            actualPostExecute.onPostExecute(serverResponse);
        }
    }
}
