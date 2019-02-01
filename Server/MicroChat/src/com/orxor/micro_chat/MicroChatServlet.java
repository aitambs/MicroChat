package com.orxor.micro_chat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class MicroChatServlet extends javax.servlet.http.HttpServlet {
    //Change the following to relevent folder/directory name!
    private static final String HOME_PATH = "D:/";
    private static final String USERS_JSON_FILE = "users.json";
    private static final String MESSAGES_JSON_FILE = "messages.json";


    private static Map<String,User> userMap=new HashMap<>();
    private static List<Message> messages = new ArrayList<>();
    private AtomicLong id = new AtomicLong(0L);

    @Override
    public void init() throws ServletException {
        super.init();
        Gson gson = new Gson();
        InputStreamReader reader = null;
        File file = null;
        try {
            file = new File(HOME_PATH, USERS_JSON_FILE);
            reader = new InputStreamReader(new FileInputStream(file),StandardCharsets.UTF_8);
            userMap =gson.fromJson(reader, new TypeToken<Map<String,User>>(){}.getType());
            System.out.println("Users Loaded");
        } catch (FileNotFoundException e) {
            System.out.println("No Presaved users");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        try {
            file = new File(HOME_PATH, MESSAGES_JSON_FILE);
            reader = new InputStreamReader(new FileInputStream(file),StandardCharsets.UTF_8);
            messages =gson.fromJson(reader, new TypeToken<List<Message>>(){}.getType());
            System.out.println("Messages Loaded");
            if (!messages.isEmpty()) {
                id = new AtomicLong(messages.get(messages.size() - 1).getId() + 1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No Presaved messages");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        InputStream inputStream=request.getInputStream();
        int actuallyRead;
        byte[] buffer = new byte[1024];
        StringBuilder data = new StringBuilder();
        while((actuallyRead = inputStream.read(buffer))!=-1){
            data.append(new String(buffer,0,actuallyRead,StandardCharsets.UTF_8));
        }
        Gson gson = new Gson();
        ServerAction action;
        try {
            action=gson.fromJson(data.toString(),ServerAction.class);
        } catch (JsonSyntaxException e) {
            return;
        }
        ServerResponse reply;
        switch (action.getAction()){
            case ServerAction.ACTION_REGISTER:
                reply=register(action);
                break;
            case ServerAction.ACTION_LOGIN:
                reply=login(action);
                break;
            case ServerAction.ACTION_SEND_MESSAGE:
                reply=reciveMessage(action);
                break;
            case ServerAction.ACTION_REFRESH_MESSAGES:
                reply=refreshMessages(action);
                break;
            case ServerAction.ACTION_GET_PHOTO:
                reply=getPhoto(action);
                break;
            default:
                return;
        }

        response.setContentType("application/json");
        response.getOutputStream().write(gson.toJson(reply).getBytes(StandardCharsets.UTF_8));

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.getWriter().write("Use Post!");
    }

    private ServerResponse register(ServerAction action){
        synchronized (userMap){
            if (userMap.containsKey(action.getUser().getUserName())){
                return new ServerResponse(false,"User Already Exists");
            } else {
                userMap.put(action.getUser().getUserName(),action.getUser());
                return new ServerResponse(true,"");
            }
        }
    }

    private ServerResponse login(ServerAction action){
        if (!userMap.containsKey(action.getUser().getUserName()) ||
                !userMap.get(action.getUser().getUserName()).getPassword().equals(action.getUser().getPassword())){
            return new ServerResponse(false,"Incorrect Username or Password");
        } else return new ServerResponse(true,"");
    }

    private ServerResponse reciveMessage(ServerAction action) {
        ServerResponse verifyUser=login(action);
        if (!verifyUser.isSuccess()) return verifyUser;
        Message message =action.getMessage();
        message.setId(id.getAndIncrement());
        if (message.isPhoto()){
            String name =String.valueOf(System.currentTimeMillis())+"_"+String.valueOf(System.nanoTime())+".jpeg.b64";
            File file = new File (HOME_PATH,name);
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(message.getExtraContent().getBytes(StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            message.setExtraContent(name);
        }
        messages.add(message);

        PushToFCM push = new PushToFCM(message);
        push.start();

        return new ServerResponse(true,"");
    }

    private ServerResponse refreshMessages(ServerAction action){
        ServerResponse verifyUser=login(action);
        if (!verifyUser.isSuccess()) return verifyUser;
        int index = 0;
        long from= -1;

        try {
            from = Long.parseLong(action.getMessage().getContent());
        } catch (NumberFormatException e) {
            return new ServerResponse(false,"Invalid Params");
        }

        while (index<messages.size() && messages.get(index).getId() <= from){
            index++;
        }
        Gson gson = new Gson();
        return new ServerResponse(true, gson.toJson(messages.subList(index,messages.size())));
    }

    private ServerResponse getPhoto(ServerAction action) {
        ServerResponse verifyUser=login(action);
        if (!verifyUser.isSuccess()) return verifyUser;
        File file = new File(HOME_PATH,action.getMessage().getContent());
        if (file.exists()) {
            InputStream inputStream=null;
            try {
                inputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int actuallyRead;
                StringBuilder builder = new StringBuilder();
                while ((actuallyRead=inputStream.read(buffer))!=-1){
                    builder.append(new String(buffer,0,actuallyRead,StandardCharsets.UTF_8));
                }
                return new ServerResponse(true, builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return new ServerResponse(false,"Photo does not exist!");
    }

    @Override
    public void destroy() {
        super.destroy();
        OutputStreamWriter writer =null;
        Gson gson = new Gson();
        File file=null;
        try {
            file = new File(HOME_PATH, USERS_JSON_FILE);
            writer = new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8);
            writer.write(gson.toJson(userMap));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            file = new File(HOME_PATH, MESSAGES_JSON_FILE);
            writer = new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8);
            writer.write(gson.toJson(messages));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
