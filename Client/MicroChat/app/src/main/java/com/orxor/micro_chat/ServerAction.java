package com.orxor.micro_chat;

public class ServerAction {

    public static final String ACTION_REGISTER="register";
    public static final String ACTION_LOGIN="login";
    public static final String ACTION_SEND_MESSAGE="message";
    public static final String ACTION_REFRESH_MESSAGES="refresh";
    public static final String ACTION_GET_PHOTO="getPhoto";

    private String action;
    private Message message;
    private User user;

    public ServerAction(String action, Message message, User user) {
        this.action = action;
        this.message = message;
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
